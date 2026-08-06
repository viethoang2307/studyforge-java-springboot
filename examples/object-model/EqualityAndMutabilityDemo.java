import java.util.ArrayList;
import java.util.List;

public class EqualityAndMutabilityDemo {
    public static void main(String[] args) {
        String first = new String("Java");
        String second = new String("Java");

        assert first != second;
        assert first.equals(second);

        List<String> mutable = new ArrayList<>();
        final List<String> finalReference = mutable;
        finalReference.add("changed");
        assert mutable.size() == 1;

        String immutable = "Java";
        String upperCase = immutable.toUpperCase();
        assert immutable.equals("Java");
        assert upperCase.equals("JAVA");
        assert immutable != upperCase;

        System.out.println("== checks identity; equals checks String content.");
        System.out.println("final reference still points to a mutable list: " + finalReference);
    }
}
