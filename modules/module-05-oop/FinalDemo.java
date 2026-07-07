import java.util.ArrayList;
import java.util.List;

public class FinalDemo {
    public static void main(String[] args) {

        // final reference to a mutable object
        final List<String> list = new ArrayList<>();
        list.add("hi");
        list.add("there");
        System.out.println("mutated final list: " + list);   // works fine

        // The next line is COMMENTED OUT because it does not compile.
        // Uncomment it to see the compiler error, then re-comment.
        // list = new ArrayList<>();   // ❌ cannot assign a value to final variable 'list'

        // final primitive: assign-once
        final int x = 10;
        // x = 20;                     // ❌ would not compile
        System.out.println("final int x: " + x);

        // "blank final" — declared final, assigned exactly once, later
        final String name;
        if (args.length > 0) {
            name = args[0];
        } else {
            name = "default";
        }
        System.out.println("blank final name: " + name);
    }
}
