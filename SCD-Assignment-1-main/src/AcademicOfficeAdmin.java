import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class AcademicOfficeAdmin extends Administrator {
    private List<Request> requests;
    private List<Course> courses;
    private List<Section> sections;

    public AcademicOfficeAdmin(String adminId, String name, String email, String phone) {
        this(adminId, name, email, phone, new ArrayList<Course>(), new ArrayList<Section>());
    }

    public AcademicOfficeAdmin(String adminId, String name, String email, String phone,
                               List<Course> courses, List<Section> sections) {
        super(adminId, name, email, phone);
        this.requests = new ArrayList<>();
        this.courses = courses;
        this.sections = sections;
    }

    @Override
    public String getRole() {
        return "Academic Office Admin";
    }

    public void createCourse(Course course) throws CourseClashException {
        if (course == null) {
            throw new CourseClashException("Course cannot be null");
        }
        validateCourseDetails(course.getCourseCode(), course.getTitle(), course.getCreditHours());
        if (searchCourse(course.getCourseCode()) != null) {
            throw new CourseClashException("Course " + course.getCourseCode() + " already exists");
        }
        courses.add(course);
        Logger.log("Course " + course.getCourseCode() + " created by admin " + adminId);
    }

    public void updateCourse(Course course) throws CourseClashException {
        if (course == null) {
            throw new CourseClashException("Course cannot be null");
        }
        validateCourseDetails(course.getCourseCode(), course.getTitle(), course.getCreditHours());
        Course existing = searchCourse(course.getCourseCode());
        if (existing == null) {
            throw new CourseClashException("Course " + course.getCourseCode() + " does not exist");
        }
        existing.setTitle(course.getTitle());
        existing.setCreditHours(course.getCreditHours());
        Logger.log("Course " + existing.getCourseCode() + " updated by admin " + adminId);
    }

    public Course searchCourse(String courseCode) {
        if (courseCode == null) {
            return null;
        }
        for (Course c : courses) {
            if (c.getCourseCode().equalsIgnoreCase(courseCode.trim())) {
                return c;
            }
        }
        return null;
    }

    public List<Course> viewCourses() {
        return new ArrayList<>(courses);
    }

    public void createSection(Section section) throws CourseClashException {
        if (section == null) {
            throw new CourseClashException("Section cannot be null");
        }
        if (section.getSectionId() == null || section.getSectionId().isBlank()) {
            throw new CourseClashException("Section ID cannot be empty");
        }
        if (section.getCapacity() <= 0) {
            throw new CourseClashException("Capacity must be greater than zero");
        }
        if (section.getCourse() == null || !courses.contains(section.getCourse())) {
            throw new CourseClashException("The section's course must be created first");
        }
        for (Section s : sections) {
            if (s.getSectionId().equalsIgnoreCase(section.getSectionId())) {
                throw new CourseClashException("Section " + section.getSectionId() + " already exists");
            }
        }
        checkRoomFree(section, section.getSchedule());
        sections.add(section);
        if (!section.getCourse().getSections().contains(section)) {
            section.getCourse().addSection(section);
        }
        Logger.log("Section " + section.getSectionId() + " created for course "
                + section.getCourse().getCourseCode() + " by admin " + adminId);
    }

    public void updateSection(Section section) throws CourseClashException {
        Section existing = findRegisteredSection(section);
        if (existing != section) {
            existing.setCapacity(section.getCapacity());
            checkRoomFree(existing, section.getSchedule());
            existing.setSchedule(section.getSchedule());
        }
        Logger.log("Section " + existing.getSectionId() + " updated by admin " + adminId);
    }

    public void setCapacity(Section section, int capacity) throws CourseClashException {
        findRegisteredSection(section).setCapacity(capacity);
    }

    public void assignRoom(Section section, Schedule schedule) throws CourseClashException {
        Section existing = findRegisteredSection(section);
        if (schedule == null || schedule.getRoom() == null || schedule.getRoom().isBlank()) {
            throw new CourseClashException("A room is required");
        }
        checkRoomFree(existing, schedule);
        existing.setSchedule(schedule);
        Logger.log("Room " + schedule.getRoom() + " (" + schedule.getScheduleInfo() + ") assigned to section "
                + existing.getSectionId() + " by admin " + adminId);
    }

    public void assignInstructor(Section section, Instructor instructor) throws CourseClashException {
        Section existing = findRegisteredSection(section);
        if (instructor == null) {
            throw new CourseClashException("Instructor cannot be null");
        }
        for (Section other : instructor.viewSections()) {
            if (other != existing && other.hasClash(existing)) {
                throw new CourseClashException("Instructor " + instructor.getTeacherId()
                        + " already teaches section " + other.getSectionId() + " at that time");
            }
        }
        Instructor previous = existing.getInstructor();
        if (previous != null && previous != instructor) {
            previous.removeAssignedSection(existing);
        }
        existing.assignInstructor(instructor);
        instructor.addAssignedSection(existing);
        Logger.log("Admin " + adminId + " assigned instructor " + instructor.getTeacherId()
                + " to section " + existing.getSectionId());
    }

    public List<Section> viewSections() {
        return new ArrayList<>(sections);
    }

    private Section findRegisteredSection(Section section) throws CourseClashException {
        if (section != null) {
            for (Section s : sections) {
                if (s.getSectionId().equals(section.getSectionId())) {
                    return s;
                }
            }
        }
        throw new CourseClashException("Section does not exist");
    }

    private void validateCourseDetails(String code, String title, int creditHours) throws CourseClashException {
        if (code == null || code.isBlank()) {
            throw new CourseClashException("Course code cannot be empty");
        }
        if (title == null || title.isBlank()) {
            throw new CourseClashException("Course title cannot be empty");
        }
        if (creditHours < 1 || creditHours > 6) {
            throw new CourseClashException("Credit hours must be between 1 and 6");
        }
    }

    private void checkRoomFree(Section section, Schedule schedule) throws CourseClashException {
        if (schedule == null) {
            throw new CourseClashException("A schedule is required");
        }
        for (Section other : sections) {
            if (other != section && other.getSchedule().getRoom().equalsIgnoreCase(schedule.getRoom())
                    && other.getSchedule().hasClash(schedule)) {
                throw new CourseClashException("Room " + schedule.getRoom() + " is already used by section "
                        + other.getSectionId() + " at that time");
            }
        }
    }

    public void receiveRequest(Request request) throws InvalidRequestException {
        if (request == null) {
            throw new InvalidRequestException("Request cannot be null");
        }
        for (Request r : requests) {
            if (r.getRequestId().equals(request.getRequestId())) {
                throw new InvalidRequestException("Request " + request.getRequestId() + " was already received");
            }
        }
        requests.add(request);
    }

    public List<Request> viewRequests() {
        return new ArrayList<>(requests);
    }

    public List<Request> viewRequests(Comparator<Request> comparator) {
        List<Request> sorted = new ArrayList<>(requests);
        Collections.sort(sorted, comparator);
        return sorted;
    }

    public List<Request> viewPendingRequests() {
        List<Request> pending = new ArrayList<>();
        for (Request r : requests) {
            if (r.getStatus() == RequestStatus.PENDING) {
                pending.add(r);
            }
        }
        return pending;
    }

    public void approveRequest(Request request) throws RequestException, UnauthorizedActionException {
        updateRequestStatus(request, RequestStatus.APPROVED);
    }

    public void rejectRequest(Request request) throws RequestException, UnauthorizedActionException {
        updateRequestStatus(request, RequestStatus.REJECTED);
    }

    public void updateRequestStatus(Request request, RequestStatus newStatus)
            throws RequestException, UnauthorizedActionException {
        if (request == null || !requests.contains(request)) {
            Logger.log("Request processing error by admin " + adminId + ": request not found");
            throw new UnauthorizedActionException("This request was not submitted to you");
        }
        if (newStatus == null || newStatus == RequestStatus.PENDING) {
            Logger.log("Request processing error: invalid status for request " + request.getRequestId());
            throw new InvalidRequestException("A request can only be changed to APPROVED or REJECTED");
        }
        if (request.getStatus() != RequestStatus.PENDING) {
            Logger.log("Request processing error: request " + request.getRequestId()
                    + " is already " + request.getStatus());
            throw new InvalidRequestException("Request " + request.getRequestId() + " is already " + request.getStatus());
        }
        request.setStatus(newStatus);
        request.setProcessedBy(this);
        Logger.log("Request " + request.getRequestId() + " " + newStatus + " by admin " + adminId);
    }
}
