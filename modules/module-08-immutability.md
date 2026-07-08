# Module 8 — Building Immutable Classes

> Phase 1 — Core language. Priority: **Medium**. A common design task ("write an immutable class"). The practical payoff of `final`, defensive copying, and `equals`/`hashCode`.

Companion demo: [`module-08-immutability/ImmutableDemo.java`](./module-08-immutability/ImmutableDemo.java)

---

## 1. The recipe — 5 rules

To make a class immutable (state can never change after construction):

1. **Make the class `final`** — so no subclass can override methods or add mutable behavior that breaks invariants.
2. **All fields `private final`** — private (no external access) + final (assign-once).
3. **No setters** — and no method that mutates state.
4. **Initialize everything in the constructor.**
5. **Defensively copy mutable fields** — on the way *in* (constructor) *and* on the way *out* (getters).

Rules 1–4 are mechanical. Rule 5 is the crux and the source of most bugs.

---

## 2. Why immutability matters

- **Thread-safe for free** — no synchronization, ever (nothing can change → no races).
- **Safe hash keys** — can't strand themselves in a `HashMap` (Module 7's mutable-key trap can't happen).
- **No defensive checks everywhere** — pass the object around freely.
- **Cacheable / shareable** — like the String pool and Integer cache.
- **Security** — safe across trust boundaries.

---

## 3. The core problem: leaking mutable state

If a field is a **mutable object** (`Date`, `List`, array, mutable custom class), `final` alone is **not enough** — `final` freezes the *reference*, not the *object* (Module 5). There are two leak points:

### Leak A — the constructor

```java
final class BrokenPeriod {
    private final Date start;
    BrokenPeriod(Date start) { this.start = start; }   // stores caller's reference
}
Date d = new Date();
BrokenPeriod p = new BrokenPeriod(d);
d.setTime(0);   // caller mutates p's internal state
```

### Leak B — the getter

```java
    Date getStart() { return start; }   // hands out internal reference
}
p.getStart().setTime(0);   // caller mutates p's internal state
```

### The fix — defensive copies at both points

```java
final class SafePeriod {
    private final Date start;
    SafePeriod(Date start) { this.start = new Date(start.getTime()); }  // copy IN
    Date getStart()        { return new Date(start.getTime()); }         // copy OUT
}
```

The object holds its own private copy nobody else references, and every getter hands out a fresh copy.

---

## 4. Collections — nuances

- **`List.copyOf(input)`** (Java 10+) — a **true independent immutable copy**. Best default for a collection field.
- **`Collections.unmodifiableList(input)` is NOT a copy** — it's a read-only **view** wrapping the original. If anyone keeps a reference to the original, they can still mutate it and the view reflects the change. Only `copyOf` (or `new ArrayList<>(input)`) decouples.
- **Shallow vs deep:** copying the *list* protects the structure, but mutable *elements* are still shared. `List<String>` is fully safe (Strings immutable); `List<Date>` would need each `Date` copied for full protection. Keep elements immutable and stay shallow.

---

## 5. Two more interview points

- **Defensively copy BEFORE validating.** If you validate the caller's object then store it, a malicious/mutable arg could change between the check and the store (a TOCTOU race). Correct order: **copy first, validate the copy.**
- **Primitives, wrappers, and `String` need no defensive copy** — already immutable. Only copy genuinely mutable fields. Arrays are always mutable → use `arr.clone()` / `Arrays.copyOf`.

---

## 6. The records nuance

A **record** gives `final` fields and no setters — but does **NOT** auto-defensively-copy mutable components:

```java
record Period(Date start) {}   // still leaks — start is the caller's Date
```

Fix with a **compact constructor** that copies:

```java
record Period(Date start) {
    Period {                                   // compact constructor
        start = new Date(start.getTime());     // copy IN
    }
}
```

Records handle rules 1–4 for free; **rule 5 (defensive copying) is still your responsibility** for mutable components.

---

## 7. PHP contrast

- PHP historically lacked a strong immutability idiom. **PHP 8.1 `readonly` properties** (assign-once) are the closest analog to `final` fields, but PHP has no built-in defensive-copying convention (you'd `clone` manually).
- Java's approach is explicit, manual defensive copying — more ceremony, airtight result.

---

## 8. Interview checklist

- **The 5-rule recipe** — `final` class, `private final` fields, no setters, construct fully, defensive copies.
- **Why `final` alone isn't enough** — it freezes the reference, not the object.
- **Two leak points** — constructor (copy in) and getter (copy out).
- **`List.copyOf` vs `Collections.unmodifiableList`** — real copy vs view.
- **Shallow vs deep** copying for mutable elements.
- **Copy before validate** (TOCTOU).
- **Records don't auto-copy mutable components** — add a compact constructor.
