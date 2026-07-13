public class EnumBasicsDemo {

    enum Day { MON, TUE, WED, THU, FRI }

    enum Planet {
        MERCURY(3.30e23, 2.44e6),
        EARTH  (5.97e24, 6.37e6);

        private final double mass, radius;
        Planet(double mass, double radius) { this.mass = mass; this.radius = radius; }
        double gravity() { return 6.674e-11 * mass / (radius * radius); }
    }

    public static void main(String[] args) {
        System.out.println("1) " + Day.MON);                        // toString → ?
        System.out.println("2) " + Day.WED.ordinal());              // ordinal → ?
        System.out.println("3) " + Day.valueOf("FRI"));             // parse by name → ?
        System.out.println("4) " + Day.values().length);            // how many → ?
        System.out.println("5) " + (Day.MON == Day.valueOf("MON"))); // singleton identity → ?
        System.out.printf("6) EARTH gravity = %.2f%n", Planet.EARTH.gravity()); // ≈ ?

        // 7) What happens here?  (predict: value or exception)
        try {
            System.out.println("7) " + Day.valueOf("SUNDAY"));
        } catch (Exception e) {
            System.out.println("7) threw " + e.getClass().getSimpleName());
        }
    }
}
