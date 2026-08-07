public final class StackOverflowDemo {
    private StackOverflowDemo() {
    }

    public static void main(String[] args) {
        if (args.length != 1 || !"--run".equals(args[0])) {
            System.err.println("Refusing to overflow without --run");
            System.exit(2);
        }

        recurse(1);
    }

    private static void recurse(long depth) {
        if (depth % 1_000 == 0) {
            System.out.println("depth=" + depth);
        }
        recurse(depth + 1); // Each call consumes another stack frame.
    }
}
