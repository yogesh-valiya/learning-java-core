import java.util.ArrayList;
import java.util.List;
import java.util.function.*;

public class LambdaDemo {

    int value = 100;

    Runnable makeAnonymous() {
        return new Runnable() {
            @Override
            public void run() {
                System.out.println("   anonymous class 'this' class name: " + this.getClass().getSimpleName());
                // this.value would NOT compile here -- Runnable has no 'value' field.
                System.out.println("   reaching outer field needs LambdaDemo.this.value: " + LambdaDemo.this.value);
            }
        };
    }

    Runnable makeLambda() {
        return () -> {
            System.out.println("   lambda 'this' class name: " + this.getClass().getSimpleName());
            System.out.println("   lambda sees enclosing field directly via this.value: " + this.value);
        };
    }

    public static void main(String[] args) {
        LambdaDemo demo = new LambdaDemo();
        System.out.println("1) anonymous class:");
        demo.makeAnonymous().run();
        System.out.println("2) lambda:");
        demo.makeLambda().run();

        // The 4 core functional interfaces, plus their default-method chaining.
        Function<Integer, Integer> doubleIt = x -> x * 2;
        Function<Integer, Integer> addTen = x -> x + 10;
        System.out.println("3) doubleIt.andThen(addTen).apply(5) = " + doubleIt.andThen(addTen).apply(5));
        System.out.println("4) doubleIt.compose(addTen).apply(5) = " + doubleIt.compose(addTen).apply(5));

        Predicate<String> isLong = s -> s.length() > 3;
        Predicate<String> startsWithA = s -> s.startsWith("A");
        System.out.println("5) isLong.and(startsWithA).test(\"Alpha\") = " + isLong.and(startsWithA).test("Alpha"));
        System.out.println("6) isLong.negate().test(\"Al\") = " + isLong.negate().test("Al"));

        // The 4 kinds of method reference.
        Consumer<String> printer = System.out::println;              // bound instance method reference
        printer.accept("7) consumer via bound-instance method reference");

        Supplier<List<String>> listFactory = ArrayList::new;          // constructor reference
        System.out.println("8) supplier-created list is empty: " + listFactory.get().isEmpty());

        Function<String, Integer> parse = Integer::parseInt;          // static method reference
        System.out.println("9) parse.apply(\"42\") = " + parse.apply("42"));

        Function<String, String> upper = String::toUpperCase;         // unbound instance method reference
        System.out.println("10) upper.apply(\"hi\") = " + upper.apply("hi"));
    }
}
