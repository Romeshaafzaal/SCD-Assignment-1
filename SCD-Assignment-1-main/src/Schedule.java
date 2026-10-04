import java.time.LocalTime;

public class Schedule {
    private Day day;
    private LocalTime startTime;
    private LocalTime endTime;
    private String room;

    public Schedule(Day day, LocalTime startTime, LocalTime endTime, String room) {
        this.day = day;
        this.startTime = startTime;
        this.endTime = endTime;
        this.room = room;
    }

    public Day getDay() {
        return day;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public String getRoom() {
        return room;
    }

    public boolean hasClash(Schedule other) {
        if (this.day != other.day) {
            return false;
        }
        return this.startTime.isBefore(other.endTime) && other.startTime.isBefore(this.endTime);
    }

    public String getScheduleInfo() {
        return day + " " + startTime + "-" + endTime + " Room " + room;
    }
}