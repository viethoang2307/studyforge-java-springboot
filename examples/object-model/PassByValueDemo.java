public class PassByValueDemo {
    private static final class Box {
        private int value;

        private Box(int value) {
            this.value = value;
        }
    }

    private static void changePrimitive(int value) {
        value = 99;
    }

    private static void mutateObject(Box box) {
        box.value = 99;
    }

    private static void reassignReference(Box box) {
        box = new Box(123);
        assert box.value == 123;
    }

    public static void main(String[] args) {
        int number = 1;
        changePrimitive(number);
        assert number == 1;

        Box box = new Box(1);
        mutateObject(box);
        assert box.value == 99;

        reassignReference(box);
        assert box.value == 99;
        System.out.println("Caller keeps its values; a copied reference can mutate the shared object.");
    }
}
