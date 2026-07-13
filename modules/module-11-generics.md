# Module 11 — Generics

> Phase 2 — Generics & Collections. Priority: **High**. The interview-internals gateway to the rest of this phase — Collections, wildcards, and erasure gotchas all build on this module.

Companion demos:
- [`module-11-generics/GenericsBasicsDemo.java`](./module-11-generics/GenericsBasicsDemo.java)
- [`module-11-generics/WildcardsDemo.java`](./module-11-generics/WildcardsDemo.java)
- [`module-11-generics/ErasureDemo.java`](./module-11-generics/ErasureDemo.java)

⚠️ **PHP has no generics.** No `List<int>`, nothing enforced by a runtime or compiler (PHPStan/Psalm `@template` annotations are static-analysis-only and erased completely). This module is genuinely new mechanics, not new syntax for an old concept.

---

## 1. Why generics exist

Pre-Java-5, collections stored raw `Object`:

```java
List list = new ArrayList();
list.add("hello");
list.add(42);                   // compiles fine — no type checking at all
String bad = (String) list.get(1); // compiles, blows up at RUNTIME: ClassCastException
```

The bug surfaces **far from its cause** — at the cast, not at the bad `add()`. Generics move the error to **compile time**:

```java
List<String> list = new ArrayList<>();
list.add("hello");
list.add(42);          // ← compile error, right here
String s = list.get(0); // no cast needed
```

**The whole point: compile-time type safety + no manual casts.**

---

## 2. Generic classes & methods

```java
class Box<T> {
    private T value;
    Box(T value) { this.value = value; }
    T get() { return value; }
    void set(T value) { this.value = value; }
}

Box<String> box = new Box<>("hello");   // T resolved to String at the use site
```

`T` is a **type parameter** — a placeholder resolved when you write `Box<String>`. Naming convention: `T` (Type), `E` (Element — `List<E>`), `K`/`V` (Key/Value — `Map<K,V>`), `N` (Number), `R` (Return).

A **generic method** declares its own `<T>`, independent of any class's type parameter:

```java
static <T> T firstOf(List<T> list) {
    return list.get(0);
}
```

The call site never states `T` explicitly — it's **inferred** from the argument (`firstOf(nums)` infers `T=Integer`).

---

## 3. Bounded type parameters

```java
static <T extends Comparable<T>> T max(T a, T b) {
    return a.compareTo(b) >= 0 ? a : b;
}
```

`extends Comparable<T>` restricts `T` to types comparable to themselves. The bound isn't just a restriction — **it's what grants the method call**. Without it, `T` defaults to plain `Object` (every unbounded `<T>` is shorthand for `<T extends Object>`), and `Object` has no `.compareTo()` — the line wouldn't compile at all.

- `extends` does double duty for both classes and interfaces in a bound — there's no `implements` keyword here.
- Multiple bounds: `<T extends Comparable<T> & Serializable>` — class first (if any), then interfaces, joined with `&`.
- `<T extends Comparable<T>>` is a **self-referential / recursive generic bound** — not just a teaching example; the JDK itself declares `Enum<E extends Enum<E>>` this way.

---

## 4. Invariance & wildcards

`Integer IS-A Number`, but `List<Integer>` is **NOT** a `List<Number>` — generics are **invariant** by default:

```java
List<Integer> ints = new ArrayList<>();
List<Number> nums = ints;   // compile error — does not compile, period
```

If it did compile: `nums.add(3.14)` would be legal (`3.14` IS a `Number`), silently corrupting what's actually a `List<Integer>` — a hole in compile-time safety. Java just refuses the assignment outright.

**Wildcards** let a method accept a family of parameterizations instead of one exact type:

| Wildcard | Means | Read | Write |
|---|---|---|---|
| `List<? extends Number>` | unknown subtype of Number | ✅ as `Number` | ❌ (might secretly be `List<Integer>`) |
| `List<? super Integer>` | unknown supertype of Integer | ⚠️ only as `Object` | ✅ `Integer` |
| `List<?>` | totally unknown type | ⚠️ only as `Object` | ❌ (except `null`) |

**`extends` buys safe reads and blocks writes; `super` buys safe writes and cripples reads.** You never get both fully open on one wildcard — that's the type system working as intended, not a limitation to route around.

---

## 5. PECS — Producer Extends, Consumer Super

The rule (Joshua Bloch, *Effective Java*) for choosing the wildcard direction:

- Structure **produces** values you **read** → `? extends`
- Structure **consumes** values you **write** → `? super`

Canonical JDK example, `Collections.copy`:

```java
static <T> void copy(List<? super T> dest, List<? extends T> src)
```

