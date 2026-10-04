public class NormalStudent extends Student {
    public NormalStudent(String studentId, String name, String email, String phone) {
        super(studentId, name, email, phone);
    }

    @Override
    public String getRole() {
        return "Normal Student";
    }
}