public class VisitingInstructor extends Instructor {
    public VisitingInstructor(String teacherId, String name, String email, String phone) {
        super(teacherId, name, email, phone);
    }

    @Override
    public String getRole() {
        return "Visiting Instructor";
    }
}
