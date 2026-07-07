# Module 6 — Interfaces vs Abstract Classes

> Phase 1 — Core language. Priority: **High**. A favorite design question. Builds on the overriding/polymorphism from Module 5.

Companion demo: [`module-06-interfaces-abstract/InterfaceDemo.java`](./module-06-interfaces-abstract/InterfaceDemo.java)

---

## 1. Interface — a contract of type and behavior

An interface declares **what** a type can do; a class `implements` it and provides the bodies.

```java
interface Payment {
    void pay(int amount);        // implicitly  public abstract
    int MAX = 100_000;           // implicitly  public static final  (a constant)
}
```

Implicit-modifier rules (common interview bait):

- Interface **methods** are implicitly `public abstract` (pre-Java 8).
- Interface **fields** are implicitly `public static final` — i.e. **constants**. An interface **cannot hold instance state**.
- A class must implement **all** abstract methods, or be declared `abstract` itself.
- An interface may **extend multiple** interfaces.

---

## 2. What Java 8 & 9 added to interfaces

This is where the interface/abstract-class line blurred — know exactly what changed:

- **`default` methods (Java 8)** — a method *with a body* in an interface. Implementors inherit it and may override it.
  ```java
  interface Greeter {
      String name();
      default String greet() { return "Hello, " + name(); }   // has a body
  }
  ```
  **Why added? Backward compatibility.** When Java 8 added `stream()` to `Collection`, every existing `List`/`Set` implementation would have broken if it were a plain abstract method. Making it a `default` method let the JDK **extend an interface without breaking existing implementors**.
- **`static` methods (Java 8)** — utility/factory methods on the interface itself, e.g. `Comparator.naturalOrder()`.
- **`private` methods (Java 9)** — helpers to share code between default methods, hidden from implementors.

Still **not allowed** in an interface: instance fields (state) and constructors. That is the durable dividing line.

---

## 3. Abstract class — a partial implementation with state

```java
abstract class Shape {
    private final String name;                 // instance state — interfaces can't have this
    Shape(String name) { this.name = name; }   // constructor — interfaces can't have this
    abstract double area();                    // subclasses MUST implement
    String describe() { return name + " area=" + area(); }  // shared concrete method
}
```

- **Cannot be instantiated** (`new Shape()` is a compile error) — it's incomplete.
- Can have instance fields, constructors, any access modifiers, and a mix of abstract + concrete methods.
- A subclass `extends` it and implements the abstract methods.

---

## 4. "Multiple inheritance of type"

- A class can **`implement` many interfaces** but **`extend` only one class**:
  ```java
  class C extends Base implements Serializable, Comparable<C>, Runnable { ... }
  ```
- Java bans multiple *class* inheritance to avoid the **diamond problem for state**: if you inherited two classes each with a field `x`, which `x` wins? Ambiguous (C++'s classic headache).
- Interfaces avoid this because (pre-8) they had no state and no bodies — inheriting multiple *contracts* is unambiguous. Hence Java gives "multiple inheritance of **type**" (interfaces) but not "of **state**" (classes).

### The diamond problem with default methods

Once interfaces got method bodies, ambiguity became possible again:

```java
interface A { default String hi() { return "A.hi"; } }
interface B { default String hi() { return "B.hi"; } }
class C implements A, B { /* which hi()? */ }
```

Java's rule: **the compiler forces resolution.** `C` won't compile until it overrides `hi()`, and inside it can pick a parent explicitly with `Interface.super.method()`:

```java
class C implements A, B {
    public String hi() { return A.super.hi() + " + " + B.super.hi(); }
}
```

---

## 5. The comparison table

| | **Interface** | **Abstract class** |
|---|---|---|
| Instance state (fields) | ❌ only `public static final` constants | ✅ yes |
| Constructors | ❌ no | ✅ yes |
| Method bodies | ✅ `default`/`static`/`private` (Java 8/9+) | ✅ concrete methods |
| Multiple inheritance | ✅ implement many | ❌ extend only one |
| Access modifiers on methods | public (by default) | any (`private`, `protected`, …) |
| Relationship modeled | "can-do" / capability | "is-a" / shared identity |
| Fields default to | `public static final` | normal instance fields |

**The crisp differentiator to state in an interview:** *"Even after Java 8 default methods, an abstract class can hold **instance state and constructors**; an interface cannot. If you need shared fields or controlled construction, use an abstract class; if you just need a capability contract — especially across unrelated types or when you need more than one — use an interface."*

---

## 6. When to use which — design guidance

- **Interface** when: modeling a **capability** (`Comparable`, `Runnable`, `Serializable`), unrelated classes share it, you need **multiple**, or you want to enable lambdas (a single-abstract-method interface is a **functional interface** — basis of Module 16).
- **Abstract class** when: several closely-related classes share **common state + code**, there's a strong **"is-a"** relationship, and you want to control construction or hold fields.
- **Modern default:** prefer interfaces; reach for an abstract class only when you genuinely need shared state or a constructor.

---

## 7. PHP contrast

- PHP also **implements many interfaces, extends one class** — same rule.
- PHP interfaces **cannot have method bodies** — no `default` methods. PHP's tool for shared implementation is **traits** (`use SomeTrait;`), the closest analog to Java default methods / mixins.
- PHP trait conflict resolution (`insteadof` / `as`) is the direct analog of Java's `A.super.hi()` diamond resolution.
- Mental map: **Java default methods ≈ PHP traits** — both bolt shared behavior onto a type without class inheritance.

---

## 8. Under the hood

- Interfaces compile to their own `.class` files. Interface method calls use the `invokeinterface` bytecode (vs `invokevirtual` for class methods).
- `default` methods are real methods on the interface; the JVM resolves them via the class's method table — which is why diamond ambiguity must be settled at compile time.
- **Marker interfaces** (e.g. `Serializable`) have **no methods** — they exist purely to *tag* a type so framework/JVM code can check `instanceof` and change behavior. A pre-annotation idiom still seen everywhere.

---

## 9. Interview checklist

- **Interface vs abstract class — when to use which?** Know the capability-vs-shared-state framing and the durable "state + constructor" differentiator.
- **What did Java 8/9 add to interfaces?** `default`, `static`, `private` methods — and *why* default methods exist (backward compatibility, e.g. `Collection.stream()`).
- **Multiple inheritance:** implement many interfaces, extend one class; "type not state"; the diamond problem.
- **Diamond resolution:** compile error until overridden; resolve with `Interface.super.method()`.
- **Implicit modifiers:** interface methods `public abstract`; fields `public static final`.
- **Marker interface:** no methods, tags a type (e.g. `Serializable`).
- **PHP bridge:** default methods ≈ traits.
