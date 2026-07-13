# Module 19 — Date/Time API (java.time)

> Phase 3 — Modern Functional Java. Priority: **Medium**. Final module of Phase 3.

Companion demo: [`module-19-date-time-api/DateTimeDemo.java`](./module-19-date-time-api/DateTimeDemo.java)

---

## 1. Why `java.time` exists

The original `Date`/`Calendar` classes were a well-known design failure: **mutable**, **not thread-safe**, and riddled with surprises — most infamously, `Calendar` months are **0-indexed** (`Calendar.JANUARY == 0`), a legendary bug source. Java 8 introduced `java.time` (JSR-310, built on Joda-Time's philosophy): **immutable, thread-safe, and unambiguous** about what each type represents.

## 2. The core types — each represents exactly one concept

| Type | Represents |
|---|---|
| `LocalDate` | date only (year-month-day), no time, no timezone |
| `LocalTime` | time only, no date, no timezone |
| `LocalDateTime` | date + time, no timezone |
| `ZonedDateTime` | date + time + timezone — a full, unambiguous instant in human terms |
| `Instant` | a single point on the UTC timeline (epoch seconds/nanos) — for computers, not humans |

Pick based on whether the concept actually has a timezone or time-of-day at all. A birthday is a `LocalDate`. A log timestamp is an `Instant`/`ZonedDateTime`. **Months are 1-indexed** here — a deliberate fix of the old `Calendar` bug.

## 3. `Duration` vs `Period` — time-based vs calendar-based

- **`Duration`** — time-based, exact, fixed-length (hours/minutes/seconds/nanos). Pairs with `Instant`/`LocalTime`.
- **`Period`** — date-based, calendar-aware (years/months/days). "1 month" means "same day next month," whatever length that turns out to be. Pairs with `LocalDate`.

Confirmed in `DateTimeDemo.java`: adding `Period.ofMonths(1)` to Jan 31, 2026 gives **Feb 28, 2026** (clamped to February's actual length) — but adding `Duration.ofDays(1)` (a fixed 24 hours) to Jan 31 09:00 gives **Feb 1, 09:00**. Same "1 unit" concept, genuinely different results — `Duration` has no notion of "a month" at all, by design.

## 4. Immutability — the same rule as `String` (Module 4)

Every `java.time` type is immutable. `plusDays`, `withYear`, etc. all return a **new** instance. Confirmed: calling `date.plusDays(10)` without reassigning left `date` completely unchanged; only `date = date.plusDays(10)` updated it. Identical discipline to `String` — forgetting to reassign is a silent no-op.

## 5. Formatting/parsing — and a real danger in the old API

```java
DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd-MM-yyyy");
String text = date.format(fmt);
LocalDate parsed = LocalDate.parse(text, fmt);
```

Confirmed round-trip: format → parse → equals original, `true`. `DateTimeFormatter` is immutable and **thread-safe** — safe as a shared `static final`. This directly fixes a real bug in the old API: `SimpleDateFormat` is mutable and **explicitly not thread-safe** — sharing one instance across threads silently corrupts parse/format results under concurrent access (wrong dates, not a crash). Still a live interview question even though the fix (`java.time`) is what you'd actually use.

## 6. Comparing and measuring

```java
date.isBefore(other); date.isAfter(other); date.isEqual(other);
ChronoUnit.DAYS.between(a, b);   // single-unit count
Period.between(a, b);            // full calendar breakdown
```

Confirmed for `2023-01-10` → `2026-07-13`: `ChronoUnit.DAYS.between` gives a raw **1280 days**; `Period.between` gives the human-shaped **3 years, 6 months, 3 days**. Pick based on whether a single measurement or a readable breakdown is needed.

## PHP contrast

PHP's `DateTime`/`DateTimeImmutable` split mirrors this exactly — `DateTime` is the mutable mistake, `DateTimeImmutable` is the fix, and `DateInterval` covers roughly what `Duration`/`Period` split into two types for. Disciplined `DateTimeImmutable` use in PHP maps closely onto this whole module.

## Interview checklist

- `java.time` (Java 8+) replaced `Date`/`Calendar` for immutability, thread-safety, and clarity; `Calendar`'s 0-indexed months were a real bug source.
- `LocalDate`/`LocalTime`/`LocalDateTime`/`ZonedDateTime`/`Instant` each represent one concept — pick by whether timezone/time-of-day genuinely applies.
- `Duration` = time-based, exact (pairs with `Instant`); `Period` = calendar-based (pairs with `LocalDate`) — not interchangeable, proven by the Feb 28 vs Feb 1 divergence.
- All `java.time` types are immutable — same reassignment discipline as `String`.
- `DateTimeFormatter` is thread-safe; `SimpleDateFormat` is dangerously not.
- `ChronoUnit.between` = single-unit count; `Period.between` = full calendar breakdown.
