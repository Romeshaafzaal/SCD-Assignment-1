import java.util.ArrayList;
import java.util.List;

public class Section {
    private String sectionId;
    private int capacity;
    private Course course;
    private Instructor instructor;
    private TeachingAssistant teachingAssistant;
    private Schedule schedule;
    private List<Enrollment> enrollments;
    private List<Assignment> assignments;

    public Section(String sectionId, int capacity, Course course, Schedule schedule) {
        this.sectionId = sectionId;
        this.capacity = capacity;
        this.course = course;
        this.schedule = schedule;
        this.enrollments = new ArrayList<>();
        this.assignments = new ArrayList<>();
    }

    public String getSectionId() {
        return sectionId;
    }

    public Course getCourse() {
        return course;
    }

    public Schedule getSchedule() {
        return schedule;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) throws CourseClashException {
        if (capacity <= 0) {
            throw new CourseClashException("Capacity must be greater than zero");
        }
        if (capacity < getActiveEnrollmentCount()) {
            throw new CourseClashException("Capacity cannot be lower than the " + getActiveEnrollmentCount()
                    + " students already enrolled");
        }
        this.capacity = capacity;
        Logger.log("Capacity of section " + sectionId + " set to " + capacity);
    }

    public void setSchedule(Schedule schedule) {
        this.schedule = schedule;
    }

    public void restoreEnrollment(Enrollment enrollment) {
        enrollments.add(enrollment);
    }

    public Instructor getInstructor() {
        return instructor;
    }

    public TeachingAssistant getTeachingAssistant() {
        return teachingAssistant;
    }

    public void enroll(Student student) throws CourseException {
        if (isFull()) {
            throw new CourseFullException("Section " + sectionId + " is full");
        }
        Enrollment enrollment = new Enrollment(
                IdGenerator.nextId("E"),
                student,
                this,
                java.time.LocalDate.now(),
                EnrollmentStatus.ACTIVE
        );
        enrollments.add(enrollment);
        Logger.log("Student " + student.getStudentId() + " enrolled in section " + sectionId);
    }

    public void drop(Student student) {
        for (Enrollment e : enrollments) {
            if (e.getStudent().equals(student) && e.getStatus() == EnrollmentStatus.ACTIVE) {
                e.cancel();
                Logger.log("Student " + student.getStudentId() + " dropped from section " + sectionId);
                break;
            }
        }
    }

    public boolean isFull() {
        return getActiveEnrollmentCount() >= capacity;
    }

    public int getAvailableSeats() {
        return capacity - getActiveEnrollmentCount();
    }

    private int getActiveEnrollmentCount() {
        int count = 0;
        for (Enrollment e : enrollments) {
            if (e.getStatus() == EnrollmentStatus.ACTIVE) {
                count++;
            }
        }
        return count;
    }

    public void assignInstructor(Instructor instructor) {
        this.instructor = instructor;
        Logger.log("Instructor " + instructor.getTeacherId() + " assigned to section " + sectionId);
    }

    public void assignTA(TeachingAssistant ta) {
        this.teachingAssistant = ta;
        Logger.log("TA assigned to section " + sectionId);
    }

    public List<Student> getEnrolledStudents() {
        List<Student> students = new ArrayList<>();
        for (Enrollment e : enrollments) {
            if (e.getStatus() == EnrollmentStatus.ACTIVE) {
                students.add(e.getStudent());
            }
        }
        return students;
    }

    public boolean hasClash(Section other) {
        return this.schedule.hasClash(other.getSchedule());
    }

    public void addAssignment(Assignment assignment) {
        assignments.add(assignment);
    }

    public List<Assignment> getAssignments() {
        return assignments;
    }
}