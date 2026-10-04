import java.time.LocalDate;

public abstract class Assessment {
    protected String id;
    protected String title;
    protected String description;
    protected LocalDate deadline;
    protected double totalMarks;

    public Assessment(String id, String title, String description, LocalDate deadline, double totalMarks) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Assessment ID cannot be empty");
        }
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Title cannot be empty");
        }
        if (deadline == null) {
            throw new IllegalArgumentException("Deadline is required");
        }
        if (Double.isNaN(totalMarks) || totalMarks <= 0) {
            throw new IllegalArgumentException("Total marks must be positive");
        }
        this.id = id;
        this.title = title;
        this.description = (description == null) ? "" : description;
        this.deadline = deadline;
        this.totalMarks = totalMarks;
    }

    public static void validate(String title, LocalDate deadline, double totalMarks) throws SubmissionDeadlineException {
        if (title == null || title.isBlank()) {
            throw new SubmissionDeadlineException("Title cannot be empty");
        }
        if (deadline == null) {
            throw new SubmissionDeadlineException("Deadline is required");
        }
        if (Double.isNaN(totalMarks) || totalMarks <= 0) {
            throw new SubmissionDeadlineException("Total marks must be positive");
        }
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public LocalDate getDeadline() {
        return deadline;
    }

    public void setDeadline(LocalDate deadline) {
        this.deadline = deadline;
    }

    public double getTotalMarks() {
        return totalMarks;
    }

    public void setTotalMarks(double totalMarks) {
        this.totalMarks = totalMarks;
    }
}
