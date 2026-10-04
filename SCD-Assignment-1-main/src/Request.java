import java.time.LocalDate;

public abstract class Request {
    protected String requestId;
    protected LocalDate requestDate;
    protected String description;
    protected RequestStatus status;
    protected int priority;
    protected Student student;
    protected AcademicOfficeAdmin processedBy;

    public Request(String requestId, LocalDate requestDate, String description, int priority, Student student) throws InvalidRequestException {
        if (requestId == null || requestId.isBlank()) {
            throw new InvalidRequestException("Request ID cannot be empty");
        }
        if (requestDate == null) {
            throw new InvalidRequestException("Request date is required");
        }
        if (description == null || description.isBlank()) {
            throw new InvalidRequestException("Request description cannot be empty");
        }
        if (priority < 1) {
            throw new InvalidRequestException("Priority must be at least 1");
        }
        if (student == null) {
            throw new InvalidRequestException("Request must be associated with a student");
        }
        this.requestId = requestId;
        this.requestDate = requestDate;
        this.description = description;
        this.priority = priority;
        this.student = student;
        this.status = RequestStatus.PENDING;
    }

    public void submit() {
        Logger.log("Request " + requestId + " submitted by student " + student.getStudentId());
    }

    public String getRequestId() {
        return requestId;
    }

    public LocalDate getRequestDate() {
        return requestDate;
    }

    public String getDescription() {
        return description;
    }

    public RequestStatus getStatus() {
        return status;
    }

    public void setStatus(RequestStatus status) {
        this.status = status;
    }

    public int getPriority() {
        return priority;
    }

    public Student getStudent() {
        return student;
    }

    public AcademicOfficeAdmin getProcessedBy() {
        return processedBy;
    }

    public void setProcessedBy(AcademicOfficeAdmin admin) {
        this.processedBy = admin;
    }

    public abstract String getRequestType();

    public String getDetails() {
        return requestId + " | " + getRequestType() + " | Student " + student.getName()
                + " | Priority " + priority + " | Date " + requestDate + " | Status " + status
                + " | " + description;
    }
}
