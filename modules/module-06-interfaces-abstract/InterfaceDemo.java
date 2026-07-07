public class InterfaceDemo {

    // ---- Interface with abstract + default + static methods ----
    interface Greeter {
        String name();                                  // abstract (public abstract)
        default String greet() {                        // default method (Java 8+)
            return "Hello, " + name();
        }
        static Greeter anonymous() {                    // static factory (Java 8+)
            return () -> "stranger";                    // lambda: single abstract method
        }
    }

    // Only implements the abstract method; inherits default greet()
    static class Person implements Greeter {
        private final String n;
        Person(String n) { this.n = n; }
        public String name() { return n; }
    }

    // ---- Diamond problem: two interfaces, same default method ----
    interface A { default String hi() { return "A.hi"; } }
    interface B { default String hi() { return "B.hi"; } }
    static class C implements A, B {
        // MUST override to resolve ambiguity; delegate with Interface.super
        public String hi() { return A.super.hi() + " + " + B.super.hi(); }
    }

    // ---- Abstract class: instance STATE + constructor + abstract method ----
    abstract static class Shape {
        private final String name;                      // interfaces can't hold state
        Shape(String name) { this.name = name; }        // interfaces can't have constructors
        abstract double area();                         // subclass must implement
        String describe() { return name + " area=" + String.format("%.2f", area()); }
    }
    static class Circle extends Shape {
        private final double r;
        Circle(double r) { super("circle"); this.r = r; }
        double area() { return Math.PI * r * r; }
    }

    public static void main(String[] args) {
        Greeter p = new Person("Yogesh");
        System.out.println("1) " + p.greet());                     // default method
        System.out.println("2) " + Greeter.anonymous().greet());   // static factory + lambda
        System.out.println("3) " + new C().hi());                  // diamond resolved
        System.out.println("4) " + new Circle(2).describe());      // abstract-class state
    }
}
