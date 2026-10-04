import java.time.LocalDate;

public class Submission {
    private String submissionId;
    private Assignment assignment;
    private Student student;
    private LocalDate submissionDate;
    private String content;
    private double marks;
    private Feedback feedback;
    private SubmissionStatus status;

    public Submission(String submissionId, Assignment assignment, Student student, LocalDate submissionDate, String content, double marks) {
        this(submissionId, assignment, student, submissionDate, content, marks, null, SubmissionStatus.PENDING);
    }

    public Submission(String submissionId, Assignment assignment, Student student, LocalDate submissionDate,
                      String content, double marks, Feedback feedback, SubmissionStatus status) {
        if (assignment == null || student == null || submissionDate == null) {
            throw new IllegalArgumentException("Submission needs an assignment, a student and a date");
        }
        this.submissionId = submissionId;
        this.assignment = assignment;
        this.student = student;
        this.submissionDate = submissionDate;
        this.content = content;
        this.marks = marks;
        this.feedback = feedback;
        this.status = (status == null) ? SubmissionStatus.PENDING : status;
    }

    public String getSubmissionId() {
        return submissionId;
    }

    public Assignment getAssignment() {
        return assignment;
    }

    public Student getStudent() {
        return student;
    }

    public LocalDate getSubmissionDate() {
        return submissionDate;
    }

    public String getContent() {
        return content;
    }

    public void submit() throws AssessmentException {
        if (status != SubmissionStatus.PENDING) {
            throw new SubmissionDeadlineException("Submission " + submissionId + " has already been submitted");
        }
        if (content == null || content.isBlank()) {
            throw new SubmissionDeadlineException("Submission content cannot be empty");
        }
        if (isLate()) {
            this.status = SubmissionStatus.LATE;
            Logger.log("LATE submission " + submissionId + " by student " + student.getStudentId()
                    + " for assignment " + assignment.getId() + " (deadline " + assignment.getDeadline() + ")");
        } else {
            this.status = SubmissionStatus.SUBMITTED;
        }
    }

    public boolean isLate() {
        return submissionDate.isAfter(assignment.getDeadline());
    }

    public void assignMarks(double marks) throws AssessmentException {
        if (status == SubmissionStatus.PENDING) {
            throw new SubmissionDeadlineException("Cannot evaluate a submission that has not been submitted");
        }
        if (Double.isNaN(marks) || marks < 0) {
            throw new SubmissionDeadlineException("Marks cannot be negative");
        }
        if (marks > assignment.getTotalMarks()) {
            throw new SubmissionDeadlineException("Marks (" + marks + ") cannot exceed total marks ("
                    + assignment.getTotalMarks() + ")");
        }
        this.marks = marks;
        this.status = SubmissionStatus.EVALUATED;
        Logger.log("Marks " + marks + "/" + assignment.getTotalMarks() + " assigned to submission " + submissionId);
    }

    public double getMarks() {
        return marks;
    }

    public void addFeedback(Feedback feedback) throws AssessmentException {
        if (feedback == null) {
            throw new SubmissionDeadlineException("Feedback cannot be null");
        }
        if (status == SubmissionStatus.PENDING) {
            throw new SubmissionDeadlineException("Cannot add feedback to a submission that has not been submitted");
        }
        this.feedback = feedback;
        Logger.log("Feedback " + feedback.getFeedbackId() + " added to submission " + submissionId);
    }

    public Feedback getFeedback() {
        return feedback;
    }

    public SubmissionStatus getStatus() {
        return status;
    }
}
