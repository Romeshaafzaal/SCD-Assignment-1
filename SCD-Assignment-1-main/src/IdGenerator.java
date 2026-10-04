public class IdGenerator {
    public static String nextId(String prefix) {
        return prefix + System.currentTimeMillis() + "-" + (int)(Math.random() * 100);
    }
}
