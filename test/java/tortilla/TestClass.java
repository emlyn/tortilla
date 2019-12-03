package tortilla;

public class TestClass {
    public TestClass() {}

    public String foo(long i) {
        return "foo1_" + i;
    }

    public static String foo(long i, long j) {
        return "foo2_" + i + "_" + j;
    }

    public String foo(String... args) {
        return "foo3_" + String.join("_", args);
    }

    String foo(String s, long i) {
        throw new RuntimeException("This shouldn't be called, it isn't public");
    }
}
