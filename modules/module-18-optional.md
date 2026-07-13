# Module 18 — Optional

> Phase 3 — Modern Functional Java. Priority: **Medium**.

Companion demo: [`module-18-optional/OptionalDemo.java`](./module-18-optional/OptionalDemo.java)

---

## 1. What `Optional` is actually for

`Optional<T>` exists specifically as a **return type** that makes "this might have no result" **visible in the method signature**:

```java
Employee findById(int id);            // might return null -- invisible, easy to forget to check
Optional<Employee> findById(int id);  // the signature itself says "might be absent"
```

Returning `null` is a silent contract. Returning `Optional<T>` forces the caller to consciously deal with absence before getting at the value underneath. The design goal is not "eliminate null-checking" — it's "make the possibility of absence impossible to ignore by accident."

## 2. Creating one

- `Optional.of(value)` — wraps a value certain to be non-null; throws `NullPointerException` immediately if it's actually null (fail fast).
- `Optional.ofNullable(value)` — wraps a value that might legitimately be null; produces empty instead of throwing. Right choice when bridging from a nullable source.
- `Optional.empty()` — explicitly empty.

## 3. Consuming one — the anti-pattern, and the actual point

**The common mistake:**

```java
if (opt.isPresent()) {
    Employee e = opt.get();
    System.out.println(e.name());
}
```

This is a null-check with extra ceremony — it discards the entire reason `Optional` exists. The functional-style methods are the point:

```java
opt.map(Employee::name).ifPresent(System.out::println);
String name = opt.map(Employee::name).orElse("unknown");
Employee e = opt.orElseThrow(() -> new NoSuchElementException("not found"));
opt.ifPresentOrElse(e -> process(e), () -> log("nothing found"));   // Java 9+
```

## 4. The `orElse` vs `orElseGet` gotcha — eager vs lazy

```java
Employee e1 = opt.orElse(loadDefaultFromDatabase());         // loadDefaultFromDatabase() ALWAYS runs
Employee e2 = opt.orElseGet(() -> loadDefaultFromDatabase()); // only runs if opt is actually empty
```

`orElse` takes a **plain value** — evaluated eagerly every time, present or not, because Java evaluates method arguments before the call happens. Confirmed in `OptionalDemo.java`: calling `.orElse(expensiveFallback())` on an already-**present** `Optional` still printed `"expensiveFallback(orElse) actually ran"`; the `.orElseGet(() -> expensiveFallback())` call on the same present `Optional` printed nothing — the supplier was never invoked. If the fallback is expensive (a DB hit, a network call), `orElse` pays for it unconditionally. Real performance/correctness bug, not a style nitpick.

## 5. Common mistakes, beyond the two above

- **`Optional` as a field or method parameter.** Designed for return types only. As a field, it adds pointless indirection (and isn't even `Serializable`). As a parameter, it forces callers to wrap arguments awkwardly for no benefit.
- **Calling `.get()` without checking presence.** Throws `NoSuchElementException` (confirmed) — silently reintroduces the exact crash-prone pattern `Optional` was built to prevent, under a different exception name.
- **Wrapping a collection in `Optional`** (`Optional<List<Employee>>`). Redundant — an empty `List` is already a good "no results" signal. Return an empty collection, never `null`, never `Optional`-wrapped.
- **Treating `Optional` as a general null-elimination tool** rather than a return-type communication device for "might be absent."

## 6. The refactor it's actually good for

```java
String city = Optional.ofNullable(employee)
        .map(Employee::getAddress)
        .map(Address::getCity)
        .orElse("unknown");
```

Confirmed in the demo: with an address present, the chain resolves to `"Pune"`; with a `null` address, it short-circuits cleanly to `"unknown"` — no nested `if (x != null)` pyramids.

## PHP contrast

PHP 8's nullsafe operator `?->` covers similar ground for chained access (`$employee?->getAddress()?->getCity()`) but it's a null-propagation short-circuit built into the language, not a distinct type — it doesn't force a method signature to declare "this might be absent" the way returning `Optional<T>` does. No standard-library `Optional`/`Option` type exists in PHP.

## Interview checklist

- `Optional` is a **return-type** signal for "might be absent," not a general null replacement.
- `Optional.of` throws on null immediately (fail-fast); `Optional.ofNullable` doesn't.
- `isPresent()` + `get()` is an anti-pattern — use `map`/`filter`/`orElse`/`orElseThrow`/`ifPresentOrElse`.
- `orElse(x)` evaluates `x` eagerly, always; `orElseGet(supplier)` only evaluates on empty.
- Never use `Optional` as a field or method parameter; never wrap a collection in one.
- `.get()` on empty throws `NoSuchElementException` — the same crash pattern under a new name.
