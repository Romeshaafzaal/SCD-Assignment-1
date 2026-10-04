import java.util.Comparator;

public class FYPMeetingDateComparator implements Comparator<FYPMeeting> {
    @Override
    public int compare(FYPMeeting m1, FYPMeeting m2) {
        return m1.getMeetingDate().compareTo(m2.getMeetingDate());
    }
}
