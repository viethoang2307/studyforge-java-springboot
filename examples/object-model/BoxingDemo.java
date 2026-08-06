public class BoxingDemo {
    public static void main(String[] args) {
        Integer boxed = 42;
        int unboxed = boxed;
        assert boxed.equals(42);
        assert unboxed == 42;

        Integer cachedFirst = 127;
        Integer cachedSecond = 127;
        assert cachedFirst == cachedSecond;

        Integer largerFirst = 1_000;
        Integer largerSecond = 1_000;
        assert largerFirst.equals(largerSecond);
        System.out.printf("1000: == %s, equals %s%n",
                largerFirst == largerSecond, largerFirst.equals(largerSecond));

        Integer nullable = null;
        try {
            int ignored = nullable;
            throw new AssertionError("Unboxing null should fail, but got " + ignored);
        } catch (NullPointerException expected) {
            System.out.println("Unboxing null throws NullPointerException.");
        }
    }
}
