import java.util.Comparator;

public class AssignmentDeadlineComparator implements Comparator<Assignment> {
    @Override
    public int compare(Assignment a1, Assignment a2) {
        return a1.getDeadline().compareTo(a2.getDeadline());
    }
}
