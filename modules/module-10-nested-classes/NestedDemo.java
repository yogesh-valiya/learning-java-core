public class NestedDemo {

    private String outerField = "outer-value";

    // 1. STATIC nested class — no link to any outer instance
    static class StaticNested {
        String describe() { return "static-nested (no outer instance)"; }
    }

    // 2. INNER class (non-static) — hidden reference to an outer instance
    class Inner {
        String describe() { return "inner sees outerField = " + outerField; }
    }

    // 3. method returning an ANONYMOUS class instance; captures 'prefix'
    Runnable makeRunnable(String prefix) {
        return new Runnable() {                      // anonymous class implementing Runnable
            public void run() {
                System.out.println("4) anon: " + prefix + " | outerField=" + outerField);
            }
        };
    }

    public static void main(String[] args) {
        // static nested: no outer instance needed
        StaticNested sn = new StaticNested();
        System.out.println("1) " + sn.describe());

        // inner: requires an OUTER INSTANCE via the special o.new Inner() syntax
        NestedDemo outer = new NestedDemo();
        NestedDemo.Inner inner = outer.new Inner();
        System.out.println("2) " + inner.describe());

        // change the outer's field, then run the anonymous class made earlier
        Runnable r = outer.makeRunnable("hello");
        outer.outerField = "CHANGED";               // mutate outer AFTER creating the anon
        System.out.println("3) (outerField is now CHANGED)");
        r.run();                                     // predict: does anon see old or new value?
    }
}
