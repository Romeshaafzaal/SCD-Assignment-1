import java.time.LocalDate;

public class FYPMeeting {
    private String meetingId;
    private LocalDate meetingDate;
    private String agenda;
    private String notes;

    public FYPMeeting(String meetingId, LocalDate meetingDate, String agenda) throws InvalidFYPGroupException {
        this(meetingId, meetingDate, agenda, "");
    }

    public FYPMeeting(String meetingId, LocalDate meetingDate, String agenda, String notes) throws InvalidFYPGroupException {
        if (meetingId == null || meetingId.isBlank()) {
            throw new InvalidFYPGroupException("Meeting ID cannot be empty");
        }
        if (meetingDate == null) {
            throw new InvalidFYPGroupException("Meeting date is required");
        }
        if (agenda == null || agenda.isBlank()) {
            throw new InvalidFYPGroupException("Meeting agenda cannot be empty");
        }
        this.meetingId = meetingId;
        this.meetingDate = meetingDate;
        this.agenda = agenda;
        this.notes = (notes == null) ? "" : notes;
    }

    public String getMeetingId() {
        return meetingId;
    }

    public LocalDate getMeetingDate() {
        return meetingDate;
    }

    public String getAgenda() {
        return agenda;
    }

    public String getNotes() {
        return notes;
    }

    public void updateNotes(String notes) {
        this.notes = (notes == null) ? "" : notes;
        Logger.log("Notes updated for FYP meeting " + meetingId);
    }

    public String getMeetingDetails() {
        String detail = meetingId + " | Date: " + meetingDate + " | Agenda: " + agenda;
        if (!notes.isEmpty()) {
            detail += " | Notes: " + notes;
        }
        return detail;
    }
}
