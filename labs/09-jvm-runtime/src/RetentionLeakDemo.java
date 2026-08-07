import java.util.ArrayList;
import java.util.List;

public final class RetentionLeakDemo {
    private static final List<byte[]> CACHE_WITHOUT_EVICTION = new ArrayList<>();

    private RetentionLeakDemo() {
    }

    public static void main(String[] args) throws InterruptedException {
        int batches = args.length == 0 ? 20 : Integer.parseInt(args[0]);
        for (int i = 1; i <= batches; i++) {
            CACHE_WITHOUT_EVICTION.add(new byte[512 * 1024]);
            System.out.printf("batch=%d, retained=%d KiB%n", i, i * 512);
            Thread.sleep(100);
        }
        System.out.println("The static list keeps every array reachable; GC cannot reclaim them.");
    }
}
