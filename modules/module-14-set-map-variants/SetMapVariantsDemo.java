import java.util.*;

public class SetMapVariantsDemo {

    public static void main(String[] args) {
        List<String> input = List.of("delta", "alpha", "charlie", "bravo", "alpha");

        Set<String> hashSet = new HashSet<>(input);
        Set<String> linkedHashSet = new LinkedHashSet<>(input);
        Set<String> treeSet = new TreeSet<>(input);

        System.out.println("1) HashSet       (unspecified order): " + hashSet);
        System.out.println("2) LinkedHashSet (insertion order):   " + linkedHashSet);
        System.out.println("3) TreeSet       (sorted order):      " + treeSet);

        // A 3-entry LRU cache in a few lines, via LinkedHashMap's access-order mode.
        Map<Integer, String> lru = new LinkedHashMap<>(16, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<Integer, String> eldest) {
                return size() > 3;
            }
        };
        lru.put(1, "a");
        lru.put(2, "b");
        lru.put(3, "c");
        lru.get(1);       // touch key 1 -> becomes most-recently-used
        lru.put(4, "d");  // over capacity -> evicts the LEAST-recently-used (key 2)

        System.out.println("4) LRU cache keys after touching 1, then adding 4: " + lru.keySet());
    }
}
