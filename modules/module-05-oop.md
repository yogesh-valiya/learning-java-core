# Module 5 — Classes & OOP Mechanics

> Phase 1 — Core language. Priority: **High**. Assumes OOP concepts are known — this focuses on Java's *specific rules* (where they differ from PHP and where the interview gotchas live).

Companion demos:
- [`module-05-oop/FinalDemo.java`](./module-05-oop/FinalDemo.java)
- [`module-05-oop/StaticDemo.java`](./module-05-oop/StaticDemo.java)
- [`module-05-oop/ChainingDemo.java`](./module-05-oop/ChainingDemo.java)
- [`module-05-oop/OverloadOverrideDemo.java`](./module-05-oop/OverloadOverrideDemo.java)

---

## 1. Access modifiers

| Modifier | Same class | Same package | Subclass (diff pkg) | Everywhere | PHP analog |
|----------|:----------:|:------------:|:-------------------:|:----------:|------------|
| `public` | ✅ | ✅ | ✅ | ✅ | `public` |
| `protected` | ✅ | ✅ | ✅ | ❌ | `protected` (but wider in Java — see below) |
| *(default / package-private)* | ✅ | ✅ | ❌ | ❌ | **none** |
| `private` | ✅ | ❌ | ❌ | ❌ | `private` |

Two things that catch PHP developers:

1. **Package-private ("default").** If you write **no** modifier, you get package-private: visible to any class in the **same package**, nobody else. **PHP has no equivalent** — there is no package-scoped visibility in PHP. Common interview question: *"What is the default access level in Java?"* → **package-private** (not public).

2. **Java's `protected` also grants package access.** In PHP, `protected` means "this class + subclasses, period." In Java, `protected` means subclasses **plus everything in the same package** — so Java's `protected` is *wider* than PHP's.

Extra rules:
- A **top-level class** can only be `public` or package-private — never `private`/`protected` (those apply only to members).
- `private` members **are** reachable from nested classes within the same top-level class.

---

## 2. `final` — one keyword, three meanings

| Applied to | Meaning |
|------------|---------|
| **variable** | assign-once — can't be reassigned afterwards |
| **method** | can't be overridden by a subclass |
| **class** | can't be extended (e.g., `String` is `final` — that's *why* nobody can subclass it and break its immutability) |

PHP has `final` for **methods and classes** (same meaning). The broader/different one in Java is **`final` on a variable**.

### The critical gotcha: `final` reference ≠ immutable object

`final` freezes the **variable (the reference)**, not the **object it points at**.

```java
final List<String> list = new ArrayList<>();
list.add("hi");            // ✅ legal — mutating the object
list.add("there");         // ✅ still fine
list = new ArrayList<>();  // ❌ compile error — can't repoint a final reference
```

Trick question: *"Does `final` make an object immutable?"* → **No.** It only prevents reassignment. Immutability is a property of the class's design (like `String`), not of the `final` keyword.

Related terms:
- **Blank final** — a `final` field/variable declared without an initializer and assigned exactly once later (e.g., in a constructor or each branch of an if/else).

---

## 3. `static` — belongs to the class, not the instance

`static` members belong to the **class itself**: one shared copy, accessible with no object.

```java
class Counter {
    static int total = 0;   // ONE copy, shared across all instances
    int id;                 // instance field — each object gets its own
    Counter() { id = ++total; }
}
```

Every `new Counter()` increments the *same* `total`. Access class-level members via the class name: `Counter.total`, `Math.max(...)`, `Integer.parseInt(...)`.

### PHP contrast — a real difference

PHP has static members too (`self::`, `static::`, `ClassName::method()`), so the concept maps. **But PHP has late static binding (`static::`)**, where a static call can resolve to the runtime class. **Java has no late static binding for statics** — a static method call **always** binds at **compile time** to the declared type. (This powers the hiding gotcha below.)

### Under the hood — where statics live & when they run

- **Instance fields** live on the **heap**, one set per object. **Static fields** live once at the **class level** (in *Metaspace* since Java 8, formerly PermGen), created when the **class is loaded** — before any instance exists.
- **Static initializer blocks** run **once**, at class-load time, top to bottom:
  ```java
  static int cache;
  static { cache = expensiveSetup(); }   // runs once when the class loads
  ```
