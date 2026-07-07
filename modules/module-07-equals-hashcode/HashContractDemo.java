import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class HashContractDemo {

    // equals() overridden, but hashCode() NOT overridden → breaks the contract
    static class PointBad {
        int x, y;
        PointBad(int x, int y) { this.x = x; this.y = y; }
        @Override public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof PointBad)) return false;
            PointBad p = (PointBad) o;
            return x == p.x && y == p.y;
        }
        // NO hashCode() override — inherits Object's identity hashCode
    }

    // equals() AND hashCode() both overridden → honors the contract
    static class PointGood {
        int x, y;
        PointGood(int x, int y) { this.x = x; this.y = y; }
        @Override public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof PointGood)) return false;
            PointGood p = (PointGood) o;
            return x == p.x && y == p.y;
        }
        @Override public int hashCode() { return Objects.hash(x, y); }
    }

    public static void main(String[] args) {
        // Direct equals still works for both:
        System.out.println("A) badEquals   = " + new PointBad(1, 2).equals(new PointBad(1, 2)));
        System.out.println("B) goodEquals  = " + new PointGood(1, 2).equals(new PointGood(1, 2)));

        // But hash-based collections rely on hashCode():
        Set<PointBad> bad = new HashSet<>();
        bad.add(new PointBad(1, 2));
        System.out.println("C) bad.contains(new PointBad(1,2))   = " + bad.contains(new PointBad(1, 2)));

        Set<PointGood> good = new HashSet<>();
        good.add(new PointGood(1, 2));
        System.out.println("D) good.contains(new PointGood(1,2)) = " + good.contains(new PointGood(1, 2)));

        // Add the "same" logical point twice to each set:
        bad.add(new PointBad(1, 2));
        good.add(new PointGood(1, 2));
        System.out.println("E) bad.size()  = " + bad.size());
        System.out.println("F) good.size() = " + good.size());
    }
}
