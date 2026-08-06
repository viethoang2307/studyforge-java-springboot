import java.math.BigDecimal;

public class NumberPitfallsDemo {
    public static void main(String[] args) {
        int wrapped = Integer.MAX_VALUE + 1;
        assert wrapped == Integer.MIN_VALUE;

        try {
            Math.addExact(Integer.MAX_VALUE, 1);
            throw new AssertionError("addExact should detect overflow");
        } catch (ArithmeticException expected) {
            System.out.println("addExact detects integer overflow.");
        }

        double binarySum = 0.1 + 0.2;
        assert binarySum != 0.3;
        System.out.printf("0.1 + 0.2 as double = %.17f%n", binarySum);

        BigDecimal exactSum = new BigDecimal("0.1").add(new BigDecimal("0.2"));
        assert exactSum.equals(new BigDecimal("0.3"));

        BigDecimal oneDecimalPlace = new BigDecimal("1.0");
        BigDecimal twoDecimalPlaces = new BigDecimal("1.00");
        assert !oneDecimalPlace.equals(twoDecimalPlaces);
        assert oneDecimalPlace.compareTo(twoDecimalPlaces) == 0;
        System.out.println("BigDecimal string sum = " + exactSum);
    }
}
