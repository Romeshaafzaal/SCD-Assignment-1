import java.time.LocalDate;

public class Attendance {
    private Student student;
    private Section section;
    private LocalDate date;
    private AttendanceStatus status;

    public Attendance(Student student, Section section, LocalDate date, AttendanceStatus status) {
        this.student = student;
        this.section = section;
        this.date = date;
        this.status = status;
    }

    public Student getStudent() {
        return student;
    }

    public Section getSection() {
        return section;
    }

    public LocalDate getDate() {
        return date;
    }

    public AttendanceStatus getStatus() {
        return status;
    }

    public void setStatus(AttendanceStatus status) {
        this.status = status;
    }
}