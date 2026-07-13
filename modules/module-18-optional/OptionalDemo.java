import java.util.*;

public class OptionalDemo {

    static String expensiveFallback(String tag) {
        System.out.println("   expensiveFallback(" + tag + ") actually ran");
        return "default";
    }

    public static void main(String[] args) {
        Optional<String> present = Optional.of("value");

        System.out.println("1) orElse on a PRESENT optional:");
        String r1 = present.orElse(expensiveFallback("orElse"));       // argument evaluated regardless
        System.out.println("   result: " + r1);

        System.out.println("2) orElseGet on a PRESENT optional:");
        String r2 = present.orElseGet(() -> expensiveFallback("orElseGet")); // supplier only called if empty
        System.out.println("   result: " + r2);

        // Optional.of vs ofNullable with a null value.
        try {
            Optional.of(null);
        } catch (NullPointerException e) {
            System.out.println("3) Optional.of(null) threw: " + e.getClass().getSimpleName());
        }
        System.out.println("4) Optional.ofNullable(null) is empty: " + Optional.ofNullable(null).isEmpty());

        // .get() on empty.
        try {
            Optional.empty().get();
        } catch (NoSuchElementException e) {
            System.out.println("5) empty Optional.get() threw: " + e.getClass().getSimpleName());
        }

        // The chaining refactor: nested null-checks replaced by map().
        record Address(String city) {}
        record Employee(Address address) {}

        Employee withAddress = new Employee(new Address("Pune"));
        Employee noAddress = new Employee(null);

        String city1 = Optional.ofNullable(withAddress).map(Employee::address).map(Address::city).orElse("unknown");
        String city2 = Optional.ofNullable(noAddress).map(Employee::address).map(Address::city).orElse("unknown");
        System.out.println("6) city1 = " + city1 + ", city2 = " + city2);
    }
}
