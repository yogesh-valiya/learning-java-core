public class EqualsDemo {
    public static void main(String[] args) {

        // ---- Block A: primitives ----
        int a = 1000;
        int b = 1000;
        System.out.println("A1: " + (a == b));        // predict?

        // ---- Block B: Strings via 'new' ----
        String x = new String("hi");
        String y = new String("hi");
        System.out.println("B1: " + (x == y));         // predict?
        System.out.println("B2: " + x.equals(y));      // predict?

        // ---- Block C: String literals (the string pool) ----
        String p = "hi";
        String q = "hi";
        System.out.println("C1: " + (p == q));         // predict?
        System.out.println("C2: " + p.equals(q));      // predict?

        // ---- Block D: Integer wrapper caching (THE classic gotcha) ----
        Integer m = 100;
        Integer n = 100;
        System.out.println("D1: " + (m == n));         // predict?

        Integer big1 = 1000;
        Integer big2 = 1000;
        System.out.println("D2: " + (big1 == big2));   // predict?
        System.out.println("D3: " + big1.equals(big2));// predict?
    }
}
