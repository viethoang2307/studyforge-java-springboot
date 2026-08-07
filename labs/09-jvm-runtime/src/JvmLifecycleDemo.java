import java.util.ArrayList;
import java.util.List;

public final class JvmLifecycleDemo {
    private JvmLifecycleDemo() {
    }

    public static void main(String[] args) throws InterruptedException {
        int iterations = args.length == 0 ? 2_000_000 : Integer.parseInt(args[0]);
        List<String> heapObjects = new ArrayList<>();

        for (int i = 0; i < iterations; i++) {
            int result = square(i); // A hot method gives the JIT something to compile.
            if (i % 100_000 == 0) {
                heapObjects.add("checkpoint-" + result);
            }
        }

        System.out.printf("Loaded by: %s%n", JvmLifecycleDemo.class.getClassLoader());
        System.out.printf("Retained heap objects: %d%n", heapObjects.size());
        System.out.printf("PID: %d (pause briefly for jcmd)%n", ProcessHandle.current().pid());
        Thread.sleep(3_000);
    }

    static int square(int value) {
        return value * value;
    }
}
