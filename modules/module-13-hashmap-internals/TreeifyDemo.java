import java.util.*;

public class TreeifyDemo {

    // Deliberately terrible hashCode — but NOTE: still a VALID one. Consistent across
    // calls, and equal keys trivially get equal hashes. It's a distribution problem,
    // not a contract violation (contrast with Pass 1's inconsistent-hashCode scenario).
    static final class BadKey {
        final int id;
        BadKey(int id) { this.id = id; }
        @Override public int hashCode() { return 42; }
        @Override public boolean equals(Object o) {
            return o instanceof BadKey b && b.id == id;
        }
    }

    static final class GoodKey {
        final int id;
        GoodKey(int id) { this.id = id; }
        @Override public int hashCode() { return Objects.hash(id); }
        @Override public boolean equals(Object o) {
            return o instanceof GoodKey g && g.id == id;
        }
    }

    interface KeyFactory<K> { K make(int i); }

    static <K> long lookupAll(Map<K, Integer> map, int n, KeyFactory<K> factory) {
        long sum = 0;
        for (int i = 0; i < n; i++) sum += map.get(factory.make(i));
        return sum;
    }

    static <K> long timeLookups(Map<K, Integer> map, int n, KeyFactory<K> factory) {
        lookupAll(map, n, factory); // warm up JIT
        long start = System.nanoTime();
        lookupAll(map, n, factory);
        return System.nanoTime() - start;
    }

    public static void main(String[] args) {
        int n = 50_000;

        Map<BadKey, Integer> badMap = new HashMap<>();
        Map<GoodKey, Integer> goodMap = new HashMap<>();
        for (int i = 0; i < n; i++) {
            badMap.put(new BadKey(i), i);
            goodMap.put(new GoodKey(i), i);
        }

        long goodTime = timeLookups(goodMap, n, GoodKey::new);
        long badTime = timeLookups(badMap, n, BadKey::new);

        System.out.printf("1) GoodKey (well-distributed) lookups: %8.2f ms%n", goodTime / 1_000_000.0);
        System.out.printf("2) BadKey  (all same hashCode) lookups: %8.2f ms%n", badTime / 1_000_000.0);
        System.out.printf("3) BadKey is ~%.0fx slower than GoodKey (see TreeifyScalingDemo for why " +
                "this is NOT the clean O(log n) a treeified bucket usually promises)%n", (double) badTime / goodTime);
    }
}
