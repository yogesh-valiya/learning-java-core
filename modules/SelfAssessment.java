import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class SelfAssessment {
    static interface IGreeter {
        String defaultName = "Yogesh";

        String name();
        void setName(String name);

        default String greet() {
            return "Hello " + name();
        }
    }

    static class Greeter implements IGreeter {
        private String currentName;

        @java.lang.Override
        public String name() {
            return currentName != null ? this.currentName : IGreeter.defaultName;
        }

        @Override
        public void setName(String name) {
            this.currentName = name;
        }
    }

    public static void main(String[] args) {
        Greeter greeter = new Greeter();
        System.out.println(greeter.greet());
        greeter.setName("Yogi");
        System.out.println(greeter.greet());

        List<String> names = Arrays.asList("Charlie", "Alpha", "Beta");

        names.sort(Comparator.naturalOrder());

        System.out.println(names); // Output: [Alpha, Beta, Charlie]
    }
}