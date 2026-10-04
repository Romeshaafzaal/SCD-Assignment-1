import java.time.LocalDate;

public class FYPEvaluation {
    private String evaluationId;
    private LocalDate evaluationDate;
    private Evaluator evaluator;
    private double score;
    private String feedback;
    private boolean evaluated;

    public FYPEvaluation(String evaluationId, LocalDate evaluationDate, Evaluator evaluator) throws InvalidFYPEvaluationException {
        if (evaluationId == null || evaluationId.isBlank()) {
            throw new InvalidFYPEvaluationException("Evaluation ID cannot be empty");
        }
        if (evaluationDate == null) {
            throw new InvalidFYPEvaluationException("Evaluation date is required");
        }
        if (evaluator == null) {
            throw new InvalidFYPEvaluationException("Evaluation must have an evaluator");
        }
        this.evaluationId = evaluationId;
        this.evaluationDate = evaluationDate;
        this.evaluator = evaluator;
        this.score = 0;
        this.feedback = "";
        this.evaluated = false;
    }

    public void evaluate(double score) throws InvalidFYPEvaluationException {
        if (Double.isNaN(score) || score < 0) {
            throw new InvalidFYPEvaluationException("Score cannot be negative");
        }
        this.score = score;
        this.evaluated = true;
        Logger.log("FYP evaluation " + evaluationId + " scored " + score);
    }

    public void addFeedback(String feedback) throws InvalidFYPEvaluationException {
        if (feedback == null || feedback.isBlank()) {
            throw new InvalidFYPEvaluationException("Feedback cannot be empty");
        }
        this.feedback = feedback;
        Logger.log("Feedback added to FYP evaluation " + evaluationId);
    }

    public String getEvaluationId() {
        return evaluationId;
    }

    public LocalDate getEvaluationDate() {
        return evaluationDate;
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

    public boolean isEvaluated() {
        return evaluated;
    }

    public double getScore() {
        return score;
    }

    public String getFeedback() {
        return feedback;
    }
}
