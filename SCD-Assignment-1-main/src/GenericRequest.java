import java.time.LocalDate;

public class GenericRequest extends Request {
    private RequestCategory category;

    public GenericRequest(String requestId, LocalDate requestDate, String description, int priority,
                          Student student, RequestCategory category) throws InvalidRequestException {
        super(requestId, requestDate, description, priority, student);
        if (category == null) {
            throw new InvalidRequestException("Category is required");
        }
        this.category = category;
    }

    public RequestCategory getCategory() {
        return category;
    }

    @Override
    public String getRequestType() {
        return "GENERIC";
    }

    @Override
    public String getDetails() {
        return super.getDetails() + " | Category: " + category;
    }
}
