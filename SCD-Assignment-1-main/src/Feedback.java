import java.time.LocalDate;

public class Feedback {
    private String feedbackId;
    private Evaluator evaluator;
    private String comments;
    private LocalDate date;

    public Feedback(String feedbackId, Evaluator evaluator, String comments, LocalDate date) {
        if (feedbackId == null || feedbackId.isBlank()) {
            throw new IllegalArgumentException("Feedback ID cannot be empty");
        }
        if (evaluator == null) {
            throw new IllegalArgumentException("Feedback requires an evaluator");
        }
        if (comments == null || comments.isBlank()) {
            throw new IllegalArgumentException("Feedback comments cannot be empty");
        }
        if (date == null) {
            throw new IllegalArgumentException("Feedback date is required");
        }
        this.feedbackId = feedbackId;
        this.evaluator = evaluator;
        this.comments = comments;
        this.date = date;
    }

    public String getFeedbackId() {
        return feedbackId;
    }

    public Evaluator getEvaluator() {
        return evaluator;
    }

    public String getEvaluatorName() {
        if (evaluator instanceof Person) {
            return ((Person) evaluator).getName();
        }
        return "Unknown";
    }

    public String getComments() {
        return comments;
    }

    public LocalDate getDate() {
        return date;
    }
}
