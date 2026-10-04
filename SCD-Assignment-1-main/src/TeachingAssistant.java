import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class TeachingAssistant extends Student implements Evaluator {
    private Section assignedSection;

    public TeachingAssistant(String studentId, Section assignedSection, String name, String email, String phone) {
        super(studentId, name, email, phone);
        this.assignedSection = assignedSection;
    }

    public Section getAssignedSection() {
        return assignedSection;
    }

    public List<Student> viewEnrolledStudents() {
        return assignedSection.getEnrolledStudents();
    }

    public List<Assignment> viewAssignments() {
        return assignedSection.getAssignments();
    }

    public Assignment createAssignment(String title, String description, LocalDate deadline, double totalMarks)
            throws AssessmentException, UnauthorizedActionException {
        checkIsTAOfAssignedSection();
        Assessment.validate(title, deadline, totalMarks);
        if (deadline.isBefore(LocalDate.now())) {
            throw new SubmissionDeadlineException("Deadline cannot be in the past");
        }
        Assignment assignment = new Assignment(
                IdGenerator.nextId("A"),
                title,
                description,
                deadline,
                totalMarks,
                assignedSection,
                this
        );
        assignedSection.addAssignment(assignment);
        Logger.log("Assignment '" + title + "' created by TA " + getStudentId() + " for section " + assignedSection.getSectionId());
        return assignment;
    }

    public void setAssignmentDeadline(Assignment assignment, LocalDate deadline)
            throws AssessmentException, UnauthorizedActionException {
        checkOwnsAssignment(assignment);
        if (deadline != null && deadline.isBefore(LocalDate.now())) {
            throw new SubmissionDeadlineException("Deadline cannot be in the past");
        }
        assignment.setDeadline(deadline);
        Logger.log("Deadline of assignment " + assignment.getId() + " set to " + deadline + " by TA " + getStudentId());
    }

    public void setAssignmentTotalMarks(Assignment assignment, double totalMarks)
            throws AssessmentException, UnauthorizedActionException {
        checkOwnsAssignment(assignment);
        for (Submission s : assignment.getSubmissions()) {
            if (s.getStatus() == SubmissionStatus.EVALUATED && s.getMarks() > totalMarks) {
                throw new SubmissionDeadlineException("Total marks cannot be lower than marks already awarded ("
                        + s.getMarks() + ")");
            }
        }
        assignment.setTotalMarks(totalMarks);
        Logger.log("Total marks of assignment " + assignment.getId() + " set to " + totalMarks + " by TA " + getStudentId());
    }

    public List<Submission> viewSubmissions(Assignment assignment) throws UnauthorizedActionException {
        checkOwnsAssignment(assignment);
        return assignment.getSubmissions();
    }

    public List<Submission> checkLateSubmissions(Assignment assignment) throws UnauthorizedActionException {
        checkOwnsAssignment(assignment);
        List<Submission> late = new ArrayList<>(assignment.getLateSubmissions());
        Logger.log("TA " + getStudentId() + " checked late submissions of assignment " + assignment.getId()
                + ": " + late.size() + " found");
        return late;
    }

    public void evaluateSubmission(Submission submission, double marks)
            throws AssessmentException, UnauthorizedActionException {
        checkOwnsSubmission(submission);
        submission.assignMarks(marks);
        Logger.log("Submission " + submission.getSubmissionId() + " evaluated with marks " + marks + " by TA " + getStudentId());
    }

    public void giveFeedback(Submission submission, String comments)
            throws AssessmentException, UnauthorizedActionException {
        checkOwnsSubmission(submission);
        Feedback feedback = new Feedback(IdGenerator.nextId("F"), this, comments, LocalDate.now());
        submission.addFeedback(feedback);
        Logger.log("Feedback given on submission " + submission.getSubmissionId() + " by TA " + getStudentId());
    }

    @Override
    public void evaluate() {
        Logger.log("Generic evaluate() called on Teaching Assistant " + getStudentId());
    }

    private void checkIsTAOfAssignedSection() throws UnauthorizedActionException {
        if (assignedSection == null) {
            throw new UnauthorizedActionException("You are not assigned to any section");
        }
        TeachingAssistant current = assignedSection.getTeachingAssistant();
        if (current == null || !current.getStudentId().equals(getStudentId())) {
            throw new UnauthorizedActionException("You are not the TA of section " + assignedSection.getSectionId());
        }
    }

    private void checkOwnsAssignment(Assignment assignment) throws UnauthorizedActionException {
        checkIsTAOfAssignedSection();
        if (assignment == null || !assignment.getSection().getSectionId().equals(assignedSection.getSectionId())) {
            throw new UnauthorizedActionException("Assignment does not belong to your section "
                    + assignedSection.getSectionId());
        }
    }

    private void checkOwnsSubmission(Submission submission) throws UnauthorizedActionException {
        if (submission == null) {
            throw new UnauthorizedActionException("Submission not found");
        }
        checkOwnsAssignment(submission.getAssignment());
    }

    @Override
    public String getRole() {
        return "Teaching Assistant";
    }
}
