public class ImmutabilityDemo {
    public static void main(String[] args) {

        // ---- Block A: does a method mutate the original? ----
        String s = "hello";
        s.toUpperCase();                 // result ignored
        System.out.println("A: " + s);   // predict?

        // ---- Block B: reassignment ----
        String t = "hello";
        t = t.concat(" world");
        System.out.println("B: " + t);   // predict?

        // ---- Block C: pool identity after a "modification" ----
        String x = "hi";
        String y = "hi";
        String z = x.concat("");         // concat, even with "", builds a new String
        System.out.println("C1: " + (x == y));   // predict?
        System.out.println("C2: " + (x == z));   // predict?
        System.out.println("C3: " + x.equals(z));// predict?

        // ---- Block D: intern() ----
        String a = new String("hi");     // forced new heap object (not pooled)
        String b = a.intern();           // intern() returns the POOLED "hi"
        System.out.println("D1: " + (a == "hi")); // predict?
        System.out.println("D2: " + (b == "hi")); // predict?
    }
}
