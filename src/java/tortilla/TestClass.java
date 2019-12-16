package tortilla;

import java.util.Arrays;

public class TestClass {
    // constructor
    public TestClass() {}

    // simple method
    public String foo(long i) {
        return "foo1_" + i;
    }

    // satic method
    public static String foo(long i, long j) {
        return "foo2_" + i + "_" + j;
    }

    // vararg method
    public String foo(String... args) {
        return "foo3_" + String.join("_", args);
    }

    // array (of Object and primitive) args
    public static long bar(Long[] i, long[] j) {
        return Arrays.stream(i).mapToLong(Long::longValue).sum()
            - Arrays.stream(j).sum();
    }

    // for coercion to int
    public static String hex(int i) {
        return Integer.toHexString(i);
    }

    // for coercion to Integer
    public static String hexy(Integer i) {
        if (i == null) {
            return "<null>";
        } else {
            return Integer.toHexString(i);
        }
    }

    // two overloads with overlapping types, one more specific
    public static String baz(String x) {
        return "baz1_" + x;
    }

    // ...and one less specific
    public static String baz(Object x) {
        return "baz2_" + x;
    }

    // for coercion in varargs
    public static int qux(int i, int... j) {
        return i - Arrays.stream(j).sum();
    }

    // second vararg overload with non-consecutive number of args
    public static String qux(String s, int i, int j, int... k) {
        return "qux_" + s + (i + j - Arrays.stream(k).sum());
    }

    // void return
    public static void flibble(Long i) {
        // do nothing
    }

    // non-public method, should not get a wrapper function
    String foo(String s, long i) {
        throw new RuntimeException("This shouldn't be called, it isn't public");
    }
}