`src` is only ever read (produces) → `extends`. `dest` is only ever written (consumes) → `super`. Swapping them breaks both directions at once: `? super T` iterated as `T` only yields `Object`; `? extends T` never accepts `.add()`.

---

## 6. Type erasure

Generics are **compile-time only**. The compiler type-checks using `T`, inserts casts where needed, then **erases** all generic type info from the bytecode.

**Erasure rule:** unbounded `T` erases to `Object`; a bounded `<T extends Foo>` erases to `Foo` (leftmost bound if several). So `Box<String>` and `Box<Integer>` — and `List<String>` / `List<Integer>` — are **the same runtime `.class`**:

```java
Box<String> stringBox = new Box<>("a");
Box<Integer> intBox = new Box<>(1);
stringBox.getClass() == intBox.getClass();   // true
```

**Why erase at all?** Backward compatibility — Java 5 added generics without breaking every `.class` file and JVM already in existence. To the JVM, there was never a generic type to begin with.

---

## 7. Consequences of erasure

| You can't... | Because... |
|---|---|
| `new T()` | No runtime type info left to instantiate. |
| `instanceof List<String>` | Only `instanceof List<?>` / raw `List` compiles — the `<String>` part doesn't exist at runtime to check. |
| `new T[10]` / `new List<String>[10]` | Arrays enforce element type on every store at runtime; generics don't. Allowing it would let incompatible types in with no check → **heap pollution**. |
| Overload `f(List<String>)` and `f(List<Integer>)` | Both erase to `f(List)` — identical signature, compile error. |
| Use the class's `T` in a `static` member | `static` belongs to the class itself, not to any one parameterized instance — there's no instantiation, so no `T` to refer to. |

**Bridge methods:** overriding a generic method with a more specific parameter type makes the compiler generate a synthetic bridge so polymorphism survives erasure:

```java
class MyThing implements Comparable<MyThing> {
    public int compareTo(MyThing other) { ... }   // what you write
    // compiler ALSO emits: public int compareTo(Object o) { return compareTo((MyThing) o); }
}
```

`Comparable<T>` erases to `compareTo(Object)`; without the bridge, calling through a `Comparable` reference would find no matching override. Visible via `javap -p` on any class implementing a generic interface.

---

## 8. Raw types — the classic gotcha

A **raw type** (no `<>`) opts completely out of generics checking. Legal only for backward compatibility — never write one in new code.

```java
Box raw = new Box("safe");
raw.set(42);                 // compiles — erasure means Box's methods just take/return Object

Box<String> sneaky = raw;    // unchecked warning, but COMPILES

String s = sneaky.get();     // throws ClassCastException — HERE, not at raw.set(42)
```

The compiler inserts an invisible `(String)` cast around the erased `get()` call **at this call site**, based on `sneaky`'s declared type `Box<String>`. At runtime that cast meets an actual `Integer` → `ClassCastException`, thrown at the **read**, nowhere near where `42` was actually stored. This is the exact Object-casting failure mode generics exist to prevent (§1) — raw types let it back in through the side door: **the crash surfaces far from its real cause.**

---

## 9. PHP contrast

- PHP has no generics and no erasure — there was never a compile-time generic type to erase.
- The closest emotional equivalent: a loosely-typed array meant to hold one shape of data that silently accepts anything, then blows up in a distant `foreach` instead of where the bad value went in — same "failure far from cause" shape as the raw-type gotcha above.
- PHPStan/Psalm `@template` phpdoc generics are the nearest tool-level analog — but advisory only, never enforced at runtime, and invisible to the PHP engine itself.

---

## 10. Interview checklist

- **Why generics:** move type errors from runtime (`ClassCastException`, far from the bug) to compile time (right at the bad call).
- **Bound = permission, not just restriction:** `<T extends Comparable<T>>` is what lets the method body call `.compareTo()` — without it, `T` defaults to `Object`.
- **Invariance:** `List<Integer>` is not a `List<Number>` — prevents smuggling the wrong type in through a supertype reference.
- **PECS:** Producer `extends`, Consumer `super` — say this exact phrase.
- **Type erasure:** generics are compile-time only, erased for backward compatibility with pre-Java-5 bytecode; `Box<String>` and `Box<Integer>` share one runtime class.
- **Consequences of erasure:** no `new T()`, no `instanceof List<String>`, no generic arrays, no overloads differing only by type parameter, no `T` in `static` context.
- **Bridge methods:** synthetic compiler-generated overloads that keep polymorphism working after erasure flattens a generic method's signature.
- **Raw types:** bypass all generic checks; the resulting `ClassCastException` fires at the **read** site (where the compiler inserted the cast), not at the point where the bad value was stored.
