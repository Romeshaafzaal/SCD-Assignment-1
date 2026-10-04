import java.time.LocalDate;

public class Enrollment {
    private String enrollmentId;
    private Student student;
    private Section section;
    private LocalDate enrollmentDate;
    private EnrollmentStatus status;

    public Enrollment(String enrollmentId, Student student, Section section, LocalDate enrollmentDate, EnrollmentStatus status) {
        this.enrollmentId = enrollmentId;
        this.student = student;
        this.section = section;
        this.enrollmentDate = enrollmentDate;
        this.status = status;
    }

    public String getEnrollmentId() {
        return enrollmentId;
    }

    public Student getStudent() {
        return student;
    }

    public Section getSection() {
        return section;
    }

    public LocalDate getEnrollmentDate() {
        return enrollmentDate;
    }

    public EnrollmentStatus getStatus() {
        return status;
    }

    public void cancel() {
        this.status = EnrollmentStatus.DROPPED;
    }
}