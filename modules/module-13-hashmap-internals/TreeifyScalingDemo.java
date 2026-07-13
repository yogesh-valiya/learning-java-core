import java.util.*;

// Checks HOW lookup cost scales with n for a fully-degenerate bucket (every key,
// same hashCode, non-Comparable) -- to see whether treeification's usual O(log n)
// guarantee actually holds in this pathological case.
public class TreeifyScalingDemo {

    static final class BadKey {
        final int id;
        BadKey(int id) { this.id = id; }
        @Override public int hashCode() { return 42; } // every key collides
        @Override public boolean equals(Object o) {
            return o instanceof BadKey b && b.id == id;
        }
    }

    public static void main(String[] args) {
        for (int n : new int[]{2000, 4000, 8000, 16000, 32000}) {
            Map<BadKey, Integer> map = new HashMap<>();
            for (int i = 0; i < n; i++) map.put(new BadKey(i), i);

            for (int i = 0; i < n; i++) map.get(new BadKey(i)); // warm up

            long start = System.nanoTime();
            long sum = 0;
            for (int i = 0; i < n; i++) sum += map.get(new BadKey(i));
            long elapsed = System.nanoTime() - start;

            System.out.printf("n=%6d  total=%8.2f ms  per-lookup=%7.3f microsec%n",
                    n, elapsed / 1_000_000.0, (elapsed / 1000.0) / n);
        }
    }
}
