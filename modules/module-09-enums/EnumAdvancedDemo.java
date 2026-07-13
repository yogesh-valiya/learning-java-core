import java.util.EnumMap;
import java.util.EnumSet;

public class EnumAdvancedDemo {

    // Constant-specific method bodies via an abstract method
    enum Operation {
        PLUS  { public int apply(int a, int b) { return a + b; } },
        MINUS { public int apply(int a, int b) { return a - b; } },
        TIMES { public int apply(int a, int b) { return a * b; } };
        public abstract int apply(int a, int b);
    }

    // Enum implementing an interface
    interface Describable { String describe(); }
    enum Status implements Describable {
        ACTIVE, INACTIVE;
        public String describe() { return "Status:" + name(); }
    }

    enum Day { MON, TUE, WED, SAT, SUN }

    public static void main(String[] args) {
        System.out.println("1) PLUS  3,4 = " + Operation.PLUS.apply(3, 4));   // ?
        System.out.println("2) TIMES 3,4 = " + Operation.TIMES.apply(3, 4));  // ?
        System.out.println("3) " + Status.ACTIVE.describe());                 // ?

        EnumMap<Day, String> plans = new EnumMap<>(Day.class);
        plans.put(Day.WED, "gym");
        plans.put(Day.MON, "code");
        System.out.println("4) EnumMap iteration = " + plans);  // predict ORDER of keys

        EnumSet<Day> weekend = EnumSet.of(Day.SAT, Day.SUN);
        EnumSet<Day> workdays = EnumSet.complementOf(weekend);
        System.out.println("5) workdays = " + workdays);        // predict contents
    }
}
