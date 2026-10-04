import java.util.List;
import java.util.ArrayList;

public class PermanentInstructor extends Instructor implements Evaluator {

    private List<FYPGroup> fypGroups = new ArrayList<>();

    public PermanentInstructor(String teacherId, String name, String email, String phone) {
        super(teacherId, name, email, phone);
    }

    @Override
    public String getRole() {
        return "Permanent Instructor";
    }

    public void assignTA(NormalStudent student, Section section) throws UnauthorizedActionException {
        if (!assignedSections.contains(section)) {
            throw new UnauthorizedActionException("You are not assigned to this section");
        }
        for (Student s : section.getEnrolledStudents()) {
            if (s.getStudentId().equals(student.getStudentId())) {
                throw new UnauthorizedActionException("A student enrolled in this section cannot be its TA");
            }
        }
        TeachingAssistant ta = new TeachingAssistant(
                student.getStudentId(), section, student.getName(), student.getEmail(), student.getPhone()
        );
        section.assignTA(ta);
        Logger.log("Student " + student.getStudentId() + " assigned as TA for section " + section.getSectionId());
    }

    public void addFYPGroup(FYPGroup group) {
        if (group != null && !fypGroups.contains(group)) {
            fypGroups.add(group);
        }
    }

    public void removeFYPGroup(FYPGroup group) {
        fypGroups.remove(group);
    }

    public List<FYPGroup> viewFYPGroups() {
        return new ArrayList<>(fypGroups);
    }

    public String viewFYPGroupDetails(FYPGroup group) throws UnauthorizedActionException {
        checkSupervises(group);
        return group.getDetails();
    }

    public List<Student> viewFYPGroupMembers(FYPGroup group) throws UnauthorizedActionException {
        checkSupervises(group);
        return group.getMembers();
    }

    public void scheduleFYPMeeting(FYPGroup group, FYPMeeting meeting)
            throws UnauthorizedActionException, InvalidFYPGroupException {
        checkSupervises(group);
        if (meeting == null) {
            throw new InvalidFYPGroupException("Meeting cannot be null");
        }
        if (meeting.getMeetingDate().isBefore(java.time.LocalDate.now())) {
            throw new InvalidFYPGroupException("Cannot schedule a meeting in the past");
        }
        group.addMeeting(meeting);
        Logger.log("FYP meeting scheduled for group " + group.getGroupId() + " by " + teacherId);
    }

    public void evaluateFYPIdea(FYPGroup group, FYPEvaluation evaluation)
            throws UnauthorizedActionException, InvalidFYPGroupException, InvalidFYPEvaluationException {
        checkSupervises(group);
        group.addEvaluation(evaluation);
        Logger.log("FYP idea evaluated for group " + group.getGroupId() + " by " + teacherId);
    }

    public void provideFYPFeedback(FYPEvaluation evaluation, String feedback)
            throws UnauthorizedActionException, InvalidFYPEvaluationException {
        FYPGroup owner = null;
        for (FYPGroup g : fypGroups) {
            if (g.getEvaluations().contains(evaluation)) {
                owner = g;
            }
        }
        if (evaluation == null || owner == null) {
            throw new UnauthorizedActionException("This evaluation does not belong to one of your groups");
        }
        evaluation.addFeedback(feedback);
        Logger.log("Feedback provided on FYP evaluation " + evaluation.getEvaluationId() + " by " + teacherId);
    }

    private void checkSupervises(FYPGroup group) throws UnauthorizedActionException {
        if (group == null || group.getSupervisor() != this) {
            throw new UnauthorizedActionException("You are not the supervisor of this FYP group");
        }
    }

    @Override
    public void evaluate() {
        Logger.log("Generic evaluate() called on Permanent Instructor " + this.teacherId);
    }
}