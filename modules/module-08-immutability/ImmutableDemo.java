import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ImmutableDemo {

    // ❌ BROKEN: stores and returns the internal Date reference directly
    static final class BrokenPeriod {
        private final Date start;                       // Date is MUTABLE
        BrokenPeriod(Date start) { this.start = start; }
        Date getStart() { return start; }
    }

    // ✅ SAFE: defensive copy on the way in AND out
    static final class SafePeriod {
        private final Date start;
        SafePeriod(Date start) { this.start = new Date(start.getTime()); }  // copy IN
        Date getStart() { return new Date(start.getTime()); }               // copy OUT
    }

    // ✅ SAFE collection: true immutable copy in, unmodifiable out
    static final class Team {
        private final List<String> members;
        Team(List<String> members) { this.members = List.copyOf(members); } // Java 10+ immutable copy
        List<String> getMembers() { return members; }                       // already unmodifiable
    }

    public static void main(String[] args) {
        System.out.println("--- Leak A: mutate via the constructor argument ---");
        Date d = new Date(0);
        BrokenPeriod bp = new BrokenPeriod(d);
        d.setTime(1000);                          // caller mutates the SAME Date object
        System.out.println("Broken start = " + bp.getStart().getTime() + "   (expected 0)");

        SafePeriod sp = new SafePeriod(new Date(0));
        Date d2 = new Date(0);
        SafePeriod sp2 = new SafePeriod(d2);
        d2.setTime(1000);                         // caller mutates its own Date
        System.out.println("Safe   start = " + sp2.getStart().getTime() + "   (expected 0)");

        System.out.println("\n--- Leak B: mutate via the getter's returned reference ---");
        BrokenPeriod bp2 = new BrokenPeriod(new Date(0));
        bp2.getStart().setTime(2000);             // mutate the returned internal Date
        System.out.println("Broken start = " + bp2.getStart().getTime() + "   (expected 0)");

        sp.getStart().setTime(2000);              // mutate the returned COPY (harmless)
        System.out.println("Safe   start = " + sp.getStart().getTime() + "   (expected 0)");

        System.out.println("\n--- Collection immutability ---");
        List<String> src = new ArrayList<>(List.of("A", "B"));
        Team t = new Team(src);
        src.add("C");                             // mutate the source list after construction
        System.out.println("Team size = " + t.getMembers().size() + "   (expected 2)");
        try {
            t.getMembers().add("X");              // try to mutate the returned list
            System.out.println("mutation allowed (BAD)");
        } catch (UnsupportedOperationException e) {
            System.out.println("returned list is unmodifiable (GOOD)");
        }
    }
}
