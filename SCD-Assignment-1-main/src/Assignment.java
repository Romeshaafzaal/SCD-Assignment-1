import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Assignment extends Assessment {
    private Section section;
    private TeachingAssistant createdBy;
    private List<Submission> submissions;

    public Assignment(String id, String title, String description, LocalDate deadline, double totalMarks, Section section, TeachingAssistant createdBy) {
        super(id, title, description, deadline, totalMarks);
        if (section == null) {
            throw new IllegalArgumentException("Assignment must belong to a section");
        }
        if (createdBy == null) {
            throw new IllegalArgumentException("Assignment must be created by a teaching assistant");
        }
        if (createdBy.getAssignedSection() == null
                || !createdBy.getAssignedSection().getSectionId().equals(section.getSectionId())) {
            throw new IllegalArgumentException("Teaching assistant is not assigned to section " + section.getSectionId());
        }
        this.section = section;
        this.createdBy = createdBy;
        this.submissions = new ArrayList<>();
    }

    public Section getSection() {
        return section;
    }

    public TeachingAssistant getCreatedBy() {
        return createdBy;
    }

    public void addSubmission(Submission submission) throws SubmissionDeadlineException {
        if (submission == null) {
            throw new SubmissionDeadlineException("Submission cannot be null");
        }
        if (submission.getAssignment() != this) {
            throw new SubmissionDeadlineException("Submission belongs to a different assignment");
        }
        if (getSubmissionBy(submission.getStudent()) != null) {
            throw new SubmissionDeadlineException("Student " + submission.getStudent().getStudentId()
                    + " has already submitted assignment " + getId());
        }
        submissions.add(submission);
    }

    public List<Submission> getSubmissions() {
        return submissions;
    }

    public Submission getSubmissionBy(Student student) {
        for (Submission s : submissions) {
            if (s.getStudent().getStudentId().equals(student.getStudentId())) {
                return s;
            }
        }
        return null;
    }

    public List<Submission> getLateSubmissions() {
        List<Submission> late = new ArrayList<>();
        for (Submission s : submissions) {
            if (s.isLate()) {
                late.add(s);
            }
        }
        return late;
    }

    public boolean isDeadlinePassed() {
        return LocalDate.now().isAfter(getDeadline());
    }

    public boolean isSubmissionWindowOpen(LocalDate date) {
        return true;
    }

    public String getDetails() {
        return getId() + " | " + getTitle() + " | Section " + section.getSectionId()
                + " | Deadline " + getDeadline() + " | Total marks " + getTotalMarks();
    }
}
