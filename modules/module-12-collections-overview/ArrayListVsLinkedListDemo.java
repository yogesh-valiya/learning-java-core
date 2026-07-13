import java.util.*;

public class ArrayListVsLinkedListDemo {

    // Classic anti-pattern: index-loop over a List that might not be RandomAccess.
    static long timeIndexLoop(List<Integer> list) {
        long start = System.nanoTime();
        long sum = 0;
        for (int i = 0; i < list.size(); i++) {
            sum += list.get(i);
        }
        long elapsed = System.nanoTime() - start;
        if (sum < 0) System.out.println(sum); // keep JIT from discarding the loop
        return elapsed;
    }

    // Correct pattern: iterator/for-each — O(1) per step regardless of backing structure.
    static long timeIteratorLoop(List<Integer> list) {
        long start = System.nanoTime();
        long sum = 0;
        for (int value : list) {
            sum += value;
        }
        long elapsed = System.nanoTime() - start;
        if (sum < 0) System.out.println(sum);
        return elapsed;
    }

    public static void main(String[] args) {
        int n = 40_000;
        List<Integer> arrayList = new ArrayList<>();
        List<Integer> linkedList = new LinkedList<>();
        for (int i = 0; i < n; i++) {
            arrayList.add(i);
            linkedList.add(i);
        }

        // Warm up the JIT on both shapes before the real measurement.
        timeIndexLoop(arrayList);
        timeIteratorLoop(linkedList);

        double alIndexMs = timeIndexLoop(arrayList) / 1_000_000.0;
        double llIndexMs = timeIndexLoop(linkedList) / 1_000_000.0;
        double llIterMs = timeIteratorLoop(linkedList) / 1_000_000.0;

        System.out.println("1) instanceof RandomAccess - ArrayList: " + (arrayList instanceof RandomAccess));
        System.out.println("2) instanceof RandomAccess - LinkedList: " + (linkedList instanceof RandomAccess));
        System.out.printf("3) ArrayList  get(i) loop:    %8.2f ms%n", alIndexMs);
        System.out.printf("4) LinkedList get(i) loop:    %8.2f ms%n", llIndexMs);
        System.out.printf("5) LinkedList iterator loop:  %8.2f ms%n", llIterMs);
        System.out.printf("6) LinkedList get(i)-loop is ~%.0fx slower than its own iterator loop%n",
                llIndexMs / Math.max(llIterMs, 0.001));
    }
}
