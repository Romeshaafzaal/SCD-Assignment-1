import java.util.Comparator;

public class RequestPriorityComparator implements Comparator<Request> {
    @Override
    public int compare(Request r1, Request r2) {
        return Integer.compare(r1.getPriority(), r2.getPriority());
    }
}
