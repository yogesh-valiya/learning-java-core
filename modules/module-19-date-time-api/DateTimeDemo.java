import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class DateTimeDemo {

    public static void main(String[] args) {
        // 1) Immutability -- same rule as String (Module 4).
        LocalDate date = LocalDate.of(2026, 1, 15);
        date.plusDays(10);                 // return value discarded -- does nothing visible
        System.out.println("1) after discarded plusDays(10), date is still: " + date);
        date = date.plusDays(10);          // must reassign
        System.out.println("2) after reassigning, date is: " + date);

        // 2) Period is calendar-aware: adding "1 month" to Jan 31 clamps to Feb's actual length.
        LocalDate jan31 = LocalDate.of(2026, 1, 31);
        LocalDate plusOneMonth = jan31.plus(Period.ofMonths(1));
        System.out.println("3) Jan 31 2026 + Period.ofMonths(1) = " + plusOneMonth);

        // 3) Duration is a fixed, exact time span -- no concept of "a month" at all.
        LocalDateTime start = LocalDateTime.of(2026, 1, 31, 9, 0);
        LocalDateTime plus24h = start.plus(Duration.ofDays(1));
        System.out.println("4) Jan 31 09:00 + Duration.ofDays(1) = " + plus24h);

        // 4) DateTimeFormatter round-trip.
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        String text = plusOneMonth.format(fmt);
        LocalDate reparsed = LocalDate.parse(text, fmt);
        System.out.println("5) formatted: " + text + " -> reparsed: " + reparsed
                + " -> equal to original: " + reparsed.equals(plusOneMonth));

        // 5) ChronoUnit.between (single-unit count) vs Period.between (calendar breakdown).
        LocalDate a = LocalDate.of(2023, 1, 10);
        LocalDate b = LocalDate.of(2026, 7, 13);
        long daysBetween = ChronoUnit.DAYS.between(a, b);
        Period between = Period.between(a, b);
        System.out.println("6) ChronoUnit.DAYS.between: " + daysBetween + " days");
        System.out.println("7) Period.between: " + between.getYears() + "y "
                + between.getMonths() + "m " + between.getDays() + "d");
    }
}
