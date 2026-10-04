import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FYPGroup {
    private String groupId;
    private String title;
    private String description;
    private List<Student> members;
    private PermanentInstructor supervisor;
    private List<FYPMeeting> meetings;
    private List<FYPEvaluation> evaluations;

    public FYPGroup(String groupId, String title, String description) throws InvalidFYPGroupException {
        if (groupId == null || groupId.isBlank()) {
            throw new InvalidFYPGroupException("Group ID cannot be empty");
        }
        if (title == null || title.isBlank()) {
            throw new InvalidFYPGroupException("Group title cannot be empty");
        }
        this.groupId = groupId;
        this.title = title;
        this.description = (description == null) ? "" : description;
        this.members = new ArrayList<>();
        this.meetings = new ArrayList<>();
        this.evaluations = new ArrayList<>();
        Logger.log("FYP group " + groupId + " created: " + title);
    }

    public String getGroupId() {
        return groupId;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public PermanentInstructor getSupervisor() {
        return supervisor;
    }

    public void addMember(Student student) throws InvalidFYPGroupException {
        if (student == null) {
            throw new InvalidFYPGroupException("Student cannot be null");
        }
        if (hasMember(student)) {
            throw new InvalidFYPGroupException("Student " + student.getStudentId() + " is already in group " + groupId);
        }
        members.add(student);
        Logger.log("Student " + student.getStudentId() + " added to FYP group " + groupId);
    }

    public void removeMember(Student student) throws InvalidFYPGroupException {
        if (student == null || !hasMember(student)) {
            throw new InvalidFYPGroupException("Student is not a member of group " + groupId);
        }
        for (int i = 0; i < members.size(); i++) {
            if (members.get(i).getStudentId().equals(student.getStudentId())) {
                members.remove(i);
                break;
            }
        }
        Logger.log("Student " + student.getStudentId() + " removed from FYP group " + groupId);
    }

    public boolean hasMember(Student student) {
        for (Student s : members) {
            if (s.getStudentId().equals(student.getStudentId())) {
                return true;
            }
        }
        return false;
    }

    public List<Student> getMembers() {
        return new ArrayList<>(members);
    }

    public void assignSupervisor(PermanentInstructor supervisor) throws InvalidFYPGroupException {
        if (supervisor == null) {
            throw new InvalidFYPGroupException("Supervisor cannot be null");
        }
        if (this.supervisor != null && this.supervisor != supervisor) {
            this.supervisor.removeFYPGroup(this);
        }
        this.supervisor = supervisor;
        supervisor.addFYPGroup(this);
        Logger.log("Supervisor " + supervisor.getTeacherId() + " assigned to FYP group " + groupId);
    }

    public void addMeeting(FYPMeeting meeting) throws InvalidFYPGroupException {
        if (meeting == null) {
            throw new InvalidFYPGroupException("Meeting cannot be null");
        }
        if (supervisor == null) {
            throw new InvalidFYPGroupException("Group " + groupId + " has no supervisor yet");
        }
        for (FYPMeeting m : meetings) {
            if (m.getMeetingId().equals(meeting.getMeetingId())) {
                throw new InvalidFYPGroupException("Meeting " + meeting.getMeetingId() + " already exists");
            }
        }
        meetings.add(meeting);
        Logger.log("FYP meeting " + meeting.getMeetingId() + " scheduled for group " + groupId
                + " on " + meeting.getMeetingDate());
    }

    public List<FYPMeeting> getMeetings() {
        return new ArrayList<>(meetings);
    }

    public void addEvaluation(FYPEvaluation evaluation) throws InvalidFYPGroupException, InvalidFYPEvaluationException {
        if (evaluation == null) {
            throw new InvalidFYPEvaluationException("Evaluation cannot be null");
        }
        if (supervisor == null) {
            throw new InvalidFYPGroupException("Group " + groupId + " has no supervisor yet");
        }
        if (evaluation.getEvaluator() != supervisor) {
            throw new InvalidFYPEvaluationException("Only the group's supervisor can evaluate group " + groupId);
        }
        if (!evaluation.isEvaluated()) {
            throw new InvalidFYPEvaluationException("Evaluation has no score yet");
        }
        for (FYPEvaluation e : evaluations) {
            if (e.getEvaluationId().equals(evaluation.getEvaluationId())) {
                throw new InvalidFYPEvaluationException("Evaluation " + evaluation.getEvaluationId() + " already exists");
            }
        }
        evaluations.add(evaluation);
        Logger.log("FYP evaluation " + evaluation.getEvaluationId() + " added to group " + groupId);
    }

    public List<FYPEvaluation> getEvaluations() {
        return new ArrayList<>(evaluations);
    }

    public String getDetails() {
        StringBuilder sb = new StringBuilder();
        sb.append("Group ").append(groupId).append(": ").append(title).append("\n");
        sb.append("  Description: ").append(description).append("\n");
        sb.append("  Supervisor: ").append(supervisor == null ? "(none)" : supervisor.getName()).append("\n");
        sb.append("  Members: ");
        if (members.isEmpty()) {
            sb.append("(none)");
        }
        for (int i = 0; i < members.size(); i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(members.get(i).getName()).append(" (").append(members.get(i).getStudentId()).append(")");
        }
        sb.append("\n  Meetings:");
        List<FYPMeeting> sortedMeetings = new ArrayList<>(meetings);
        Collections.sort(sortedMeetings, new FYPMeetingDateComparator());
        for (FYPMeeting m : sortedMeetings) {
            sb.append("\n    ").append(m.getMeetingDetails());
        }
        if (meetings.isEmpty()) {
            sb.append(" (none)");
        }
        sb.append("\n  Evaluations:");
        for (FYPEvaluation e : evaluations) {
            sb.append("\n    ").append(e.getEvaluationId()).append(" | ").append(e.getEvaluationDate())
              .append(" | Score ").append(e.getScore()).append(" | Feedback: ")
              .append(e.getFeedback().isEmpty() ? "(none)" : e.getFeedback());
        }
        if (evaluations.isEmpty()) {
            sb.append(" (none)");
        }
        return sb.toString();
    }
}
