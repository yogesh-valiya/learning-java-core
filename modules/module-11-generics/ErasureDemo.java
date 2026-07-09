import java.util.*;

public class ErasureDemo {

    static class Box<T> {
        private T value;
        Box(T value) { this.value = value; }
        T get() { return value; }
        void set(T value) { this.value = value; }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static void main(String[] args) {
        // 1) & 2) Erasure proof: <T> is gone at runtime — same .class regardless of T
        Box<String> stringBox = new Box<>("a");
        Box<Integer> intBox = new Box<>(1);
        System.out.println("1) Box<String> and Box<Integer> same class? "
                + (stringBox.getClass() == intBox.getClass()));

        List<String> strings = new ArrayList<>();
        List<Integer> ints = new ArrayList<>();
        System.out.println("2) List<String> and List<Integer> same class? "
                + (strings.getClass() == ints.getClass()));

        // 3) instanceof only works against the unbounded/raw shape — the <String> part is gone
        System.out.println("3) strings instanceof List<?> = " + (strings instanceof List<?>));

        // 4) Raw type: opts OUT of all generics checking. Compiles clean, no warning even needed.
        Box raw = new Box("safe");
        raw.set(42);   // legal! erasure means Box's methods take/return plain Object here

        // Assigning a raw type into a parameterized reference: unchecked warning, but COMPILES.
        Box<String> sneaky = raw;

        // 5) predict: what happens right here?
        try {
            String s = sneaky.get();
            System.out.println("5) " + s);
        } catch (ClassCastException e) {
            System.out.println("5) threw " + e.getClass().getSimpleName() + ": " + e.getMessage());
        }
    }
}
