import java.time.LocalDate;

public class CourseClashRequest extends Request {
    private Section conflictingSection;
    private Section requestedSection;

    public CourseClashRequest(String requestId, LocalDate requestDate, String description, int priority,
                              Student student, Section conflictingSection, Section requestedSection)
            throws InvalidRequestException {
        super(requestId, requestDate, description, priority, student);
        if (conflictingSection == null || requestedSection == null) {
            throw new InvalidRequestException("Both conflicting and requested sections are required");
        }
        if (conflictingSection == requestedSection) {
            throw new InvalidRequestException("Conflicting section and requested section cannot be the same");
        }
        if (!conflictingSection.hasClash(requestedSection)) {
            throw new InvalidRequestException("Sections " + conflictingSection.getSectionId()
                    + " and " + requestedSection.getSectionId() + " do not clash");
        }
        this.conflictingSection = conflictingSection;
        this.requestedSection = requestedSection;
    }

    public Section getConflictingSection() {
        return conflictingSection;
    }

    public Section getRequestedSection() {
        return requestedSection;
    }

    public String getConflictDetails() {
        return "Conflicting: " + conflictingSection.getSectionId() + " (" + conflictingSection.getSchedule().getScheduleInfo()
                + ") vs Requested: " + requestedSection.getSectionId() + " (" + requestedSection.getSchedule().getScheduleInfo() + ")";
    }

    @Override
    public String getRequestType() {
        return "COURSE_CLASH";
    }

    @Override
    public String getDetails() {
        return super.getDetails() + " | " + getConflictDetails();
    }
}
