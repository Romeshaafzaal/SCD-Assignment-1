import java.util.ArrayList;
import java.util.List;

public abstract class Instructor extends Person {
    protected String teacherId;
    protected List<Section> assignedSections;
    protected List<Attendance> attendanceRecords;

    public Instructor(String teacherId, String name, String email, String phone) {
        super(name, email, phone);
        this.teacherId = teacherId;
        this.assignedSections = new ArrayList<>();
        this.attendanceRecords = new ArrayList<>();
    }

    public String getTeacherId() {
        return teacherId;
    }

    public List<Course> viewCourses() {
        List<Course> courses = new ArrayList<>();
        for (Section section : assignedSections) {
            Course course = section.getCourse();
            if (!courses.contains(course)) {
                courses.add(course);
            }
        }
        return courses;
    }

    public void addAssignedSection(Section section) {
        if (section != null && !assignedSections.contains(section)) {
            assignedSections.add(section);
        }
    }

    public List<Section> viewSections() {
        return assignedSections;
    }

    public List<Student> viewEnrolledStudents(Section section) {
        return section.getEnrolledStudents();
    }

    public void removeAssignedSection(Section section) {
        assignedSections.remove(section);
    }

    public List<Attendance> getAttendanceRecords() {
        return new ArrayList<>(attendanceRecords);
    }

    public void addAttendanceRecord(Attendance attendance) {
        attendanceRecords.add(attendance);
    }

    public List<Attendance> getAttendanceFor(Student student, Section section) {
        List<Attendance> result = new ArrayList<>();
        for (Attendance a : attendanceRecords) {
            if (a.getStudent().getStudentId().equals(student.getStudentId()) && a.getSection() == section) {
                result.add(a);
            }
        }
        return result;
    }

    public Attendance findAttendance(Student student, Section section, java.time.LocalDate date) {
        for (Attendance a : getAttendanceFor(student, section)) {
            if (a.getDate().equals(date)) {
                return a;
            }
        }
        return null;
    }

    private void checkCanManageAttendance(Attendance attendance) throws UnauthorizedActionException {
        Section section = attendance.getSection();
        if (!assignedSections.contains(section)) {
            throw new UnauthorizedActionException("You are not assigned to section " + section.getSectionId());
        }
        boolean enrolled = false;
        for (Student s : section.getEnrolledStudents()) {
            if (s.getStudentId().equals(attendance.getStudent().getStudentId())) {
                enrolled = true;
            }
        }
        if (!enrolled) {
            throw new UnauthorizedActionException("Student " + attendance.getStudent().getStudentId()
                    + " is not enrolled in section " + section.getSectionId());
        }
    }

    public void markAttendance(Attendance attendance, AttendanceStatus status)
            throws UnauthorizedActionException, CourseClashException {
        checkCanManageAttendance(attendance);
        if (findAttendance(attendance.getStudent(), attendance.getSection(), attendance.getDate()) != null) {
            throw new CourseClashException("Attendance for " + attendance.getDate()
                    + " is already marked; use update instead");
        }
        attendance.setStatus(status);
        attendanceRecords.add(attendance);
        Logger.log("Attendance marked for student " + attendance.getStudent().getStudentId()
                + " in section " + attendance.getSection().getSectionId() + " as " + status);
    }

    public void updateAttendance(Attendance attendance, AttendanceStatus status) throws UnauthorizedActionException {
        checkCanManageAttendance(attendance);
        attendance.setStatus(status);
        Logger.log("Attendance updated for student " + attendance.getStudent().getStudentId()
                + " in section " + attendance.getSection().getSectionId() + " to " + status);
    }

    public double calculateAttendancePercentage(Student student, Section section) {
        int total = 0;
        int present = 0;
        for (Attendance a : attendanceRecords) {
            if (a.getStudent().equals(student) && a.getSection().equals(section)) {
                total++;
                if (a.getStatus() == AttendanceStatus.PRESENT) {
                    present++;
                }
            }
        }
        if (total == 0) {
            return 0.0;
        }
        return (present * 100.0) / total;
    }

    @Override
    public abstract String getRole();
}