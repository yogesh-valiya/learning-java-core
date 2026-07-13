import java.util.*;

public class WildcardsDemo {

    // PECS: src PRODUCES T for us to read  -> extends
    //       dest CONSUMES T that we write   -> super
    static <T> void copy(List<? super T> dest, List<? extends T> src) {
        for (T item : src) {
            dest.add(item);
        }
    }

    // Producer only: we just read Numbers out. "? extends Number" accepts
    // List<Integer>, List<Double>, List<Number>, anything that IS-A Number.
    static double sum(List<? extends Number> nums) {
        double total = 0;
        for (Number n : nums) {
            total += n.doubleValue();
        }
        return total;
    }

    // Consumer only: we just write Integers in. "? super Integer" accepts
    // List<Integer>, List<Number>, List<Object>, anything Integer IS-A-subtype-of.
    static void fillWithOneToFive(List<? super Integer> dest) {
        for (int i = 1; i <= 5; i++) {
            dest.add(i);
        }
    }

    public static void main(String[] args) {
        List<Integer> ints = new ArrayList<>(List.of(1, 2, 3));
        List<Double> doubles = new ArrayList<>(List.of(1.5, 2.5));

        System.out.println("1) sum(ints) = " + sum(ints));
        System.out.println("2) sum(doubles) = " + sum(doubles));

        List<Number> numberBucket = new ArrayList<>();
        copy(numberBucket, ints);
        copy(numberBucket, doubles);
        System.out.println("3) numberBucket = " + numberBucket);

        List<Object> objectBucket = new ArrayList<>();
        fillWithOneToFive(objectBucket);
        System.out.println("4) objectBucket = " + objectBucket);
    }
}
