public class SelfAssessment {
    static interface IGreeter {
        String name();

        default String greet() {
            return "Hello " + name();
        }
    }

    static class Greeter implements IGreeter {
        @java.lang.Override
        public String name() {
            return "Yogesh";
        }

        @java.lang.Override
        public String greet() {
            return "Welcome " + name() + '!';
        }
    }

    public static void main(String[] args) {
        Greeter greeter = new Greeter();
        System.out.println(greeter.greet());
    }
}