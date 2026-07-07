public class OverloadOverrideDemo {

    // ---- OVERLOADING: same name, different params, resolved at COMPILE time ----
    static class Printer {
        String f(Object o)  { return "f(Object)";  }
        String f(String s)  { return "f(String)";  }
        String f(Integer i) { return "f(Integer)"; }
    }

    // ---- OVERRIDING: same signature, resolved at RUNTIME ----
    static class Animal {
        String speak() { return "Animal: ..."; }
    }
    static class Dog extends Animal {
        @Override String speak() { return "Dog: Woof"; }
    }

    public static void main(String[] args) {
        Printer p = new Printer();

        Object x = "hello";            // declared type Object, runtime type String
        System.out.println("p.f(x)     = " + p.f(x));      // predict? (uses DECLARED type)
        System.out.println("p.f(\"hi\")  = " + p.f("hi"));   // predict?
        System.out.println("p.f(42)    = " + p.f(42));       // predict? (int → ?)

        Animal a = new Dog();          // declared type Animal, runtime type Dog
        System.out.println("a.speak()  = " + a.speak());   // predict? (uses RUNTIME type)
    }
}
