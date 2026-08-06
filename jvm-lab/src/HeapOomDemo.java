import java.util.ArrayList;
import java.util.List;

public final class HeapOomDemo {
    private static final int ONE_MIB = 1024 * 1024;

    private HeapOomDemo() {
    }

    public static void main(String[] args) {
        if (args.length != 1 || !"--run".equals(args[0])) {
            System.err.println("Refusing to exhaust the heap without --run");
            System.exit(2);
        }

        List<byte[]> retained = new ArrayList<>();
        while (true) {
            retained.add(new byte[ONE_MIB]);
            System.out.println("retained MiB=" + retained.size());
        }
    }
}
