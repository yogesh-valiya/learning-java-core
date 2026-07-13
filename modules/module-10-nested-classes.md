# Module 10 — Nested & Anonymous Classes

> Phase 1 — Core language. Priority: **Medium**. Needed to read real Java and to understand the pre-lambda idiom (sets up Phase 3 lambdas).

Companion demo: [`module-10-nested-classes/NestedDemo.java`](./module-10-nested-classes/NestedDemo.java)

---

## 1. The four kinds

Java has four classes-inside-things. The distinction that matters most is **static nested vs inner (non-static)**.

### 1. Static nested class

A `static` class inside another — essentially a **top-level class living in another's namespace**, with **no link to any outer instance**.

```java
class Outer {
    static class StaticNested {
        // cannot access Outer's instance fields — there is no outer instance
    }
}
Outer.StaticNested x = new Outer.StaticNested();   // no Outer instance needed
```

Use it to group a helper with its owner. `Map.Entry` is the canonical example.

### 2. Inner class (non-static nested)

A non-static class inside another. Each inner instance holds a **hidden implicit reference to an outer instance**, so it can access the outer's instance members — but it **requires an outer instance to exist**:

```java
class Outer {
    private String field = "hi";
    class Inner {
        String read() { return field; }   // reads Outer's instance field
    }
}
Outer o = new Outer();
Outer.Inner in = o.new Inner();   // special syntax — needs an outer instance
```

The `o.new Inner()` syntax is the tell: an inner class can't exist without its outer.

### 3. Local class

A class declared **inside a method**, scoped to that method. Rare directly, but the conceptual parent of anonymous classes.

### 4. Anonymous class

A class + instance declared **inline with no name**, usually implementing an interface or extending a class on the spot:

```java
Runnable r = new Runnable() {          // "new Interface() { body }"
    public void run() { System.out.println("running"); }
};
```

Before Java 8 lambdas, this was **how you passed behavior** — listeners, `Comparator`s, `Runnable`s. A lambda is essentially a compact anonymous class for a single-method (functional) interface — the bridge to Phase 3.

---

## 2. Variable capture — "effectively final"

Local and anonymous classes can use local variables from the enclosing method, but only if they are **`final` or effectively final** (assigned once, never reassigned):

```java
void make(String prefix) {                         // prefix is effectively final
    Runnable r = () -> System.out.println(prefix); // captured OK
    // prefix = "x";  // would break effectively-final → compile error
}
```

### Captured local vs outer instance field (important distinction)

- A **captured local variable** is **copied** into the class at capture time → it is **frozen**. Later changes to the original don't affect it.
- An **outer instance field** is accessed **live** through the hidden outer reference → it reflects the **current** value at call time.

Demonstrated in `NestedDemo`: an anonymous `Runnable` captures `prefix="hello"` and also reads `outerField`. After creating it, `outerField` is changed to `"CHANGED"`, then `run()` is called:

```
anon: hello | outerField=CHANGED
```

`prefix` stayed `hello` (frozen copy); `outerField` became `CHANGED` (live read).

---

## 3. Under the hood

- The compiler generates separate `.class` files: `Outer$StaticNested.class`, `Outer$Inner.class`, and anonymous classes get numbers — `Outer$1.class`, `Outer$2.class`.
- An **inner class gets a synthetic field** holding the outer reference (`Outer.this`). A **static nested class does not**.
- Captured local variables are copied into **synthetic fields** of the local/anonymous class — which is *why* they must be effectively final: the class holds a copy, so allowing reassignment would be inconsistent/unsafe.

---

## 4. The memory-leak gotcha

Because an inner-class instance holds a hidden reference to its outer instance, **if the inner instance outlives the outer, it keeps the entire outer object alive** — the outer can't be garbage collected. Classic cases: a non-static inner class used as a long-lived listener, callback, or a returned `Iterator`/`Runnable` stashed somewhere.

> **Fix:** if a nested class doesn't need the outer instance, make it **`static`**. *Effective Java*: "prefer static nested classes over non-static." This is the key takeaway of the module.

---

## 5. PHP contrast

- PHP has **anonymous classes** (`new class implements Foo { ... }`) since PHP 7 — similar to Java's. PHP **closures** (`function() use ($x) {}`) are the closer analog for passing behavior, and `use ($x)` mirrors Java's capture (by value by default).
- PHP has no real **class-in-class nesting** or inner-class/outer-instance binding — so static-nested vs inner is a Java-specific concept.

---

## 6. Interview checklist

- **Static nested vs inner:** static = no outer reference; inner = hidden outer reference, needs `outer.new Inner()`.
- **Why inner classes leak memory:** they pin the outer instance alive → prefer `static` when the outer isn't needed.
- **Anonymous classes** = pre-lambda way to pass behavior; a lambda ≈ anonymous impl of a functional interface.
- **Effectively final:** captured locals must be assigned once; they're copied into synthetic fields.
- **Captured local (frozen copy) vs outer field (live read).**
- **Generated files:** `Outer$Inner.class`, `Outer$1.class` (anonymous).
