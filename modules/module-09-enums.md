# Module 9 — Enums (real Java enums) + EnumMap/EnumSet

> Phase 1 — Core language. Priority: **Medium**. Java enums are full classes, not named integers.

Companion demos:
- [`module-09-enums/EnumBasicsDemo.java`](./module-09-enums/EnumBasicsDemo.java)
- [`module-09-enums/EnumAdvancedDemo.java`](./module-09-enums/EnumAdvancedDemo.java)

---

## 1. Enums are real classes

A Java enum is a **full class**, and **each constant is a singleton instance** of it. Enums can have **fields, a constructor, and methods**.

```java
enum Planet {
    MERCURY(3.30e23, 2.44e6),      // each constant calls the constructor
    EARTH  (5.97e24, 6.37e6);

    private final double mass, radius;      // fields
    Planet(double mass, double radius) {    // constructor (implicitly private)
        this.mass = mass; this.radius = radius;
    }
    double gravity() { return 6.674e-11 * mass / (radius * radius); }  // method
}
```

`Planet.EARTH` is a single shared immutable object — not the number `1`.

---

## 2. Built-in methods every enum gets

| Method | Returns |
|--------|---------|
| `name()` | the constant's name as a String (`"EARTH"`) |
| `ordinal()` | zero-based position in declaration order |
| `values()` | array of all constants (declaration order) |
| `valueOf("EARTH")` | the constant with that name; throws `IllegalArgumentException` if none |

- `toString()` defaults to `name()`.
- Enums are `Comparable` (by ordinal) and `Serializable` out of the box.

---

## 3. The `==` rule (reversed for enums)

Because each constant is a **singleton**, compare enums with `==`:

```java
if (day == Day.MONDAY)   // preferred
```

This is the **opposite** of the rule for normal objects. For enums `==` is idiomatic because:
- There is exactly one instance per constant, so identity = value.
- It is **null-safe** (won't NPE if the reference is null).
- It is **compile-checked** (comparing against a different enum type is a compile error; `.equals()` would silently return false).

`.equals()` also works (final, identity-based), but `==` is preferred.

### switch on enums

```java
switch (day) {
    case MONDAY -> "start";      // unqualified — MONDAY, not Day.MONDAY
    default     -> "other";
}
```

---

## 4. Per-constant method bodies (killer feature)

Each constant can override a method with its own body. Declare an `abstract` method; every constant must implement it — a type-safe replacement for `switch`:

```java
enum Operation {
    PLUS  { public int apply(int a, int b) { return a + b; } },
    MINUS { public int apply(int a, int b) { return a - b; } },
    TIMES { public int apply(int a, int b) { return a * b; } };
    public abstract int apply(int a, int b);
}

Operation.PLUS.apply(3, 4);    // 7
Operation.TIMES.apply(3, 4);   // 12
```

**Why it beats `switch`:** adding a new constant **won't compile** until you supply its behavior — the compiler enforces completeness. A `switch(op)` could silently miss the new case. This is the standard "how do you avoid switch statements?" design answer.

---

## 5. Enums can implement interfaces

An enum can't `extend` a class (it already extends `java.lang.Enum`), but it **can implement interfaces**:

```java
interface Describable { String describe(); }
enum Status implements Describable {
    ACTIVE, INACTIVE;
    public String describe() { return "Status:" + name(); }
}
```

---

## 6. EnumMap & EnumSet — efficient specialists

When keys/elements are enum constants, prefer these over `HashMap`/`HashSet`:

- **`EnumMap<K extends Enum, V>`** — a Map keyed by an enum. Internally a **plain array indexed by `ordinal()`** — no hashing, no buckets, no collisions. Fast, compact, and iterates in **declaration order** (not insertion order).
- **`EnumSet<E>`** — a Set of enum values. Internally a **bitvector** (a single `long` for ≤64 constants). Membership/union/intersection are bit operations — very fast.

```java
EnumMap<Day, String> plans = new EnumMap<>(Day.class);
plans.put(Day.WED, "gym");
plans.put(Day.MON, "code");
// iterates as {MON=code, WED=gym}  — declaration order, not insertion order

EnumSet<Day> weekend  = EnumSet.of(Day.SAT, Day.SUN);
EnumSet<Day> workdays = EnumSet.complementOf(weekend);   // everything else
```

**Interview point:** when the key/element type is an enum, `EnumMap`/`EnumSet` are array/bit backed → faster and more memory-efficient than the hash-based collections, and `EnumMap` preserves natural ordering.

---

## 7. Under the hood — the enum singleton pattern

The compiler turns an enum into a `final class extends java.lang.Enum`; each constant is a `public static final` field instantiated once in a static initializer at class load. Consequences:

- You cannot `new` an enum or extend it.
- The singleton guarantee survives **serialization and reflection** — both of which can break a hand-written singleton — so a single-element enum is *Effective Java*'s recommended **Singleton**:

```java
enum Config { INSTANCE;  /* fields + methods */ }
Config.INSTANCE.doSomething();
```

---

## 8. Gotchas

- **Never persist `ordinal()`** to a DB/protocol — reordering or inserting a constant shifts the numbers and corrupts stored data. Persist `name()` or an explicit `code` field.
- **Don't put mutable state in an enum** — constants are shared singletons living for the JVM's lifetime, so a mutable enum field is effectively global mutable state (thread-safety hazard). Keep enum fields `final`.

---

## 9. PHP contrast

- **PHP 8.1+ has real enums** (`enum Suit { case Hearts; }`), including **backed enums** (`enum Status: string { case Active = 'active'; }` with `->value`) — roughly a Java enum with a field. Fairly similar now.
- **Pre-8.1 PHP** used class constants (`const ACTIVE = 1`) — named scalars, **no type safety**. Java enums have been type-safe since 2004.
- Java's edge: per-constant method bodies and richer built-ins.

---

## 10. Interview checklist

- **Enum = full class, constants = singletons** (fields, constructor, methods).
- **`==` is preferred** for enums (singleton, null-safe, compile-checked).
- **Built-ins:** `name`, `ordinal`, `values`, `valueOf`.
- **Per-constant bodies via abstract method** → compiler-enforced completeness (beats `switch`).
- **Enums implement interfaces** (can't extend classes).
- **EnumMap (ordinal array) / EnumSet (bitvector)** — faster than hash collections for enum keys/elements.
- **Never persist `ordinal()`**; keep enum state `final`.
- **Enum singleton** — best Singleton (survives serialization/reflection).
