import java.util.*;

// Run this as TWO SEPARATE JVM processes and compare, e.g.:
//   java ResizeDemo            (default capacity)
//   java ResizeDemo presized   (pre-sized to avoid ever resizing)
//
// An earlier version of this demo ran both configurations back-to-back inside
// ONE process (warm up, then time). That gave noisy, inconsistent, sometimes
// backwards results -- JIT tiering and GC state bleed between two consecutive
// HashMap-building loops in the same JVM. Measuring each configuration as an
// independent process removes that contamination and gives a clean, repeatable
// signal (see module-13-hashmap-internals.md for the measured numbers).
public class ResizeDemo {
    public static void main(String[] args) {
        int n = 2_000_000;
        boolean presized = args.length > 0 && args[0].equalsIgnoreCase("presized");

        long start = System.nanoTime();
        Map<Integer, Integer> map = presized
                ? new HashMap<>((int) (n / 0.75f) + 1)   // sized so the resize threshold is never crossed
                : new HashMap<>();                        // default cap 16 -- resizes repeatedly while growing
        for (int i = 0; i < n; i++) {
            map.put(i, i);
        }
        long elapsed = System.nanoTime() - start;

        System.out.printf("%s HashMap, n=%,d: %.2f ms%n",
                presized ? "pre-sized" : "default-capacity", n, elapsed / 1_000_000.0);
    }
}
