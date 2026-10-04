public abstract class Administrator extends Person {
    protected String adminId;

    public Administrator(String adminId, String name, String email, String phone) {
        super(name, email, phone);
        this.adminId = adminId;
    }

    public String getAdminId() {
        return adminId;
    }

    @Override
    public abstract String getRole();
}