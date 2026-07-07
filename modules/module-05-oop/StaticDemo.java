public class StaticDemo {

    static class Parent {
        static String who()  { return "Parent.static"; }   // static
        String       name()  { return "Parent.instance"; } // instance
    }

    static class Child extends Parent {
        static String who()  { return "Child.static"; }     // HIDES Parent.who
        @Override
        String       name()  { return "Child.instance"; }   // OVERRIDES Parent.name
    }

    // shared-counter demo
    static class Counter {
        static int total = 0;
        int id;
        Counter() { id = ++total; }
    }

    public static void main(String[] args) {

        Child p = new Child();   // declared type Parent, runtime type Child

        System.out.println("p.name() = " + p.name());   // predict? (instance/override)
        System.out.println("p.who()  = " + p.who());    // predict? (static/hiding)

/*
* p.name() = Parent.static
* p.who() = Child.instance
* a.id=1 b.id=2 c.id=3 Counter.total=3
*
* */

        // shared static field across instances
        Counter a = new Counter();
        Counter b = new Counter();
        Counter c = new Counter();
        System.out.println("a.id=" + a.id + " b.id=" + b.id + " c.id=" + c.id
                + "  Counter.total=" + Counter.total);   // predict?
    }
}
