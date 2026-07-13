# Module 28 — Modern Java 11–21

> Phase 7 — Rounding Out. Priority: **Medium**. Increasingly asked in interviews. A survey — broader, faster-moving than the deep-dive modules — covering seven features across several Java versions.

Companion demo: [`module-28-modern-java-11-21/ModernJavaDemo.java`](./module-28-modern-java-11-21/ModernJavaDemo.java)

---

## 1. `var` (Java 10) — local type inference, not dynamic typing

```java
var list = new ArrayList<String>();   // inferred as ArrayList<String>, fixed forever
```

Only for local variables with an initializer — never fields, parameters, or return types. **Still static typing** — the compiler infers a concrete type at compile time; reassigning to an incompatible type is a compile error, exactly as if the type had been written out. Confirmed: `var message = "hello";` reported its runtime class as `java.lang.String` — a real, fixed, inferred type, nothing dynamic about it.

## 2. Records (Java 16) — deepening Modules 7–8

```java
record Range(int low, int high) {
    Range {                              // compact constructor -- validates/normalizes only
        if (low > high) throw new IllegalArgumentException("low > high");
    }                                     // field assignment still happens implicitly, after this
}
```

Confirmed: `new Range(1, 10)` succeeded; `new Range(10, 1)` threw `IllegalArgumentException` from the compact constructor. Records are **implicitly `final`**, implicitly extend `java.lang.Record` (can't extend anything else), but **can implement interfaces** and carry additional methods/static members. Use for immutable data carriers; not for mutable entities or inheritance-based polymorphism.

## 3. Sealed classes/interfaces (Java 17)

```java
sealed interface Shape permits Circle, Square, Triangle {}
record Circle(double radius) implements Shape {}
record Square(double side) implements Shape {}
record Triangle(double base, double height) implements Shape {}
```

`permits` fixes the complete implementer set at compile time; every permitted type must be `final`, `sealed`, or `non-sealed`. Payoff: a `switch` over the sealed type can be checked for **exhaustiveness** by the compiler — Module 9's enum-completeness guarantee, generalized to class hierarchies.

## 4. Pattern matching + switch expressions (Java 14/16/21)

```java
static double area(Shape shape) {
    return switch (shape) {
        case Circle(double r)      -> Math.PI * r * r;   // record deconstruction
        case Square(double side)   -> side * side;
        case Triangle(double b, double h) -> 0.5 * b * h;
        // no default -- compiler knows these are the ONLY permitted cases
    };
}
```

Confirmed against all three shapes with correct areas, no `default` arm needed. `instanceof` pattern matching (Java 16) similarly eliminates the redundant cast that used to follow every check. Switch-as-**expression** (Java 14) uses `->` arms with no fall-through (fixing the classic forgotten-`break` bug), `yield` for multi-statement arms, and the same compiler-enforced exhaustiveness over enums/sealed types. Guarded patterns (`case Integer i when i > 0`) add conditions without an `if`-chain inside the arm.

## 5. Text blocks (Java 15)

```java
String json = """
        {
          "name": "%s"
        }""".formatted(name);
```

Confirmed clean multi-line output with incidental whitespace stripped automatically — no more `"line1\n" + "line2\n"` concatenation for embedded JSON/SQL/HTML.

## 6. Virtual threads (Java 21) — overview level

Lightweight, **JVM-managed** threads, not 1:1 with OS threads — millions can exist where platform threads cap out around thousands. Confirmed: 10,000 tasks submitted via `Executors.newVirtualThreadPerTaskExecutor()` all completed (`10000 / 10000`); an unstarted virtual thread's `toString()` reported `VirtualThread[#10023]/new` — a genuinely distinct kind of thread object. The mechanism: when a virtual thread blocks on I/O, the JVM unmounts it from its carrier OS thread and mounts a different one — ordinary blocking-looking code, scaled by the runtime, no reactive rewrite needed. Directly addresses Module 23's `ExecutorService` thread-count caution for I/O-bound workloads specifically (not CPU-bound work, which still needs roughly one thread per core).

## PHP contrast

No PHP analogue for sealed types, record deconstruction, or exhaustive switch checking. PHP 8.1's `readonly` + constructor promotion partially overlaps with records; PHP's `match` expression (8.0) maps closely onto Java's switch expressions (no fall-through, produces a value).

## Interview checklist

- `var` is statically typed, local-only, inferred at compile time — confirmed via runtime class, not dynamic typing.
- Records: compact constructor validates without restating assignments — confirmed throwing on invalid input; implicitly `final`, extend `java.lang.Record`, can implement interfaces.
- Sealed types fix the permitted-subtype set at compile time, enabling compiler-checked exhaustive `switch` — confirmed with zero `default` needed.
- `instanceof`/switch pattern matching: type match + variable binding + record deconstruction + `when` guards.
- Switch expressions: `->` arms, no fall-through, `yield` for multi-statement arms, enforced exhaustiveness.
- Text blocks for multi-line literals with automatic whitespace stripping.
- Virtual threads are JVM-managed, not 1:1 with OS threads — confirmed 10,000 concurrent tasks completing; solves I/O-bound scaling without reactive rewrites, doesn't help CPU-bound work.
