import java.util.List;

public class GenericsBasicsDemo {

    // Generic class — T is a placeholder filled in at the use site.
    static class Box<T> {
        private T value;
        Box(T value) { this.value = value; }
        T get() { return value; }
        void set(T value) { this.value = value; }
    }

    // Generic method — its own <T>, independent of any class's type parameter.
    static <T> T firstOf(List<T> list) {
        return list.get(0);
    }

    // Bounded type parameter — T must be a Comparable-to-itself.
    // The bound isn't just a restriction — it's what LETS us call .compareTo() below.
    static <T extends Comparable<T>> T max(T a, T b) {
        return a.compareTo(b) >= 0 ? a : b;
    }

    public static void main(String[] args) {
        Box<String> box = new Box<>("hello");
        System.out.println("1) " + box.get().toUpperCase());

        List<Integer> nums = List.of(3, 1, 4, 1, 5);
        System.out.println("2) " + firstOf(nums));

        System.out.println("3) " + max(7, 3));
        System.out.println("4) " + max("banana", "apple"));

        Box<Integer> intBox = new Box<>(10);
        intBox.set(20);
        System.out.println("5) " + intBox.get());
    }
}
