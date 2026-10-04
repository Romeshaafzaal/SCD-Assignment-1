import java.io.File;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class FileManager {
    private String dataDir;
    private List<Course> courses = new ArrayList<>();
    private List<VisitingInstructor> visitingInstructors = new ArrayList<>();
    private boolean academicEnabled = false;
    private List<Section> sections;
    private List<Student> students;
    private List<PermanentInstructor> instructors;
    private List<AcademicOfficeAdmin> admins;
    private List<FYPGroup> fypGroups;

    private List<String> unresolvedAssignments = new ArrayList<>();
    private List<String> unresolvedSubmissions = new ArrayList<>();
    private List<String> unresolvedRequests = new ArrayList<>();
    private List<String> unresolvedFypLines = new ArrayList<>();
    private List<String> unresolvedUsers = new ArrayList<>();
    private List<String> unresolvedCourses = new ArrayList<>();
    private List<String> unresolvedSections = new ArrayList<>();
    private List<String> unresolvedEnrollments = new ArrayList<>();
    private List<String> unresolvedAttendance = new ArrayList<>();

    public FileManager(String dataDir, List<Section> sections, List<Student> students,
                       List<PermanentInstructor> instructors, List<AcademicOfficeAdmin> admins,
                       List<FYPGroup> fypGroups) {
        this.dataDir = dataDir;
        this.sections = sections;
        this.students = students;
        this.instructors = instructors;
        this.admins = admins;
        this.fypGroups = fypGroups;
        File dir = new File(dataDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    public FileManager(String dataDir, List<Course> courses, List<Section> sections, List<Student> students,
                       List<PermanentInstructor> instructors, List<VisitingInstructor> visitingInstructors,
                       List<AcademicOfficeAdmin> admins, List<FYPGroup> fypGroups) {
        this(dataDir, sections, students, instructors, admins, fypGroups);
        this.courses = courses;
        this.visitingInstructors = visitingInstructors;
        this.academicEnabled = true;
    }

    private String path(String fileName) {
        return dataDir + "/" + fileName;
    }

    public void loadAll() {
        loadAcademic();
        loadWorkflow();
    }

    public void loadWorkflow() {
        boolean wasEnabled = Logger.isEnabled();
        Logger.setEnabled(false);
        try {
            loadAssignments();
            loadSubmissions();
            loadRequests();
            loadFYPGroups();
        } finally {
            Logger.setEnabled(wasEnabled);
        }
        Logger.log("Workflow data loaded from " + dataDir + " (assignments, submissions, requests, FYP groups)");
    }

    public void saveAll() {
        saveAcademic();
        saveAssignments();
        saveSubmissions();
        saveRequests();
        saveFYPGroups();
    }

    public void saveAssignments() {
        List<String> lines = new ArrayList<>();
        for (Section section : sections) {
            for (Assignment a : section.getAssignments()) {
                lines.add(join(a.getId(), a.getTitle(), a.getDescription(), a.getDeadline().toString(),
                        String.valueOf(a.getTotalMarks()), section.getSectionId(), a.getCreatedBy().getStudentId()));
            }
        }
        lines.addAll(unresolvedAssignments);
        writeLines("assignments.txt", lines);
    }

    public void loadAssignments() {
        unresolvedAssignments.clear();
        for (String line : readLines("assignments.txt")) {
            try {
                String[] f = split(line);
                Section section = findSection(f[5]);
                if (section == null || section.getTeachingAssistant() == null
                        || !section.getTeachingAssistant().getStudentId().equals(f[6])) {
                    unresolvedAssignments.add(line);
                    continue;
                }
                if (findAssignment(f[0]) != null) {
                    continue;
                }
                Assignment a = new Assignment(f[0], f[1], f[2], LocalDate.parse(f[3]), Double.parseDouble(f[4]),
                        section, section.getTeachingAssistant());
                section.addAssignment(a);
            } catch (RuntimeException e) {
                warn("Skipped bad assignment record: " + e.getMessage());
                unresolvedAssignments.add(line);
            }
        }
    }

    public void saveSubmissions() {
        List<String> lines = new ArrayList<>();
        for (Section section : sections) {
            for (Assignment a : section.getAssignments()) {
                for (Submission s : a.getSubmissions()) {
                    Feedback fb = s.getFeedback();
                    lines.add(join(s.getSubmissionId(), a.getId(), s.getStudent().getStudentId(),
                            s.getSubmissionDate().toString(), s.getContent(), String.valueOf(s.getMarks()),
                            s.getStatus().name(),
                            fb == null ? "" : fb.getFeedbackId(),
                            fb == null ? "" : evaluatorId(fb.getEvaluator()),
                            fb == null ? "" : fb.getComments(),
                            fb == null ? "" : fb.getDate().toString()));
                }
            }
        }
        lines.addAll(unresolvedSubmissions);
        writeLines("submissions.txt", lines);
    }

    public void loadSubmissions() {
        unresolvedSubmissions.clear();
        for (String line : readLines("submissions.txt")) {
            try {
                String[] f = split(line);
                Assignment assignment = findAssignment(f[1]);
                Student student = findStudent(f[2]);
                if (assignment == null || student == null) {
                    unresolvedSubmissions.add(line);
                    continue;
                }
                Feedback feedback = null;
                if (!f[7].isEmpty()) {
                    Evaluator evaluator = findEvaluator(f[8]);
                    if (evaluator == null) {
                        unresolvedSubmissions.add(line);
                        continue;
                    }
                    feedback = new Feedback(f[7], evaluator, f[9], LocalDate.parse(f[10]));
                }
                Submission sub = new Submission(f[0], assignment, student, LocalDate.parse(f[3]), f[4],
                        Double.parseDouble(f[5]), feedback, SubmissionStatus.valueOf(f[6]));
                boolean exists = false;
                for (Submission s : assignment.getSubmissions()) {
                    if (s.getSubmissionId().equals(f[0])) {
                        exists = true;
                    }
                }
                if (!exists) {
                    assignment.addSubmission(sub);
                }
            } catch (CampusException | RuntimeException e) {
                warn("Skipped bad submission record: " + e.getMessage());
                unresolvedSubmissions.add(line);
            }
        }
    }

    public void saveRequests() {
        List<String> lines = new ArrayList<>();
        for (AcademicOfficeAdmin admin : admins) {
            for (Request r : admin.viewRequests()) {
                String processedBy = (r.getProcessedBy() == null) ? "" : r.getProcessedBy().getAdminId();
                String extra1 = "";
                String extra2 = "";
                if (r instanceof CourseClashRequest) {
                    CourseClashRequest c = (CourseClashRequest) r;
                    extra1 = c.getConflictingSection().getSectionId();
                    extra2 = c.getRequestedSection().getSectionId();
                } else if (r instanceof GenericRequest) {
                    extra1 = ((GenericRequest) r).getCategory().name();
                }
                lines.add(join(r.getRequestId(), r.getRequestType(), r.getStudent().getStudentId(),
                        r.getRequestDate().toString(), r.getDescription(), String.valueOf(r.getPriority()),
                        r.getStatus().name(), processedBy, extra1, extra2));
            }
        }
        lines.addAll(unresolvedRequests);
        writeLines("requests.txt", lines);
    }

    public void loadRequests() {
        unresolvedRequests.clear();
        for (String line : readLines("requests.txt")) {
            try {
                String[] f = split(line);
                Student student = findStudent(f[2]);
                AcademicOfficeAdmin admin = findAdmin(f[7]);
                if (admin == null && !admins.isEmpty()) {
                    admin = admins.get(0);
                }
                if (student == null || admin == null) {
                    unresolvedRequests.add(line);
                    continue;
                }
                int priority = Integer.parseInt(f[5]);
                LocalDate date = LocalDate.parse(f[3]);
                Request request;
                if (f[1].equals("COURSE_CLASH")) {
                    Section conflicting = findSection(f[8]);
                    Section requested = findSection(f[9]);
                    if (conflicting == null || requested == null) {
                        unresolvedRequests.add(line);
                        continue;
                    }
                    request = new CourseClashRequest(f[0], date, f[4], priority, student, conflicting, requested);
                } else if (f[1].equals("GENERIC")) {
                    request = new GenericRequest(f[0], date, f[4], priority, student, RequestCategory.valueOf(f[8]));
                } else {
                    unresolvedRequests.add(line);
                    continue;
                }
                request.setStatus(RequestStatus.valueOf(f[6]));
                request.setProcessedBy(findAdmin(f[7]));
                if (!hasRequest(admin, f[0])) {
                    admin.receiveRequest(request);
                    student.addRequest(request);
                }
            } catch (CampusException | RuntimeException e) {
                warn("Skipped bad request record: " + e.getMessage());
                unresolvedRequests.add(line);
            }
        }
    }

    private boolean hasRequest(AcademicOfficeAdmin admin, String requestId) {
        for (Request r : admin.viewRequests()) {
            if (r.getRequestId().equals(requestId)) {
                return true;
            }
        }
        return false;
    }

    public void saveFYPGroups() {
        List<String> lines = new ArrayList<>();
        for (FYPGroup g : fypGroups) {
            StringBuilder ids = new StringBuilder();
            for (Student s : g.getMembers()) {
                if (ids.length() > 0) {
                    ids.append(",");
                }
                ids.append(s.getStudentId());
            }
            String supervisorId = (g.getSupervisor() == null) ? "" : g.getSupervisor().getTeacherId();
            lines.add(join("GROUP", g.getGroupId(), g.getTitle(), g.getDescription(), ids.toString(), supervisorId));
            for (FYPMeeting m : g.getMeetings()) {
                lines.add(join("MEETING", g.getGroupId(), m.getMeetingId(), m.getMeetingDate().toString(),
                        m.getAgenda(), m.getNotes()));
            }
            for (FYPEvaluation e : g.getEvaluations()) {
                lines.add(join("EVAL", g.getGroupId(), e.getEvaluationId(), e.getEvaluationDate().toString(),
                        String.valueOf(e.getScore()), e.getFeedback(), evaluatorId(e.getEvaluator())));
            }
        }
        lines.addAll(unresolvedFypLines);
        writeLines("fypgroups.txt", lines);
    }

    public void loadFYPGroups() {
        unresolvedFypLines.clear();
        for (String line : readLines("fypgroups.txt")) {
            try {
                String[] f = split(line);
                if (f[0].equals("GROUP")) {
                    loadGroupLine(f, line);
                } else if (f[0].equals("MEETING")) {
                    FYPGroup g = findGroup(f[1]);
                    if (g == null) {
                        unresolvedFypLines.add(line);
                    } else if (!hasMeeting(g, f[2])) {
                        g.addMeeting(new FYPMeeting(f[2], LocalDate.parse(f[3]), f[4], f[5]));
                    }
                } else if (f[0].equals("EVAL")) {
                    FYPGroup g = findGroup(f[1]);
                    Evaluator evaluator = findEvaluator(f[6]);
                    if (g == null || evaluator == null) {
                        unresolvedFypLines.add(line);
                    } else if (!hasEvaluation(g, f[2])) {
                        FYPEvaluation e = new FYPEvaluation(f[2], LocalDate.parse(f[3]), evaluator);
                        e.evaluate(Double.parseDouble(f[4]));
                        if (!f[5].isEmpty()) {
                            e.addFeedback(f[5]);
                        }
                        g.addEvaluation(e);
                    }
                } else {
                    unresolvedFypLines.add(line);
                }
            } catch (CampusException | RuntimeException e) {
                warn("Skipped bad FYP record: " + e.getMessage());
                unresolvedFypLines.add(line);
            }
        }
    }

    private void loadGroupLine(String[] f, String line) throws CampusException {
        if (findGroup(f[1]) != null) {
            return;
        }
        List<Student> members = new ArrayList<>();
        if (!f[4].isEmpty()) {
            for (String id : f[4].split(",")) {
                Student s = findStudent(id);
                if (s == null) {
                    unresolvedFypLines.add(line);
                    return;
                }
                members.add(s);
            }
        }
        PermanentInstructor supervisor = null;
        if (!f[5].isEmpty()) {
            supervisor = findInstructor(f[5]);
            if (supervisor == null) {
                unresolvedFypLines.add(line);
                return;
            }
        }
        FYPGroup group = new FYPGroup(f[1], f[2], f[3]);
        for (Student s : members) {
            group.addMember(s);
        }
        if (supervisor != null) {
            group.assignSupervisor(supervisor);
        }
        fypGroups.add(group);
    }

    private boolean hasMeeting(FYPGroup g, String id) {
        for (FYPMeeting m : g.getMeetings()) {
            if (m.getMeetingId().equals(id)) {
                return true;
            }
        }
        return false;
    }

    private boolean hasEvaluation(FYPGroup g, String id) {
        for (FYPEvaluation e : g.getEvaluations()) {
            if (e.getEvaluationId().equals(id)) {
                return true;
            }
        }
        return false;
    }

    public boolean academicDataExists() {
        return !readLines("courses.txt").isEmpty() || !readLines("users.txt").isEmpty();
    }

    public void saveAcademic() {
        if (!academicEnabled) {
            return;
        }
        saveUsers();
        saveCourses();
        saveSections();
        saveEnrollments();
        saveAttendance();
    }

    public void loadAcademic() {
        if (!academicEnabled) {
            return;
        }
        boolean wasEnabled = Logger.isEnabled();
        Logger.setEnabled(false);
        try {
            loadUsers();
            loadCourses();
            loadSections();
            loadEnrollments();
            loadAttendance();
        } finally {
            Logger.setEnabled(wasEnabled);
        }
        Logger.log("Academic data loaded from " + dataDir + ": " + courses.size() + " courses, "
                + sections.size() + " sections, " + students.size() + " students");
    }

    private void saveUsers() {
        List<String> lines = new ArrayList<>();
        for (Student s : students) {
            if (s instanceof NormalStudent) {
                lines.add(join("STUDENT", s.getStudentId(), s.getName(), s.getEmail(), s.getPhone()));
            }
        }
        for (PermanentInstructor i : instructors) {
            lines.add(join("PERMANENT", i.getTeacherId(), i.getName(), i.getEmail(), i.getPhone()));
        }
        for (VisitingInstructor i : visitingInstructors) {
            lines.add(join("VISITING", i.getTeacherId(), i.getName(), i.getEmail(), i.getPhone()));
        }
        for (AcademicOfficeAdmin a : admins) {
            lines.add(join("ADMIN", a.getAdminId(), a.getName(), a.getEmail(), a.getPhone()));
        }
        lines.addAll(unresolvedUsers);
        writeLines("users.txt", lines);
    }

    private void loadUsers() {
        unresolvedUsers.clear();
        for (String line : readLines("users.txt")) {
            try {
                String[] f = split(line);
                String id = f[1];
                if (f[0].equals("STUDENT")) {
                    if (findRegisteredStudent(id) == null) {
                        students.add(new NormalStudent(id, f[2], f[3], f[4]));
                    }
                } else if (f[0].equals("PERMANENT")) {
                    if (findInstructor(id) == null) {
                        instructors.add(new PermanentInstructor(id, f[2], f[3], f[4]));
                    }
                } else if (f[0].equals("VISITING")) {
                    if (findVisitingInstructor(id) == null) {
                        visitingInstructors.add(new VisitingInstructor(id, f[2], f[3], f[4]));
                    }
                } else if (f[0].equals("ADMIN")) {
                    if (findAdmin(id) == null) {
                        admins.add(new AcademicOfficeAdmin(id, f[2], f[3], f[4], courses, sections));
                    }
                } else {
                    unresolvedUsers.add(line);
                }
            } catch (RuntimeException e) {
                warn("Skipped bad user record: " + e.getMessage());
                unresolvedUsers.add(line);
            }
        }
    }

    private void saveCourses() {
        List<String> lines = new ArrayList<>();
        for (Course c : courses) {
            StringBuilder prerequisites = new StringBuilder();
            for (Course p : c.getPrerequisites()) {
                if (prerequisites.length() > 0) {
                    prerequisites.append(",");
                }
                prerequisites.append(p.getCourseCode());
            }
            lines.add(join(c.getCourseCode(), c.getTitle(), String.valueOf(c.getCreditHours()),
                    prerequisites.toString()));
        }
        lines.addAll(unresolvedCourses);
        writeLines("courses.txt", lines);
    }

    private void loadCourses() {
        unresolvedCourses.clear();
        List<String[]> loaded = new ArrayList<>();
        for (String line : readLines("courses.txt")) {
            try {
                String[] f = split(line);
                if (findCourse(f[0]) == null) {
                    courses.add(new Course(f[0], f[1], Integer.parseInt(f[2])));
                }
                loaded.add(f);
            } catch (RuntimeException e) {
                warn("Skipped bad course record: " + e.getMessage());
                unresolvedCourses.add(line);
            }
        }
        for (String[] f : loaded) {
            if (f.length > 3 && !f[3].isEmpty()) {
                Course course = findCourse(f[0]);
                for (String code : f[3].split(",")) {
                    Course prerequisite = findCourse(code);
                    if (prerequisite == null) {
                        warn("Prerequisite " + code + " of course " + f[0] + " was not found");
                    } else {
                        course.addPrerequisite(prerequisite);
                    }
                }
            }
        }
    }

    private void saveSections() {
        List<String> lines = new ArrayList<>();
        for (Section s : sections) {
            Schedule sc = s.getSchedule();
            String instructorId = (s.getInstructor() == null) ? "" : s.getInstructor().getTeacherId();
            String taId = (s.getTeachingAssistant() == null) ? "" : s.getTeachingAssistant().getStudentId();
            lines.add(join(s.getSectionId(), s.getCourse().getCourseCode(), String.valueOf(s.getCapacity()),
                    sc.getDay().name(), sc.getStartTime().toString(), sc.getEndTime().toString(), sc.getRoom(),
                    instructorId, taId));
        }
        lines.addAll(unresolvedSections);
        writeLines("sections.txt", lines);
    }

    private void loadSections() {
        unresolvedSections.clear();
        for (String line : readLines("sections.txt")) {
            try {
                String[] f = split(line);
                if (findSection(f[0]) != null) {
                    continue;
                }
                Course course = findCourse(f[1]);
                Instructor instructor = f[7].isEmpty() ? null : findAnyInstructor(f[7]);
                Student taStudent = f[8].isEmpty() ? null : findRegisteredStudent(f[8]);
                if (course == null || (!f[7].isEmpty() && instructor == null) || (!f[8].isEmpty() && taStudent == null)) {
                    unresolvedSections.add(line);
                    continue;
                }
                Schedule schedule = new Schedule(Day.valueOf(f[3]), LocalTime.parse(f[4]), LocalTime.parse(f[5]), f[6]);
                Section section = new Section(f[0], Integer.parseInt(f[2]), course, schedule);
                sections.add(section);
                course.addSection(section);
                if (instructor != null) {
                    section.assignInstructor(instructor);
                    instructor.addAssignedSection(section);
                }
                if (taStudent != null) {
                    section.assignTA(new TeachingAssistant(taStudent.getStudentId(), section, taStudent.getName(),
                            taStudent.getEmail(), taStudent.getPhone()));
                }
            } catch (RuntimeException e) {
                warn("Skipped bad section record: " + e.getMessage());
                unresolvedSections.add(line);
            }
        }
    }

    private void saveEnrollments() {
        List<String> lines = new ArrayList<>();
        for (Student s : students) {
            for (Enrollment e : s.getEnrollments()) {
                lines.add(join(e.getEnrollmentId(), s.getStudentId(), e.getSection().getSectionId(),
                        e.getEnrollmentDate().toString(), e.getStatus().name()));
            }
        }
        lines.addAll(unresolvedEnrollments);
        writeLines("enrollments.txt", lines);
    }

    private void loadEnrollments() {
        unresolvedEnrollments.clear();
        for (String line : readLines("enrollments.txt")) {
            try {
                String[] f = split(line);
                Student student = findRegisteredStudent(f[1]);
                Section section = findSection(f[2]);
                if (student == null || section == null) {
                    unresolvedEnrollments.add(line);
                    continue;
                }
                boolean exists = false;
                for (Enrollment e : student.getEnrollments()) {
                    if (e.getEnrollmentId().equals(f[0])) {
                        exists = true;
                    }
                }
                if (!exists) {
                    Enrollment enrollment = new Enrollment(f[0], student, section, LocalDate.parse(f[3]),
                            EnrollmentStatus.valueOf(f[4]));
                    student.restoreEnrollment(enrollment);
                    section.restoreEnrollment(enrollment);
                }
            } catch (RuntimeException e) {
                warn("Skipped bad enrollment record: " + e.getMessage());
                unresolvedEnrollments.add(line);
            }
        }
    }

    private void saveAttendance() {
        List<String> lines = new ArrayList<>();
        List<Instructor> everyone = new ArrayList<>();
        everyone.addAll(instructors);
        everyone.addAll(visitingInstructors);
        for (Instructor i : everyone) {
            for (Attendance a : i.getAttendanceRecords()) {
                lines.add(join(i.getTeacherId(), a.getStudent().getStudentId(), a.getSection().getSectionId(),
                        a.getDate().toString(), a.getStatus().name()));
            }
        }
        lines.addAll(unresolvedAttendance);
        writeLines("attendance.txt", lines);
    }

    private void loadAttendance() {
        unresolvedAttendance.clear();
        for (String line : readLines("attendance.txt")) {
            try {
                String[] f = split(line);
                Instructor instructor = findAnyInstructor(f[0]);
                Student student = findRegisteredStudent(f[1]);
                Section section = findSection(f[2]);
                if (instructor == null || student == null || section == null) {
                    unresolvedAttendance.add(line);
                    continue;
                }
                LocalDate date = LocalDate.parse(f[3]);
                AttendanceStatus status = AttendanceStatus.valueOf(f[4]);
                Attendance existing = instructor.findAttendance(student, section, date);
                if (existing == null) {
                    instructor.addAttendanceRecord(new Attendance(student, section, date, status));
                } else if (existing.getStatus() != status) {
                    warn("Conflicting duplicate attendance record kept aside: " + line);
                    unresolvedAttendance.add(line);
                }
            } catch (RuntimeException e) {
                warn("Skipped bad attendance record: " + e.getMessage());
                unresolvedAttendance.add(line);
            }
        }
    }

    private Course findCourse(String code) {
        for (Course c : courses) {
            if (c.getCourseCode().equals(code)) {
                return c;
            }
        }
        return null;
    }

    private Section findSection(String id) {
        if (id == null || id.isEmpty()) {
            return null;
        }
        for (Section s : sections) {
            if (s.getSectionId().equals(id)) {
                return s;
            }
        }
        return null;
    }

    private Student findRegisteredStudent(String id) {
        for (Student s : students) {
            if (s.getStudentId().equals(id)) {
                return s;
            }
        }
        return null;
    }

    private Student findStudent(String id) {
        Student registered = findRegisteredStudent(id);
        if (registered != null) {
            return registered;
        }
        for (Section section : sections) {
            TeachingAssistant ta = section.getTeachingAssistant();
            if (ta != null && ta.getStudentId().equals(id)) {
                return ta;
            }
        }
        return null;
    }

    private PermanentInstructor findInstructor(String id) {
        for (PermanentInstructor i : instructors) {
            if (i.getTeacherId().equals(id)) {
                return i;
            }
        }
        return null;
    }

    private VisitingInstructor findVisitingInstructor(String id) {
        for (VisitingInstructor v : visitingInstructors) {
            if (v.getTeacherId().equals(id)) {
                return v;
            }
        }
        return null;
    }

    private Instructor findAnyInstructor(String id) {
        Instructor p = findInstructor(id);
        return p != null ? p : findVisitingInstructor(id);
    }

    private AcademicOfficeAdmin findAdmin(String id) {
        if (id == null || id.isEmpty()) {
            return null;
        }
        for (AcademicOfficeAdmin a : admins) {
            if (a.getAdminId().equals(id)) {
                return a;
            }
        }
        return null;
    }

    private FYPGroup findGroup(String id) {
        for (FYPGroup g : fypGroups) {
            if (g.getGroupId().equals(id)) {
                return g;
            }
        }
        return null;
    }

    private Assignment findAssignment(String id) {
        for (Section s : sections) {
            for (Assignment a : s.getAssignments()) {
                if (a.getId().equals(id)) {
                    return a;
                }
            }
        }
        return null;
    }

    private Evaluator findEvaluator(String id) {
        Instructor instructor = findAnyInstructor(id);
        if (instructor instanceof Evaluator) {
            return (Evaluator) instructor;
        }
        Student student = findStudent(id);
        if (student instanceof Evaluator) {
            return (Evaluator) student;
        }
        return null;
    }

    private String evaluatorId(Evaluator evaluator) {
        if (evaluator instanceof Instructor) {
            return ((Instructor) evaluator).getTeacherId();
        }
        if (evaluator instanceof Student) {
            return ((Student) evaluator).getStudentId();
        }
        return "";
    }

    private List<String> readLines(String file) {
        return FileUtil.readLines(path(file));
    }

    private void writeLines(String file, List<String> lines) {
        FileUtil.writeLines(path(file), lines);
    }

    private String join(String... fields) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < fields.length; i++) {
            if (i > 0) sb.append("|");
            sb.append(FileUtil.escape(fields[i]));
        }
        return sb.toString();
    }

    private String[] split(String line) {
        List<String> result = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean escaped = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (escaped) {
                if (c == 'n') current.append('\n');
                else if (c == 'r') current.append('\r');
                else current.append(c);
                escaped = false;
            } else if (c == '\\') {
                escaped = true;
            } else if (c == '|') {
                result.add(current.toString());
                current.setLength(0);
            } else {
                current.append(c);
            }
        }
        result.add(current.toString());
        return result.toArray(new String[0]);
    }

    private void warn(String message) {
        Logger.log("WARNING: " + message);
    }
}
