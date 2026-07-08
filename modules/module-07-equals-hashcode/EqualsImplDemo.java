import java.util.Objects;

public class EqualsImplDemo {

    // ---- Canonical hand-written value class (final + getClass) ----
    static final class Point {
        private final int x, y;
        Point(int x, int y) { this.x = x; this.y = y; }
        @Override public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Point p = (Point) o;
            return x == p.x && y == p.y;
        }
        @Override public int hashCode() { return Objects.hash(x, y); }
        @Override public String toString() { return "Point(" + x + ", " + y + ")"; }
    }

    // ---- Record: compiler generates equals/hashCode/toString/accessors/constructor ----
    record PointRecord(int x, int y) {}

    // ---- instanceof-based equals + subclass adding a field → ASYMMETRY ----
    static class P {
        final int x, y;
        P(int x, int y) { this.x = x; this.y = y; }
        @Override public boolean equals(Object o) {
            if (!(o instanceof P)) return false;      // instanceof (lenient)
            P p = (P) o;
            return x == p.x && y == p.y;
        }
        @Override public int hashCode() { return Objects.hash(x, y); }
    }
    static class ColorP extends P {
        final String color;
        ColorP(int x, int y, String c) { super(x, y); this.color = c; }
        @Override public boolean equals(Object o) {
            if (!(o instanceof ColorP)) return false;
            ColorP c = (ColorP) o;
            return x == c.x && y == c.y && color.equals(c.color);
        }
    }

    public static void main(String[] args) {
        System.out.println("1) toString       = " + new Point(1, 2));
        System.out.println("2) point equals   = " + new Point(1, 2).equals(new Point(1, 2)));

        System.out.println("3) record toString= " + new PointRecord(1, 2));                       // predict?
        System.out.println("4) record equals  = " + new PointRecord(1, 2).equals(new PointRecord(1, 2))); // predict?

        P p = new P(1, 2);
        ColorP cp = new ColorP(1, 2, "RED");
        System.out.println("5) p.equals(cp)   = " + p.equals(cp));   // predict? (base checks only x,y)
        System.out.println("6) cp.equals(p)   = " + cp.equals(p));   // predict? (subclass wants color too)
    }
}
