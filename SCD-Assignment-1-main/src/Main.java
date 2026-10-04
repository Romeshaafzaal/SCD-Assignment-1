import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;

public class Main {
    private static Scanner in = new Scanner(System.in);

    private static List<Course> courses = new ArrayList<>();
    private static List<Section> sections = new ArrayList<>();
    private static List<Student> students = new ArrayList<>();
    private static List<PermanentInstructor> permanentInstructors = new ArrayList<>();
    private static List<VisitingInstructor> visitingInstructors = new ArrayList<>();
    private static List<AcademicOfficeAdmin> admins = new ArrayList<>();
    private static List<FYPGroup> fypGroups = new ArrayList<>();
    private static FileManager fileManager;

    public static void main(String[] args) {
        fileManager = new FileManager("data", courses, sections, students, permanentInstructors,
                visitingInstructors, admins, fypGroups);
        if (fileManager.academicDataExists()) {
            fileManager.loadAll();
        } else {
            setupSampleData();
            fileManager.saveAcademic();
            Logger.log("No saved academic data found - sample data created and saved to the data folder");
            fileManager.loadWorkflow();
        }

        boolean running = true;
        while (running) {
            System.out.println("\n===== CAMPUS MANAGEMENT SYSTEM =====");
            System.out.println("1. Student");
            System.out.println("2. Teaching Assistant");
            System.out.println("3. Academic Office Admin");
            System.out.println("4. Permanent Instructor");
            System.out.println("5. Visiting Instructor");
            System.out.println("0. Exit");
            int choice = readInt("Choose role: ", 0, 5);
            switch (choice) {
                case 1:
                    Student student = pick("Select student", students);
                    if (student != null) studentMenu(student);
                    break;
                case 2:
                    List<TeachingAssistant> tas = new ArrayList<>();
                    for (Section s : sections) {
                        if (s.getTeachingAssistant() != null) tas.add(s.getTeachingAssistant());
                    }
                    TeachingAssistant ta = pick("Select teaching assistant", tas);
                    if (ta != null) taMenu(ta);
                    break;
                case 3:
                    AcademicOfficeAdmin admin = pick("Select admin", admins);
                    if (admin != null) adminMenu(admin);
                    break;
                case 4:
                    PermanentInstructor pi = pick("Select instructor", permanentInstructors);
                    if (pi != null) instructorMenu(pi);
                    break;
                case 5:
                    VisitingInstructor vi = pick("Select instructor", visitingInstructors);
                    if (vi != null) instructorMenu(vi);
                    break;
                default:
                    running = false;
            }
        }
        fileManager.saveAll();
        System.out.println("Goodbye.");
    }

    private static void setupSampleData() {
        Course cs101 = new Course("CS101", "Programming Fundamentals", 3);
        Course cs201 = new Course("CS201", "Data Structures", 3);
        Course mt101 = new Course("MT101", "Calculus", 3);
        Course cs301 = new Course("CS301", "Software Construction", 3);
        courses.add(cs101);
        courses.add(cs201);
        courses.add(mt101);
        courses.add(cs301);

        Section s1 = new Section("S1", 30, cs101, new Schedule(Day.MONDAY, LocalTime.of(9, 0), LocalTime.of(10, 30), "R101"));
        Section s2 = new Section("S2", 30, cs201, new Schedule(Day.MONDAY, LocalTime.of(10, 0), LocalTime.of(11, 30), "R102"));
        Section s3 = new Section("S3", 30, mt101, new Schedule(Day.TUESDAY, LocalTime.of(9, 0), LocalTime.of(10, 30), "R103"));
        Section s4 = new Section("S4", 30, cs301, new Schedule(Day.WEDNESDAY, LocalTime.of(11, 0), LocalTime.of(12, 30), "R104"));
        sections.add(s1);
        sections.add(s2);
        sections.add(s3);
        sections.add(s4);
        cs101.addSection(s1);
        cs201.addSection(s2);
        mt101.addSection(s3);
        cs301.addSection(s4);

        PermanentInstructor t1 = new PermanentInstructor("T001", "Dr. Ayesha Khan", "ayesha@campus.edu", "0300-1111111");
        PermanentInstructor t2 = new PermanentInstructor("T002", "Dr. Bilal Ahmed", "bilal@campus.edu", "0300-2222222");
        VisitingInstructor t3 = new VisitingInstructor("T003", "Mr. Usman Tariq", "usman@campus.edu", "0300-3333333");
        permanentInstructors.add(t1);
        permanentInstructors.add(t2);
        visitingInstructors.add(t3);

        s1.assignInstructor(t1);
        t1.addAssignedSection(s1);
        s4.assignInstructor(t1);
        t1.addAssignedSection(s4);
        s2.assignInstructor(t2);
        t2.addAssignedSection(s2);
        s3.assignInstructor(t3);
        t3.addAssignedSection(s3);

        NormalStudent ali = new NormalStudent("F001", "Ali Raza", "ali@campus.edu", "0301-0000001");
        NormalStudent sara = new NormalStudent("F002", "Sara Malik", "sara@campus.edu", "0301-0000002");
        NormalStudent hamza = new NormalStudent("F003", "Hamza Iqbal", "hamza@campus.edu", "0301-0000003");
        NormalStudent zainab = new NormalStudent("F004", "Zainab Noor", "zainab@campus.edu", "0301-0000004");
        NormalStudent omar = new NormalStudent("F005", "Omar Farooq", "omar@campus.edu", "0301-0000005");
        students.add(ali);
        students.add(sara);
        students.add(hamza);
        students.add(zainab);
        students.add(omar);

        try {
            ali.register(s1);
            sara.register(s1);
            hamza.register(s1);
            sara.register(s3);
            zainab.register(s3);
            omar.register(s3);
            ali.register(s4);
            hamza.register(s4);
            t1.assignTA(omar, s1);
            t1.assignTA(zainab, s4);
        } catch (CampusException e) {
            Logger.logError("setupSampleData", e);
            System.out.println("Sample data error: " + e.getMessage());
        }

        admins.add(new AcademicOfficeAdmin("A001", "Ms. Hina Shah", "hina@campus.edu", "0302-0000001", courses, sections));
    }

