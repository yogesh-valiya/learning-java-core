public class ChainingDemo {

    static class Animal {
        Animal() {
            System.out.println("  Animal()  no-arg");
        }
        Animal(String n) {
            System.out.println("  Animal(\"" + n + "\")");
        }
    }

    static class Dog extends Animal {
        Dog() {
            this("Rex");                       // chain to Dog(String) — must be 1st stmt
            System.out.println("  Dog()  no-arg");
        }
        Dog(String n) {
            super(n);                          // call Animal(String) — must be 1st stmt
            System.out.println("  Dog(\"" + n + "\")");
        }
    }

    // Demonstrates field-init vs constructor-body order across a hierarchy
    static class Base {
        int b = printAndReturn("Base.field init", 1);
        Base() { System.out.println("  Base() body"); }
        static int printAndReturn(String label, int v) {
            System.out.println("  " + label);
            return v;
        }
    }
    static class Derived extends Base {
        int d = printAndReturn("Derived.field init", 2);
        Derived() { System.out.println("  Derived() body"); }
    }

    public static void main(String[] args) {
        System.out.println("new Dog():");
        new Dog();

        System.out.println("\nnew Derived():");
        new Derived();
    }
}
