import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public abstract class Student extends Person {
    protected String studentId;
    protected int totalCreditHours;
    protected List<Enrollment> enrollments;
    protected List<Request> requests;

    public Student(String studentId, String name, String email, String phone) {
        super(name, email, phone);
        this.studentId = studentId;
        this.totalCreditHours = 0;
        this.enrollments = new ArrayList<>();
        this.requests = new ArrayList<>();
    }

    public String getStudentId() {
        return studentId;
    }

    public List<Enrollment> getEnrollments() {
        return enrollments;
    }

    public void register(Section section) throws CourseException {
        for (Enrollment e : enrollments) {
            if (e.getStatus() == EnrollmentStatus.ACTIVE && e.getSection() == section) {
                throw new CourseClashException("You are already registered in section " + section.getSectionId());
            }
        }
        if (section.isFull()) {
            throw new CourseFullException("Section " + section.getSectionId() + " is full");
        }
        for (Enrollment e : enrollments) {
            if (e.getStatus() == EnrollmentStatus.ACTIVE
                    && e.getSection().hasClash(section)) {
                throw new CourseClashException("Section clashes with existing timetable");
            }
        }

        section.enroll(this);
        Enrollment enrollment = new Enrollment(
                IdGenerator.nextId("E"),
                this,
                section,
                LocalDate.now(),
                EnrollmentStatus.ACTIVE
        );
        enrollments.add(enrollment);
        calculateTotalCreditHours();
        Logger.log("Student " + studentId + " registered for section " + section.getSectionId());
    }

    public void drop(Section section) throws CourseException {
        boolean found = false;
        for (Enrollment e : enrollments) {
            if (e.getSection().equals(section) && e.getStatus() == EnrollmentStatus.ACTIVE) {
                found = true;
            }
        }
        if (!found) {
            throw new CourseClashException("You are not registered in section " + section.getSectionId());
        }
        section.drop(this);
        for (Enrollment e : enrollments) {
            if (e.getSection().equals(section) && e.getStatus() == EnrollmentStatus.ACTIVE) {
                e.cancel();
                break;
            }
        }
        calculateTotalCreditHours();
        Logger.log("Student " + studentId + " dropped section " + section.getSectionId());
    }

    public void restoreEnrollment(Enrollment enrollment) {
        enrollments.add(enrollment);
        calculateTotalCreditHours();
    }

    public int calculateTotalCreditHours() {
        int total = 0;
        for (Enrollment e : enrollments) {
            if (e.getStatus() == EnrollmentStatus.ACTIVE) {
                total += e.getSection().getCourse().getCreditHours();
            }
        }
        this.totalCreditHours = total;
        return total;
    }

    public List<Course> viewCourses() {
        List<Course> courses = new ArrayList<>();
        for (Enrollment e : enrollments) {
            if (e.getStatus() == EnrollmentStatus.ACTIVE) {
                Course course = e.getSection().getCourse();
                if (!courses.contains(course)) {
                    courses.add(course);
                }
            }
        }
        return courses;
    }

    public List<Schedule> viewTimetable() {
        List<Schedule> timetable = new ArrayList<>();
        for (Enrollment e : enrollments) {
            if (e.getStatus() == EnrollmentStatus.ACTIVE) {
                timetable.add(e.getSection().getSchedule());
            }
        }
        return timetable;
    }

    public List<Attendance> viewAttendance(Section section) {
        Instructor instructor = section.getInstructor();
        if (instructor == null) {
            return new ArrayList<>();
        }
        return instructor.getAttendanceFor(this, section);
    }

    public double viewAttendancePercentage(Section section) {
        Instructor instructor = section.getInstructor();
        if (instructor == null) {
            return 0.0;
        }
        return instructor.calculateAttendancePercentage(this, section);
    }

    public Submission submitAssignment(Assignment assignment, String content)
            throws AssessmentException, UnauthorizedActionException {
        return submitAssignment(assignment, content, LocalDate.now());
    }

    public Submission submitAssignment(Assignment assignment, String content, LocalDate date)
            throws AssessmentException, UnauthorizedActionException {
        if (assignment == null) {
            throw new SubmissionDeadlineException("Assignment not found");
        }
        if (!isEnrolledIn(assignment.getSection())) {
            throw new UnauthorizedActionException("Student " + studentId + " is not enrolled in section "
                    + assignment.getSection().getSectionId());
        }
        if (assignment.getSubmissionBy(this) != null) {
            throw new SubmissionDeadlineException("You have already submitted assignment " + assignment.getId());
        }
        Submission submission = new Submission(
                IdGenerator.nextId("S"),
                assignment,
                this,
                date,
                content,
                0
        );
        submission.submit();
        assignment.addSubmission(submission);
        Logger.log("Student " + studentId + " submitted assignment " + assignment.getId());
        return submission;
    }

    public List<Assignment> viewAssignments() {
        List<Assignment> assignments = new ArrayList<>();
        for (Enrollment e : enrollments) {
            if (e.getStatus() == EnrollmentStatus.ACTIVE) {
                assignments.addAll(e.getSection().getAssignments());
            }
        }
        return assignments;
    }

    private boolean isEnrolledIn(Section section) {
        for (Student s : section.getEnrolledStudents()) {
            if (s.getStudentId().equals(studentId)) {
                return true;
            }
        }
        return false;
    }

    public void submitCourseClashRequest(CourseClashRequest request) throws InvalidRequestException {
        checkRequestBelongsToMe(request);
        if (!isEnrolledIn(request.getConflictingSection())) {
            throw new InvalidRequestException("You are not enrolled in section "
                    + request.getConflictingSection().getSectionId());
        }
        if (isEnrolledIn(request.getRequestedSection())) {
            throw new InvalidRequestException("You are already enrolled in section "
                    + request.getRequestedSection().getSectionId());
        }
        for (Request r : requests) {
            if (r instanceof CourseClashRequest && r.getStatus() == RequestStatus.PENDING) {
                CourseClashRequest other = (CourseClashRequest) r;
                if (other.getConflictingSection() == request.getConflictingSection()
                        && other.getRequestedSection() == request.getRequestedSection()) {
                    throw new InvalidRequestException("A pending request for these sections already exists");
                }
            }
        }
        request.submit();
        addRequest(request);
    }

    public void submitGenericRequest(GenericRequest request) throws InvalidRequestException {
        checkRequestBelongsToMe(request);
        request.submit();
        addRequest(request);
    }

    public void addRequest(Request request) throws InvalidRequestException {
        for (Request r : requests) {
            if (r.getRequestId().equals(request.getRequestId())) {
                throw new InvalidRequestException("Request " + request.getRequestId() + " already exists");
            }
        }
        requests.add(request);
    }

    public List<Request> viewRequests() {
        return new ArrayList<>(requests);
    }

    public List<Request> viewRequests(Comparator<Request> comparator) {
        List<Request> sorted = new ArrayList<>(requests);
        Collections.sort(sorted, comparator);
        return sorted;
    }

    public RequestStatus viewRequestStatus(String requestId) throws InvalidRequestException {
        for (Request r : requests) {
            if (r.getRequestId().equals(requestId)) {
                return r.getStatus();
            }
        }
        throw new InvalidRequestException("No request with ID " + requestId + " found for you");
    }

    private void checkRequestBelongsToMe(Request request) throws InvalidRequestException {
        if (request == null) {
            throw new InvalidRequestException("Request cannot be null");
        }
        if (!request.getStudent().getStudentId().equals(studentId)) {
            throw new InvalidRequestException("This request belongs to another student");
        }
    }

    @Override
    public abstract String getRole();
}