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

    public static String withPrimitives(boolean x) { return "boolean_" + x; }
    public static String withPrimitives(char x) { return "char_" + x; }
    public static String withPrimitives(byte x) { return "byte_" + x; }
    public static String withPrimitives(short x) { return "short_" + x; }
    public static String withPrimitives(int x) { return "int_" + x; }
    public static String withPrimitives(long x) { return "long_" + x; }
    public static String withPrimitives(float x) { return "float_" + x; }
    public static String withPrimitives(double x) { return "double_" + x; }
    public static String withPrimitives(String x) { return "String_" + x; }

    public static String withoutPrimitives(Boolean x) { return "Boolean_" + x; }
    public static String withoutPrimitives(Character x) { return "Character_" + x; }
    public static String withoutPrimitives(Byte x) { return "Byte_" + x; }
    public static String withoutPrimitives(Short x) { return "Short_" + x; }
    public static String withoutPrimitives(Integer x) { return "Integer_" + x; }
    public static String withoutPrimitives(Long x) { return "Long_" + x; }
    public static String withoutPrimitives(Float x) { return "Float_" + x; }
    public static String withoutPrimitives(Double x) { return "Double_" + x; }
    public static String withoutPrimitives(String x) { return "String_" + x; }

    // non-public method, should not get a wrapper function
    String foo(String s, long i) {
        throw new RuntimeException("This shouldn't be called, it isn't public");
    }
}