- **Long-running-JVM tie-in:** a `static` field holding a large reference is **never garbage-collected** while the class is loaded → a classic **memory leak** source. (In shared-nothing PHP this bug can't exist — nothing survives the request.)

### The signature gotcha: static methods are *hidden*, not *overridden*

- **Instance methods** are **overridden** → dispatched on the **runtime object type**.
- **Static methods** with the same signature in a subclass are **hidden** → resolved on the **compile-time (declared) type**.

```java
class Parent {
    static String who()  { return "Parent.static"; }
    String       name()  { return "Parent.instance"; }
}
class Child extends Parent {
    static String who()  { return "Child.static"; }     // HIDES Parent.who
    @Override String name() { return "Child.instance"; } // OVERRIDES Parent.name
}

Parent p = new Child();   // declared Parent, runtime Child
p.name();  // "Child.instance"  → overriding → RUNTIME type wins
p.who();   // "Parent.static"   → hiding    → DECLARED type wins
```

Mnemonic: **instance = the actual object (runtime); static = the declared type (compile time).**
Calling a static method through an instance reference (`p.who()`) is legal but misleading — idiomatic form is `Parent.who()` / `Child.who()`.

---

## 4. `this`, `super` & constructor chaining

### Two uses each

| Keyword | As a reference | As a constructor call |
|---------|----------------|-----------------------|
| **`this`** | `this.field` — the current object (disambiguates a shadowed field) | `this(args)` — call **another constructor in the same class** |
| **`super`** | `super.method()` / `super.field` — the **parent's** version | `super(args)` — call a **parent constructor** |

```java
class Employee {
    private String name;
    Employee(String name) {
        this.name = name;   // this.name = field; name = parameter (shadowing)
    }
}
```

### PHP contrast — three real differences

1. **PHP can't overload constructors** (one `__construct`). Java allows **multiple constructors** distinguished by parameters, chained with `this(...)`.
2. **PHP calls the parent manually** (`parent::__construct()`), optionally, anywhere. **Java auto-inserts `super()`** and forces it to be **first**.
3. `$this` → `this`, `parent::` → `super.`

### The constructor chaining rules

1. The **first statement** of every constructor is either `this(...)` or `super(...)` — never both.
2. If you write neither, the compiler **silently inserts `super()`** (the parent's **no-arg** constructor).
3. **Therefore:** if the parent has **no no-arg constructor** (only a parameterized one), the child **must** explicitly call `super(args)`, or the auto-inserted `super()` fails → **compile error**. (The most common "why won't my subclass compile?" cause.)
4. `this(...)` / `super(...)` must be the **very first statement** — no logic before it.

Why can't `this(...)` and `super(...)` coexist? Both must be the first statement, and there is only **one** first-statement slot. You never need both: a `this(...)` chain always bottoms out in exactly one `super(...)`, so the parent constructor still runs exactly once — indirectly.

### Under the hood — construction order

For `new Child()`, the `super(...)` chain runs **all the way up to `Object` first**, then bodies execute **top-down**. A parent is **fully constructed before the child's constructor body runs**.

Full order:
1. **(once, at class load)** static fields + static blocks — parent's, then child's.
2. **On each `new`:** `super(...)` chain up to `Object` → then for each class **top-down**: instance field initializers + instance init blocks → constructor body.

Worked example — `new Dog()` where `Dog()` calls `this("Rex")` → `Dog(String)` calls `super("Rex")` → `Animal(String)`:

```
Animal("Rex")     ← super runs first
Dog("Rex")        ← Dog(String) body
Dog()  no-arg     ← original Dog() body, after this(...) returns
```

And `new Derived()` (parent `Base`, child `Derived`):

```
Base.field init   ← parent field initializers
Base() body       ← parent constructor body
Derived.field init
Derived() body
```

---

## 5. Overloading vs Overriding

The single axis that separates them:

> **Overloading is resolved at COMPILE time (by declared/static types). Overriding is resolved at RUNTIME (by the actual object).**

### Overloading — "static / compile-time polymorphism"

Same method name, **different parameter list** (number, types, or order), in the same class.

```java
String f(Object o) { ... }
String f(String s) { ... }
String f(int a, int b) { ... }
```

- Must differ in **parameters**. **Cannot** overload by **return type alone** (compile error).
- Resolved by the compiler using the **declared (static) type** of the arguments → **static/early binding**.
- Access modifier and return type can differ freely.
- Resolution preference when several could match: **exact → widening (`int`→`long`) → boxing (`int`→`Integer`) → varargs** (most specific wins).

**PHP contrast:** PHP has **no method overloading** in this sense — a genuinely new capability.

**The classic surprise:**

```java
Object x = "hello";   // declared Object, runtime String
printer.f(x);         // calls f(Object) — overloading uses the DECLARED type, at compile time
printer.f("hi");      // calls f(String)
printer.f(42);        // calls f(Integer) — int autoboxes to Integer (no int overload)
```

Even though `x` holds a `String`, `f(Object)` is chosen because overload resolution happens at compile time on the declared type.

### Overriding — "dynamic / runtime polymorphism"

A subclass replaces an **inherited instance method** with the **same signature**. The JVM dispatches on the **actual object at runtime** (dynamic/late binding). This is real polymorphism.

```java
class Animal { String speak() { return "Animal: ..."; } }
class Dog extends Animal { @Override String speak() { return "Dog: Woof"; } }

Animal a = new Dog();  // declared Animal, runtime Dog
a.speak();             // "Dog: Woof" — dispatched on the runtime type
```

The override rules:

| Rule | Detail |
|------|--------|
| **Signature** | Must be **identical** (name + parameter list). Different params = overloading. |
| **Return type** | Same, **or a covariant (subtype)** return — e.g. override `Object clone()` with `String clone()`. |
| **Access** | Same or **wider**, never **more restrictive** — can't override `public` with `protected`. |
| **Checked exceptions** | May throw **fewer/narrower** or none — never **broader/new checked** exceptions. Unchecked are unrestricted. |
| **Cannot override** | `static` (that's *hiding*), `final`, or `private` methods. |
| **`@Override`** | Optional but recommended — the compiler verifies the method actually overrides something (catches signature typos). |

Why can't an override narrow access from `public` to `protected`? Polymorphism means a caller holding the parent type must be able to call the method; a secretly narrowed override would break code that is valid against the parent's contract. Java forbids it at compile time.

---

## 6. Interview checklist

- **Default access level?** — package-private. **Java `protected`** also grants package access (wider than PHP).
- **Does `final` make an object immutable?** — No; it only prevents reassignment of the reference.
- **`String` is `final`** — prevents subclassing that could break immutability.
- **Static method hiding vs instance overriding** — declared type vs runtime type; know the `Parent p = new Child()` example.
- **No late static binding in Java** (unlike PHP `static::`).
- **Static field as a memory-leak source** in a long-running JVM.
- **Constructor chaining:** first statement is `this()`/`super()`; implicit `super()` inserted; no-arg-parent gotcha; construction order (super first, then top-down).
- **Overloading vs overriding:** compile-time/declared-type vs runtime/object-type — the one-sentence answer. Can't overload by return type alone; can't narrow access when overriding; covariant returns allowed.
