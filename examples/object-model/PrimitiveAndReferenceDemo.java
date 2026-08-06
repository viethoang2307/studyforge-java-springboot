public class PrimitiveAndReferenceDemo {
    private static final class Counter {
        private int value;

        private Counter(int value) {
            this.value = value;
        }
    }

    public static void main(String[] args) {
        int firstNumber = 10;
        int secondNumber = firstNumber;
        secondNumber++;

        assert firstNumber == 10;
        assert secondNumber == 11;

        Counter firstCounter = new Counter(10);
        Counter secondCounter = firstCounter;
        secondCounter.value++;

        assert firstCounter == secondCounter;
        assert firstCounter.value == 11;
        System.out.println("Primitive is copied; references can share one object.");
    }
}
