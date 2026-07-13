import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class ModernJavaDemo {

    // Compact constructor -- validates without restating field assignments.
    record Range(int low, int high) {
        Range {
            if (low > high) throw new IllegalArgumentException("low (" + low + ") > high (" + high + ")");
        }
    }

    // Sealed interface + records implementing it -- enables exhaustive switch pattern matching.
    sealed interface Shape permits Circle, Square, Triangle {}
    record Circle(double radius) implements Shape {}
    record Square(double side) implements Shape {}
    record Triangle(double base, double height) implements Shape {}

    static double area(Shape shape) {
        return switch (shape) {
            case Circle(double r) -> Math.PI * r * r;
            case Square(double side) -> side * side;
            case Triangle(double b, double h) -> 0.5 * b * h;
            // no default -- the compiler knows Circle/Square/Triangle are the ONLY permitted cases
        };
    }

    public static void main(String[] args) throws Exception {
        // 1) var is statically typed -- an inferred concrete type, not dynamic typing.
        var message = "hello";
        System.out.println("1) var-declared variable's actual runtime class: " + message.getClass().getName());

        // 2) Record compact constructor validation.
        Range ok = new Range(1, 10);
        System.out.println("2) valid Range: " + ok);
        try {
            new Range(10, 1);
        } catch (IllegalArgumentException e) {
            System.out.println("3) invalid Range threw: " + e.getMessage());
        }

        // 3) Sealed + exhaustive switch pattern matching with record deconstruction.
        List<Shape> shapes = List.of(new Circle(2), new Square(3), new Triangle(4, 5));
        for (Shape s : shapes) {
            System.out.printf("4) area of %s = %.2f%n", s, area(s));
        }

        // 4) Text block.
        String name = "Ravi";
        String json = """
                {
                  "name": "%s"
                }""".formatted(name);
        System.out.println("5) text block result:");
        System.out.println(json);

        // 5) Virtual threads -- many lightweight, JVM-managed threads.
        AtomicInteger completed = new AtomicInteger(0);
        int taskCount = 10_000;
        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            List<Future<?>> futures = new ArrayList<>();
            for (int i = 0; i < taskCount; i++) {
                futures.add(executor.submit(() -> { completed.incrementAndGet(); }));
            }
            for (Future<?> f : futures) f.get();
        }
        System.out.println("6) virtual thread tasks completed: " + completed.get() + " / " + taskCount);
        System.out.println("7) an unstarted virtual thread's toString: "
                + Thread.ofVirtual().unstarted(() -> {}));
    }
}
