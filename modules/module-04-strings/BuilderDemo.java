public class BuilderDemo {
    public static void main(String[] args) {

        int n = 100_000;

        // ---- 1) Immutable String += in a loop  (O(n^2)) ----
        long t0 = System.nanoTime();
        String s = "";
        for (int i = 0; i < n; i++) {
            s += "x";                       // new String every iteration
        }
        long t1 = System.nanoTime();
        System.out.println("String +=      : " + (t1 - t0) / 1_000_000 + " ms, len=" + s.length());

        // ---- 2) StringBuilder append  (O(n)) ----
        long t2 = System.nanoTime();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n; i++) {
            sb.append("x");                 // mutates same buffer
        }
        String r = sb.toString();
        long t3 = System.nanoTime();
        System.out.println("StringBuilder  : " + (t3 - t2) / 1_000_000 + " ms, len=" + r.length());

        // ---- 3) Mutability proof: append returns the SAME object ----
        StringBuilder a = new StringBuilder("hi");
        StringBuilder b = a.append(" there");
        System.out.println("same object?   : " + (a == b));           // predict?
        System.out.println("a is now       : " + a);                  // predict?

        // ---- 4) Default capacity ----
        StringBuilder empty = new StringBuilder();
        System.out.println("default cap    : " + empty.capacity());   // predict?
    }
}