    private static void studentMenu(Student student) {
        boolean back = false;
        while (!back) {
            System.out.println("\n--- Student: " + student.getName() + " (" + student.getStudentId() + ") ---");
            System.out.println("1. View Available Courses");
            System.out.println("2. View Course Credit Hours");
            System.out.println("3. Register Course");
            System.out.println("4. Drop Course");
            System.out.println("5. View Registered Courses");
            System.out.println("6. View Timetable");
            System.out.println("7. Calculate Total Credit Hours");
            System.out.println("8. View Attendance");
            System.out.println("9. View Attendance Percentage");
            System.out.println("10. View Assignments");
            System.out.println("11. Submit Assignment");
            System.out.println("12. Submit Course Clash Request");
            System.out.println("13. Submit Generic Request");
            System.out.println("14. View Requests");
            System.out.println("15. View Request Status");
            System.out.println("0. Back");
            int choice = readInt("Choose: ", 0, 15);
            try {
                switch (choice) {
                    case 1:
                        if (courses.isEmpty()) System.out.println("No courses available.");
                        for (Course c : courses) {
                            System.out.println(c.getCourseCode() + " - " + c.getTitle() + " (" + c.getCreditHours() + " cr)");
                            for (Section s : c.getSections()) {
                                System.out.println("    " + describe(s) + " | Seats left: " + s.getAvailableSeats());
                            }
                        }
                        break;
                    case 2:
                        for (Course c : courses) {
                            System.out.println(c.getCourseCode() + " - " + c.getTitle() + ": " + c.getCreditHours() + " credit hours");
                        }
                        break;
                    case 3:
                        Course course = pick("Select course", courses);
                        Section toRegister = (course == null) ? null : pick("Select section", course.getSections());
                        if (toRegister != null) {
                            student.register(toRegister);
                            fileManager.saveAll();
                            System.out.println("Registered. Total credit hours now: " + student.calculateTotalCreditHours());
                        }
                        break;
                    case 4:
                        Section toDrop = pick("Select section to drop", sectionsOf(student));
                        if (toDrop != null) {
                            student.drop(toDrop);
                            fileManager.saveAll();
                            System.out.println("Dropped. Total credit hours now: " + student.calculateTotalCreditHours());
                        }
                        break;
                    case 5:
                        if (student.viewCourses().isEmpty()) System.out.println("No registered courses.");
                        for (Course c : student.viewCourses()) {
                            System.out.println(c.getCourseCode() + " - " + c.getTitle() + " (" + c.getCreditHours() + " cr)");
                        }
                        break;
                    case 6:
                        if (student.viewTimetable().isEmpty()) System.out.println("Timetable is empty.");
                        for (Schedule s : student.viewTimetable()) {
                            System.out.println(s.getScheduleInfo());
                        }
                        break;
                    case 7:
                        System.out.println("Total registered credit hours: " + student.calculateTotalCreditHours());
                        break;
                    case 8:
                        boolean any = false;
                        for (Section s : sectionsOf(student)) {
                            for (Attendance a : student.viewAttendance(s)) {
                                System.out.println(s.getSectionId() + " | " + a.getDate() + " | " + a.getStatus());
                                any = true;
                            }
                        }
                        if (!any) System.out.println("No attendance recorded yet.");
                        break;
                    case 9:
                        Section forPercentage = pick("Select section", sectionsOf(student));
                        if (forPercentage != null) {
                            System.out.println("Attendance: " + String.format("%.1f", student.viewAttendancePercentage(forPercentage)) + "%");
                        }
                        break;
                    case 10:
                        List<Assignment> assignments = student.viewAssignments();
                        assignments.sort(new AssignmentDeadlineComparator());
                        if (assignments.isEmpty()) System.out.println("No assignments.");
                        for (Assignment a : assignments) {
                            Submission sub = a.getSubmissionBy(student);
                            System.out.println(a.getDetails() + " | Your status: "
                                    + (sub == null ? "PENDING" : sub.getStatus()));
                        }
                        break;
                    case 11:
                        Assignment toSubmit = pick("Select assignment", student.viewAssignments());
                        if (toSubmit != null) {
                            String content = readLine("Submission content: ");
                            Submission sub = student.submitAssignment(toSubmit, content);
                            System.out.println("Submitted. Status: " + sub.getStatus());
                            fileManager.saveAll();
                        }
                        break;
                    case 12:
                        submitClashRequest(student);
                        break;
                    case 13:
                        submitGenericRequest(student);
                        break;
                    case 14:
                        List<Request> myRequests = student.viewRequests(chooseRequestComparator());
                        if (myRequests.isEmpty()) System.out.println("No requests.");
                        for (Request r : myRequests) System.out.println(r.getDetails());
                        break;
                    case 15:
                        Request r = pick("Select request", student.viewRequests());
                        if (r != null) {
                            System.out.println("Status: " + student.viewRequestStatus(r.getRequestId()));
                        }
                        break;
                    default:
                        back = true;
                }
            } catch (CampusException | RuntimeException e) {
                Logger.logError("studentMenu", e);
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    private static void submitClashRequest(Student student) throws CampusException {
        Section conflicting = pick("Select the section you are ALREADY enrolled in", sectionsOf(student));
        Section requested = (conflicting == null) ? null : pick("Select the section you want (it clashes)", sections);
        if (conflicting == null || requested == null) return;
        String description = readLine("Reason: ");
        CourseClashRequest request = new CourseClashRequest(IdGenerator.nextId("R"), LocalDate.now(),
                description, 1, student, conflicting, requested);
        student.submitCourseClashRequest(request);
        admins.get(0).receiveRequest(request);
        fileManager.saveAll();
        System.out.println("Request submitted: " + request.getRequestId());
    }

    private static void submitGenericRequest(Student student) throws CampusException {
        List<RequestCategory> categories = Arrays.asList(RequestCategory.values());
        RequestCategory category = pick("Select category", categories);
        if (category == null) return;
        String description = readLine("Description: ");
        int priority = readInt("Priority (1 = most urgent, bigger number = less urgent): ", 1, Integer.MAX_VALUE);
        GenericRequest request = new GenericRequest(IdGenerator.nextId("R"), LocalDate.now(),
                description, priority, student, category);
        student.submitGenericRequest(request);
        admins.get(0).receiveRequest(request);
        fileManager.saveAll();
        System.out.println("Request submitted: " + request.getRequestId());
    }

    private static List<Section> sectionsOf(Student student) {
        List<Section> result = new ArrayList<>();
        for (Enrollment e : student.getEnrollments()) {
            if (e.getStatus() == EnrollmentStatus.ACTIVE) result.add(e.getSection());
        }
        return result;
    }

    private static Comparator<Request> chooseRequestComparator() {
        int sort = readInt("Sort by: 1 = priority, 2 = date, 3 = none: ", 1, 3);
        if (sort == 1) return new RequestPriorityComparator();
        if (sort == 2) return new RequestDateComparator();
        return new Comparator<Request>() {
            public int compare(Request a, Request b) {
                return 0;
            }
        };
    }

    private static void taMenu(TeachingAssistant ta) {
        boolean back = false;
        while (!back) {
            System.out.println("\n--- Teaching Assistant: " + ta.getName() + " ---");
            System.out.println("1. View Assigned Section");
            System.out.println("2. View Enrolled Students");
            System.out.println("3. Create Assignment");
            System.out.println("4. View Assignments");
            System.out.println("5. Change Assignment Deadline");
            System.out.println("6. Change Assignment Total Marks");
            System.out.println("7. View Submissions");
            System.out.println("8. Check Late Submissions");
            System.out.println("9. Evaluate Submission");
            System.out.println("10. Give Feedback");
            System.out.println("0. Back");
            int choice = readInt("Choose: ", 0, 10);
            try {
                switch (choice) {
                    case 1:
                        System.out.println(describe(ta.getAssignedSection()));
                        break;
                    case 2:
                        List<Student> enrolled = ta.viewEnrolledStudents();
                        enrolled.sort(new StudentNameComparator());
                        if (enrolled.isEmpty()) System.out.println("No students enrolled.");
                        for (Student s : enrolled) System.out.println(s.getStudentId() + " - " + s.getName());
                        break;
                    case 3:
                        String title = readLine("Title: ");
                        String description = readLine("Description: ");
                        LocalDate deadline = LocalDate.parse(readLine("Deadline (yyyy-mm-dd): "));
                        double total = Double.parseDouble(readLine("Total marks: "));
                        Assignment created = ta.createAssignment(title, description, deadline, total);
                        fileManager.saveAll();
                        System.out.println("Created: " + created.getDetails());
                        break;
                    case 4:
                        List<Assignment> mine = new ArrayList<>(ta.viewAssignments());
                        mine.sort(new AssignmentDeadlineComparator());
                        if (mine.isEmpty()) System.out.println("No assignments.");
                        for (Assignment a : mine) System.out.println(a.getDetails());
                        break;
                    case 5:
                        Assignment a5 = pick("Select assignment", ta.viewAssignments());
                        if (a5 != null) {
                            ta.setAssignmentDeadline(a5, LocalDate.parse(readLine("New deadline (yyyy-mm-dd): ")));
                            fileManager.saveAll();
                            System.out.println("Deadline updated.");
                        }
                        break;
                    case 6:
                        Assignment a6 = pick("Select assignment", ta.viewAssignments());
                        if (a6 != null) {
                            ta.setAssignmentTotalMarks(a6, Double.parseDouble(readLine("New total marks: ")));
                            fileManager.saveAll();
                            System.out.println("Total marks updated.");
                        }
                        break;
                    case 7:
                        Assignment a7 = pick("Select assignment", ta.viewAssignments());
                        if (a7 != null) {
                            List<Submission> subs = ta.viewSubmissions(a7);
                            if (subs.isEmpty()) System.out.println("No submissions yet.");
                            for (Submission s : subs) System.out.println(describe(s));
                        }
                        break;
                    case 8:
                        Assignment a8 = pick("Select assignment", ta.viewAssignments());
                        if (a8 != null) {
                            List<Submission> late = ta.checkLateSubmissions(a8);
                            if (late.isEmpty()) System.out.println("No late submissions.");
                            for (Submission s : late) System.out.println(describe(s));
                        }
                        break;
                    case 9:
                        Submission toEvaluate = pickSubmission(ta);
                        if (toEvaluate != null) {
                            double marks = Double.parseDouble(readLine("Marks (out of "
                                    + toEvaluate.getAssignment().getTotalMarks() + "): "));
                            ta.evaluateSubmission(toEvaluate, marks);
                            fileManager.saveAll();
                            System.out.println("Marks saved.");
                        }
                        break;
                    case 10:
                        Submission toComment = pickSubmission(ta);
                        if (toComment != null) {
                            ta.giveFeedback(toComment, readLine("Feedback: "));
                            fileManager.saveAll();
                            System.out.println("Feedback saved.");
                        }
                        break;
                    default:
                        back = true;
                }
            } catch (CampusException | RuntimeException e) {
                Logger.logError("taMenu", e);
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    private static Submission pickSubmission(TeachingAssistant ta) throws CampusException {
        Assignment assignment = pick("Select assignment", ta.viewAssignments());
        if (assignment == null) return null;
        return pick("Select submission", ta.viewSubmissions(assignment));
    }

    private static void adminMenu(AcademicOfficeAdmin admin) {
        boolean back = false;
        while (!back) {
            System.out.println("\n--- Academic Office Admin: " + admin.getName() + " ---");
            System.out.println("1. Create Course");
            System.out.println("2. Update Course");
            System.out.println("3. Search Course");
            System.out.println("4. Create Section");
            System.out.println("5. Update Section");
            System.out.println("6. Set Section Capacity");
            System.out.println("7. Assign Room");
            System.out.println("8. Assign Instructor");
            System.out.println("9. View Requests");
            System.out.println("10. Approve Request");
            System.out.println("11. Reject Request");
            System.out.println("12. View All Courses and Sections");
            System.out.println("0. Back");
            int choice = readInt("Choose: ", 0, 12);
            try {
                switch (choice) {
                    case 1:
                        String code = readLine("Course code: ");
                        String title = readLine("Title: ");
                        int credits = Integer.parseInt(readLine("Credit hours: "));
                        admin.createCourse(new Course(code, title, credits));
                        fileManager.saveAll();
                        System.out.println("Course created.");
                        break;
                    case 2:
                        Course toUpdate = pick("Select course", admin.viewCourses());
                        if (toUpdate != null) {
                            String newTitle = readLine("New title: ");
                            int newCredits = Integer.parseInt(readLine("New credit hours: "));
                            admin.updateCourse(new Course(toUpdate.getCourseCode(), newTitle, newCredits));
                            fileManager.saveAll();
                            System.out.println("Course updated.");
                        }
                        break;
                    case 3:
                        Course found = admin.searchCourse(readLine("Course code: "));
                        if (found == null) {
                            System.out.println("No course found with that code.");
                        } else {
                            printCourse(found);
                        }
                        break;
                    case 4:
                        String sectionId = readLine("Section ID: ");
                        Course sectionCourse = pick("Select course", admin.viewCourses());
                        if (sectionCourse != null) {
                            int capacity = Integer.parseInt(readLine("Capacity: "));
                            Schedule schedule = readSchedule();
                            admin.createSection(new Section(sectionId, capacity, sectionCourse, schedule));
                            fileManager.saveAll();
                            System.out.println("Section created.");
                        }
                        break;
                    case 5:
                        Section sectionToUpdate = pick("Select section", admin.viewSections());
                        if (sectionToUpdate != null) {
                            int newCapacity = Integer.parseInt(readLine("New capacity: "));
                            Schedule newSchedule = readSchedule();
                            admin.updateSection(new Section(sectionToUpdate.getSectionId(), newCapacity,
                                    sectionToUpdate.getCourse(), newSchedule));
                            fileManager.saveAll();
                            System.out.println("Section updated.");
                        }
                        break;
                    case 6:
                        Section forCapacity = pick("Select section", admin.viewSections());
                        if (forCapacity != null) {
                            admin.setCapacity(forCapacity, Integer.parseInt(readLine("New capacity: ")));
                            fileManager.saveAll();
                            System.out.println("Capacity updated.");
                        }
                        break;
                    case 7:
                        Section forRoom = pick("Select section", admin.viewSections());
                        if (forRoom != null) {
                            String room = readLine("Room: ");
                            Schedule old = forRoom.getSchedule();
                            admin.assignRoom(forRoom, new Schedule(old.getDay(), old.getStartTime(), old.getEndTime(), room));
                            fileManager.saveAll();
                            System.out.println("Room assigned.");
                        }
                        break;
                    case 8:
                        Section forInstructor = pick("Select section", admin.viewSections());
                        Instructor instructor = (forInstructor == null) ? null : pick("Select instructor", allInstructors());
                        if (instructor != null) {
                            admin.assignInstructor(forInstructor, instructor);
                            fileManager.saveAll();
                            System.out.println("Instructor assigned.");
                        }
                        break;
                    case 9:
                        List<Request> all = admin.viewRequests(chooseRequestComparator());
                        if (all.isEmpty()) System.out.println("No requests.");
                        for (Request r : all) System.out.println(r.getDetails());
                        break;
                    case 10:
                        Request toApprove = pick("Select pending request", admin.viewPendingRequests());
                        if (toApprove != null) {
                            admin.approveRequest(toApprove);
                            fileManager.saveAll();
                            System.out.println("Request approved.");
                        }
                        break;
                    case 11:
                        Request toReject = pick("Select pending request", admin.viewPendingRequests());
                        if (toReject != null) {
                            admin.rejectRequest(toReject);
                            fileManager.saveAll();
                            System.out.println("Request rejected.");
                        }
                        break;
                    case 12:
                        for (Course c : admin.viewCourses()) printCourse(c);
                        break;
                    default:
                        back = true;
                }
            } catch (CampusException | RuntimeException e) {
                Logger.logError("adminMenu", e);
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    private static void printCourse(Course c) {
        System.out.println(c.getCourseCode() + " - " + c.getTitle() + " (" + c.getCreditHours() + " cr)");
        for (Section s : c.getSections()) {
            Instructor teacher = s.getInstructor();
            System.out.println("    " + describe(s) + " | Capacity " + s.getCapacity() + " | Instructor: "
                    + (teacher == null ? "(none)" : teacher.getName()));
        }
    }

    private static Schedule readSchedule() {
        Day day = pick("Select day", Arrays.asList(Day.values()));
        if (day == null) throw new IllegalArgumentException("A day is required");
        java.time.LocalTime start = java.time.LocalTime.parse(readLine("Start time (hh:mm): "));
        java.time.LocalTime end = java.time.LocalTime.parse(readLine("End time (hh:mm): "));
        if (!end.isAfter(start)) throw new IllegalArgumentException("End time must be after start time");
        String room = readLine("Room: ");
        return new Schedule(day, start, end, room);
    }

    private static List<Instructor> allInstructors() {
        List<Instructor> result = new ArrayList<>();
        result.addAll(permanentInstructors);
        result.addAll(visitingInstructors);
        return result;
    }

    private static void instructorMenu(Instructor instructor) {
        PermanentInstructor permanent = (instructor instanceof PermanentInstructor) ? (PermanentInstructor) instructor : null;
        int maxOption = (permanent == null) ? 6 : 17;
        boolean back = false;
        while (!back) {
            System.out.println("\n--- " + instructor.getRole() + ": " + instructor.getName() + " ---");
            System.out.println("1. View Assigned Courses");
            System.out.println("2. View Assigned Sections");
            System.out.println("3. View Enrolled Students");
            System.out.println("4. Mark Attendance");
            System.out.println("5. Update Attendance");
            System.out.println("6. Calculate Attendance Percentage");
            if (permanent != null) {
                System.out.println("7. Assign Teaching Assistant");
                System.out.println("8. View FYP Groups");
                System.out.println("9. View Group Details");
                System.out.println("10. View Group Members");
                System.out.println("11. Schedule Meeting");
                System.out.println("12. Evaluate FYP Idea");
                System.out.println("13. Provide FYP Feedback");
                System.out.println("14. Create FYP Group (I will supervise)");
                System.out.println("15. Add Member to Group");
                System.out.println("16. Remove Member from Group");
                System.out.println("17. Update Meeting Notes");
            }
            System.out.println("0. Back");
            int choice = readInt("Choose: ", 0, maxOption);
            try {
                if (choice > 6 && permanent == null) {
                    throw new UnauthorizedActionException("Only a permanent instructor can do this");
                }
                switch (choice) {
                    case 1:
                        if (instructor.viewCourses().isEmpty()) System.out.println("No assigned courses.");
                        for (Course c : instructor.viewCourses()) {
                            System.out.println(c.getCourseCode() + " - " + c.getTitle() + " (" + c.getCreditHours() + " cr)");
                        }
                        break;
                    case 2:
                        if (instructor.viewSections().isEmpty()) System.out.println("No assigned sections.");
                        for (Section sec : instructor.viewSections()) System.out.println(describe(sec));
                        break;
                    case 3:
                        Section forStudents = pick("Select section", instructor.viewSections());
                        if (forStudents != null) {
                            List<Student> enrolled = instructor.viewEnrolledStudents(forStudents);
                            if (enrolled.isEmpty()) System.out.println("No students enrolled.");
                            for (Student st : enrolled) System.out.println(st.getStudentId() + " - " + st.getName());
                        }
                        break;
                    case 4:
                        markAttendance(instructor);
                        break;
                    case 5:
                        updateAttendance(instructor);
                        break;
                    case 6:
                        Section forPercentage = pick("Select section", instructor.viewSections());
                        Student forStudent = (forPercentage == null) ? null
                                : pick("Select student", instructor.viewEnrolledStudents(forPercentage));
                        if (forStudent != null) {
                            System.out.println("Attendance: " + String.format("%.1f",
                                    instructor.calculateAttendancePercentage(forStudent, forPercentage)) + "%");
                        }
                        break;
                    case 7:
                        Section forTA = pick("Select section", permanent.viewSections());
                        Student taStudent = (forTA == null) ? null : pick("Select student to make TA", normalStudents());
                        if (taStudent != null) {
                            permanent.assignTA((NormalStudent) taStudent, forTA);
                            fileManager.saveAll();
                            System.out.println("Teaching assistant assigned.");
                        }
                        break;
                    default:
                        if (choice == 0) {
                            back = true;
                        } else {
                            fypOption(permanent, choice);
                        }
                }
            } catch (CampusException | RuntimeException e) {
                Logger.logError("instructorMenu", e);
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    private static List<Student> normalStudents() {
        List<Student> result = new ArrayList<>();
        for (Student st : students) {
            if (st instanceof NormalStudent) result.add(st);
        }
        return result;
    }

    private static LocalDate readDateOrToday() {
        String text = readLine("Date (yyyy-mm-dd, blank = today): ");
        return text.isEmpty() ? LocalDate.now() : LocalDate.parse(text);
    }

    private static void markAttendance(Instructor instructor) throws CampusException {
        Section section = pick("Select section", instructor.viewSections());
        if (section == null) return;
        LocalDate date = readDateOrToday();
        List<Student> enrolled = instructor.viewEnrolledStudents(section);
        if (enrolled.isEmpty()) System.out.println("No students enrolled.");
        for (Student st : enrolled) {
            if (instructor.findAttendance(st, section, date) != null) {
                System.out.println(st.getName() + ": already marked for " + date + " (use Update Attendance)");
                continue;
            }
            int status = readInt(st.getName() + " (1 = Present, 2 = Absent, 3 = Late, 0 = skip): ", 0, 3);
            if (status == 0) continue;
            AttendanceStatus value = AttendanceStatus.values()[status - 1];
            instructor.markAttendance(new Attendance(st, section, date, value), value);
        }
        fileManager.saveAll();
    }

    private static void updateAttendance(Instructor instructor) throws CampusException {
        Section section = pick("Select section", instructor.viewSections());
        Student student = (section == null) ? null : pick("Select student", instructor.viewEnrolledStudents(section));
        if (student == null) return;
        LocalDate date = readDateOrToday();
        Attendance record = instructor.findAttendance(student, section, date);
        if (record == null) {
            System.out.println("No attendance was marked for that student on " + date + ".");
            return;
        }
        AttendanceStatus status = pick("New status (now " + record.getStatus() + ")", Arrays.asList(AttendanceStatus.values()));
        if (status == null) return;
        instructor.updateAttendance(record, status);
        fileManager.saveAll();
        System.out.println("Attendance updated.");
    }

    private static void fypOption(PermanentInstructor instructor, int choice) throws CampusException {
        switch (choice) {
            case 8:
                List<FYPGroup> groups = instructor.viewFYPGroups();
                if (groups.isEmpty()) System.out.println("No FYP groups.");
                for (FYPGroup g : groups) System.out.println(g.getGroupId() + " - " + g.getTitle());
                break;
            case 9:
                FYPGroup g2 = pick("Select group", instructor.viewFYPGroups());
                if (g2 != null) System.out.println(instructor.viewFYPGroupDetails(g2));
                break;
            case 10:
                FYPGroup g3 = pick("Select group", instructor.viewFYPGroups());
                if (g3 != null) {
                    for (Student st : instructor.viewFYPGroupMembers(g3)) {
                        System.out.println(st.getStudentId() + " - " + st.getName());
                    }
                }
                break;
            case 11:
                FYPGroup g4 = pick("Select group", instructor.viewFYPGroups());
                if (g4 != null) {
                    LocalDate date = LocalDate.parse(readLine("Meeting date (yyyy-mm-dd): "));
                    String agenda = readLine("Agenda: ");
                    instructor.scheduleFYPMeeting(g4, new FYPMeeting(IdGenerator.nextId("M"), date, agenda));
                    fileManager.saveAll();
                }
                break;
            case 12:
                FYPGroup g5 = pick("Select group", instructor.viewFYPGroups());
                if (g5 != null) {
                    double score = Double.parseDouble(readLine("Score (non-negative): "));
                    FYPEvaluation evaluation = new FYPEvaluation(IdGenerator.nextId("V"), LocalDate.now(), instructor);
                    evaluation.evaluate(score);
                    instructor.evaluateFYPIdea(g5, evaluation);
                    fileManager.saveAll();
                }
                break;
            case 13:
                FYPGroup g6 = pick("Select group", instructor.viewFYPGroups());
                if (g6 != null) {
                    FYPEvaluation ev = pick("Select evaluation", g6.getEvaluations());
                    if (ev != null) {
                        instructor.provideFYPFeedback(ev, readLine("Feedback: "));
                        fileManager.saveAll();
                    }
                }
                break;
            case 14:
                String title = readLine("Group title: ");
                String description = readLine("Group description: ");
                FYPGroup group = new FYPGroup(IdGenerator.nextId("G"), title, description);
                group.assignSupervisor(instructor);
                fypGroups.add(group);
                fileManager.saveAll();
                System.out.println("Created group " + group.getGroupId());
                break;
            case 15:
                FYPGroup g8 = pick("Select group", instructor.viewFYPGroups());
                if (g8 != null) {
                    Student toAdd = pick("Select student", normalStudents());
                    if (toAdd != null) {
                        checkNotInAnotherGroup(toAdd, g8);
                        g8.addMember(toAdd);
                        fileManager.saveAll();
                    }
                }
                break;
            case 16:
                FYPGroup g9 = pick("Select group", instructor.viewFYPGroups());
                if (g9 != null) {
                    Student toRemove = pick("Select member", instructor.viewFYPGroupMembers(g9));
                    if (toRemove != null) {
                        g9.removeMember(toRemove);
                        fileManager.saveAll();
                    }
                }
                break;
            case 17:
                FYPGroup g10 = pick("Select group", instructor.viewFYPGroups());
                if (g10 != null) {
                    FYPMeeting meeting = pick("Select meeting", g10.getMeetings());
                    if (meeting != null) {
                        meeting.updateNotes(readLine("Notes: "));
                        fileManager.saveAll();
                    }
                }
                break;
            default:
                break;
        }
    }

    private static void checkNotInAnotherGroup(Student student, FYPGroup target) throws InvalidFYPGroupException {
        for (FYPGroup g : fypGroups) {
            if (g != target && g.hasMember(student)) {
                throw new InvalidFYPGroupException("Student " + student.getStudentId()
                        + " is already in group " + g.getGroupId());
            }
        }
    }

    private static String describe(Section s) {
        return "Section " + s.getSectionId() + " | " + s.getCourse().getCourseCode() + " " + s.getCourse().getTitle()
                + " | " + s.getSchedule().getScheduleInfo();
    }

    private static String describe(Submission s) {
        String text = s.getSubmissionId() + " | " + s.getStudent().getName() + " | " + s.getSubmissionDate()
                + " | " + s.getStatus() + (s.isLate() ? " (LATE)" : "") + " | Marks " + s.getMarks()
                + " | Content: " + s.getContent();
        if (s.getFeedback() != null) {
            text += " | Feedback by " + s.getFeedback().getEvaluatorName() + ": " + s.getFeedback().getComments();
        }
        return text;
    }

    private static String label(Object item) {
        if (item instanceof Section) return describe((Section) item);
        if (item instanceof Course) return ((Course) item).getCourseCode() + " - " + ((Course) item).getTitle()
                + " (" + ((Course) item).getCreditHours() + " cr)";
        if (item instanceof Student) return ((Student) item).getStudentId() + " - " + ((Student) item).getName();
        if (item instanceof PermanentInstructor) return ((PermanentInstructor) item).getTeacherId() + " - " + ((PermanentInstructor) item).getName();
        if (item instanceof VisitingInstructor) return ((VisitingInstructor) item).getTeacherId() + " - " + ((VisitingInstructor) item).getName();
        if (item instanceof AcademicOfficeAdmin) return ((AcademicOfficeAdmin) item).getAdminId() + " - " + ((AcademicOfficeAdmin) item).getName();
        if (item instanceof Assignment) return ((Assignment) item).getDetails();
        if (item instanceof Submission) return describe((Submission) item);
        if (item instanceof Request) return ((Request) item).getDetails();
        if (item instanceof FYPGroup) return ((FYPGroup) item).getGroupId() + " - " + ((FYPGroup) item).getTitle();
        if (item instanceof FYPMeeting) return ((FYPMeeting) item).getMeetingDetails();
        if (item instanceof FYPEvaluation) {
            FYPEvaluation e = (FYPEvaluation) item;
            return e.getEvaluationId() + " | " + e.getEvaluationDate() + " | Score " + e.getScore();
        }
        return String.valueOf(item);
    }

    private static <T> T pick(String prompt, List<T> items) {
        if (items.isEmpty()) {
            System.out.println("Nothing available to select.");
            return null;
        }
        System.out.println(prompt + ":");
        for (int i = 0; i < items.size(); i++) {
            System.out.println("  " + (i + 1) + ". " + label(items.get(i)));
        }
        int choice = readInt("Number (0 to cancel): ", 0, items.size());
        if (choice == 0) return null;
        return items.get(choice - 1);
    }

    private static String readLine(String prompt) {
        System.out.print(prompt);
        if (!in.hasNextLine()) {
            if (fileManager != null) fileManager.saveAll();
            System.out.println("\nInput ended. Goodbye.");
            System.exit(0);
        }
        return in.nextLine().trim();
    }

    private static int readInt(String prompt, int min, int max) {
        while (true) {
            String text = readLine(prompt);
            try {
                int value = Integer.parseInt(text);
                if (value >= min && value <= max) return value;
            } catch (NumberFormatException e) {
            }
            System.out.println("Please enter a number between " + min + " and " + max + ".");
        }
    }
}
