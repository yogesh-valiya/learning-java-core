import java.util.Arrays;

public class EnumBasicsDemo {

    enum Level { LOW, MEDIUM, HIGH }

    public static void main(String[] args) {
        Level l = Level.MEDIUM;

        System.out.println("--- Built-ins ---");
        System.out.println("name()    = " + l.name());
        System.out.println("ordinal() = " + l.ordinal());
        System.out.println("toString  = " + l);
        System.out.println("values()  = " + Arrays.toString(Level.values()));

        System.out.println("\n--- valueOf ---");
        Level parsed = Level.valueOf("HIGH");
        System.out.println("valueOf(\"HIGH\") = " + parsed);
        try {
            Level.valueOf("URGENT");
        } catch (IllegalArgumentException e) {
            System.out.println("valueOf(\"URGENT\") -> IllegalArgumentException (no such constant)");
        }

        System.out.println("\n--- switch on enum ---");
        describe(l);
        describe(Level.LOW);

        System.out.println("\n--- == is SAFE and preferred for enums (each constant is a singleton) ---");
        Level a = Level.HIGH;
        Level b = Level.valueOf("HIGH");
        System.out.println("a == b      -> " + (a == b));       // true: same singleton instance
        System.out.println("a.equals(b) -> " + a.equals(b));    // also true, but == is idiomatic here

        System.out.println("\n--- ordinal() is fragile for persistence ---");
        System.out.println("Today:  LOW=" + Level.LOW.ordinal() + " MEDIUM=" + Level.MEDIUM.ordinal()
                + " HIGH=" + Level.HIGH.ordinal());
        System.out.println("Insert a constant before HIGH, or reorder them, and every ordinal shifts.");
        System.out.println("Anything persisted as that int (DB column, serialized file) now decodes wrong.");
        System.out.println("Rule: persist name() (or an explicit code field) - never ordinal().");
    }

    static void describe(Level level) {
        switch (level) {
            case LOW    -> System.out.println("Low risk");
            case MEDIUM -> System.out.println("Medium risk");
            case HIGH   -> System.out.println("High risk");
        }
    }
}
