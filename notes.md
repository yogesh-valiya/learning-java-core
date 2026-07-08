# Java Core — Interview Notes

_Concise takeaways for quick revision. One section per module. Skim before interviews._

---

## Module 1 — Java platform & how it runs

- **JVM ⊂ JRE ⊂ JDK**
  - **JVM** — executes bytecode; platform-*specific* binary (different build per OS).
  - **JRE** — JVM + standard class libraries (enough to *run*).
  - **JDK** — JRE + dev tools (`javac`, `javap`, debugger) — needed to *compile*.
- **Compile → run:** `javac Hello.java` → `Hello.class` (bytecode) → `java Hello` (JVM runs it).
- **Bytecode** is platform-*neutral*; the JVM binary is platform-*specific*. → the portability boundary is the bytecode. ("Write once, run anywhere.")
- **Is Java compiled or interpreted? → Both.** `javac` compiles source→bytecode ahead of time; at runtime the JVM **interprets** bytecode *and* **JIT-compiles** hot paths to native code.
- **JIT / HotSpot:** JVM profiles execution, finds "hot" methods (called many times), compiles those to native machine code. Cold code stays interpreted.
  - **Why interpret first instead of JIT everything on startup?** JIT compilation costs CPU time. Interpreting gives instant startup; only methods proven hot are worth paying compilation cost for. → startup-latency vs steady-state-throughput trade-off.
- Bytecode **verification** on load = a *security* step, not just correctness.
- **vs PHP:** PHP is shared-nothing (state dies each request). JVM is a **long-running process** — static fields, pools, caches persist for the app's life → you must care about **memory leaks** and **thread-safety**.

---

## Module 2 — Syntax map + Maven

- **Static typing:** every variable's type is fixed at compile time, declared explicitly. `int x = "5";` is a **compile error** (not a runtime surprise like PHP).
- **Integer division truncates:** `9 / 4 == 2`, `7 / 2 == 3`. Force float with one decimal operand: `7 / 2.0 == 3.5`. (Classic "why is my average 0?" bug.)
- Anything in `" "` is **always** type `String`, regardless of contents.
- **Packages** must match the physical folder path — enforced by `javac` (not just convention like PSR-4).
- **`main` signature (exact):** `public static void main(String[] args)`
  - `public` (JVM calls from outside), `static` (called before any object exists), `void`, `String[] args` (like PHP CLI `$argv`).
  - No `main` → compiles fine but fails **at launch**: "Main method not found".
- **Filename must match the `public` class name** exactly (compiler-enforced). One `public` class per file max.
- **Maven ≈ Composer + build lifecycle:**
  - `pom.xml` ≈ `composer.json`; Maven Central ≈ Packagist.
  - Deps cached machine-wide in `~/.m2/repository` (shared, **not** per-project `vendor/`).
  - **GAV coordinates:** `groupId:artifactId:version` (groupId ≈ vendor name, artifactId ≈ package name).
  - Layout: source in `src/main/java`, tests in `src/test/java` (convention over configuration).
- **Lifecycle phases are ordered & cumulative** (running a phase runs all before it):
  - `compile` → `test` → `package` (builds `.jar`) → `install` (copies jar to `~/.m2`).
  - `mvn package` auto-runs compile + test first. **A failing test blocks the build.**
  - `package` vs `install`: package = jar in `target/`; install = also into `~/.m2` for other local projects.
  - **JAR** = zip of `.class` + metadata. **Fat/uber JAR** bundles deps too (runnable standalone; needs Shade plugin; Spring Boot produces these).

---

## Module 3 — Primitives, wrappers, autoboxing; `==` vs `.equals()` ⭐ high-frequency

- **Primitive vs object:** primitive holds the **value** directly; object variable holds a **reference** to a heap object.
- **8 primitives:** `byte short int long float double char boolean`. Defaults: `int` (whole), `double` (decimal). Suffixes: `long`→`L`, `float`→`f`. `char` = single quotes, one char.
- **Wrapper classes** (`Integer`, `Long`, `Double`, `Boolean`, `Character`…) = object version of each primitive.
  - **Why they exist:** Collections store only objects → `List<int>` is illegal, must be `List<Integer>`. Also allow `null` + utility methods (`Integer.parseInt`, `Integer.MAX_VALUE`).
  - `Integer x = null;` legal (reference). `int x = null;` **compile error** (primitive can't be null).

### `==` vs `.equals()` — THE rule
> `==` compares **what the variable holds**. For primitives = the value. For objects = the **reference** (same object in memory?), NOT the content.
> `.equals()` = method comparing **logical value**.
> → **Always use `.equals()` for objects (Strings, wrappers, your classes). `==` only safe on primitives.**

### Two hidden mechanisms that pollute `==`
- **String pool (intern pool):** String *literals* (`"hi"`) are interned & shared → `"hi" == "hi"` is `true`. But `new String("hi")` forces a fresh heap object → `==` is `false`. → `==` on strings is *unpredictable*; use `.equals()`.
  - `new String("hi")` = wasteful (redundant object when pooled one exists) → code smell.
- **Integer cache:** autoboxing caches `Integer` objects for **−128 to 127** (via `Integer.valueOf()`).
  - `Integer a=100, b=100; a==b` → `true` (cached, shared).
  - `Integer a=1000, b=1000; a==b` → `false` (outside cache, separate objects).
  - **Classic prod bug:** `==` on `Integer` IDs passes tests (small values ≤127), breaks in prod (IDs >127). Fix: `.equals()` or unbox to `int`.

### Autoboxing / unboxing
- Auto-convert primitive ↔ wrapper: `Integer b = 5;` (box, calls `Integer.valueOf`), `int p = b;` (unbox, calls `intValue()`).
- **Unboxing NPE trap:** `Integer count = null; int x = count;` → **NullPointerException** (invisible `count.intValue()` on null). Common with `map.get(missingKey)` returning null → auto-unbox → NPE. Null-check or keep as wrapper.

---

## Module 4 — Strings ⭐ very common

- **String is immutable:** once created, contents never change. Every "modifying" method (`toUpperCase`, `concat`, `replace`, `substring`, `+`) returns a **new** String; the original is untouched. You can only re-point the *variable*.
  - `s.toUpperCase();` alone does nothing visible — must reassign: `s = s.toUpperCase();`
  - No in-place char edit (unlike PHP `$s[0]='H'`). `s.charAt(0)='H'` doesn't compile.

### Under the hood
- Backed by a `private final byte[]` (was `char[]` pre-Java 9; Java 9+ "compact strings" use `byte[]`). Array is `final` + `private`, never exposed for writing → immutable.
- **Why immutable? (4 reasons, common "why" Q):**
  1. **Makes the string pool safe** — pooled literals are shared; safe only because nobody can mutate a shared instance. (Immutability is the *precondition* for pooling.)
  2. **Thread-safe for free** — shareable across threads, no sync.
  3. **Cached hashCode** — computed once, never changes → fast `HashMap` keys (strings are the most common key).
  4. **Security** — safe for filenames/URLs/hosts; no check-then-change exploit window.

### String pool + intern()
- Literals (`"hi"`) are interned & shared → `"hi" == "hi"` is `true`. `new String("hi")` = fresh heap object, NOT pooled → `new String("hi") == "hi"` is `false`.
- `.intern()` returns the **pooled** version of a string: `new String("hi").intern() == "hi"` → `true`.
- JDK micro-opt: `s.concat("")` returns `this` (same object) when arg is empty — but `s.concat("x")` builds a new String. (Don't over-assume library behavior — read the source.)

### StringBuilder vs StringBuffer (mutable strings)
- **Immutable `+=` in a loop = O(n²)** (new String + full copy each iteration). Measured: 100k chars → `+=` ~4300 ms vs `StringBuilder` ~3 ms (~1400×). **Reach for StringBuilder whenever concatenating in a loop.**
- **StringBuilder**: mutable buffer, `append()` mutates same object & returns `this` (chainable). **Not** thread-safe. **Default choice.**
- **StringBuffer**: identical API but every method `synchronized` (thread-safe) → slower. Legacy; use only if multiple threads mutate the **same shared** builder (rare).
- Under the hood: resizable `byte[]` + length; default capacity **16**; grows ~`2*cap+2` (doubling) → amortized O(1) append, O(n) total. Pre-size `new StringBuilder(n)` to skip resizes.
- **Gotcha:** compiler turns a *single-expression* `a + b + c` into one efficient concat (StringBuilder / Java 9+ `invokedynamic`). But `+=` in a **loop** = a new StringBuilder per iteration → back to O(n²). `+` is fine for a few pieces on one line; loops need an explicit StringBuilder.
- Convert back to String at the end with `sb.toString()`.

---

## Module 5 — Classes & OOP mechanics ⭐ high-frequency

### Access modifiers
- Levels: `public` > `protected` > *default (package-private)* > `private`.
- **Default (no keyword) = package-private** — visible in same package only. **No PHP equivalent.** (Common Q: "default access level?" → package-private, NOT public.)
- **Java `protected` = subclasses + same package** (wider than PHP's "class + subclasses").
- Top-level class can only be `public` or package-private.

### final (3 meanings)
- **variable** = assign-once; **method** = can't override; **class** = can't extend (`String` is final).
- **Gotcha:** `final` on a reference = can't reassign the reference; the **object is still mutable**. `final List l` → `l.add()` OK, `l = new...` compile error. `final` ≠ immutable.

### static (class-level, not instance)
- One shared copy; access via `ClassName.member`. Lives in Metaspace, created at class load (before any instance).
- Static init block runs **once** at class load, top-to-bottom.
- **No late static binding** in Java (unlike PHP `static::`) — static calls bind at **compile time** to declared type.
- **Static methods are HIDDEN, not overridden.** Instance methods → dispatched on **runtime** type (override). Static methods same signature in subclass → resolved on **declared** type (hiding). Mnemonic: *instance = actual object; static = declared type.*
- Static field holding a big ref = memory-leak source in long-running JVM.

### this / super + constructor chaining
- `this.field` (current obj), `this(args)` (another constructor same class); `super.x` (parent), `super(args)` (parent constructor).
- **Java can overload constructors** (PHP can't). Java **auto-inserts `super()`** as 1st statement if you write neither; PHP calls `parent::__construct()` manually.
- Rules: 1st statement of a constructor is `this(...)` OR `super(...)` (never both — only one first-statement slot). If parent has **no no-arg constructor**, child MUST call `super(args)` explicitly or it won't compile.
- **Construction order:** super chain up to Object first, then top-down per class: field initializers + init blocks → constructor body. Parent fully built before child body runs.

### Overloading vs Overriding ⭐
- **Overloading** = same name, different params; resolved at **COMPILE time** by **declared/static type** of args. Can't overload by return type alone. Preference: exact → widening → boxing → varargs. (PHP has no overloading.)
  - Surprise: `Object x = "hi"; f(x)` → calls `f(Object)`, not `f(String)` (declared type wins).
- **Overriding** = same signature in subclass; resolved at **RUNTIME** by **actual object type** (real polymorphism / dynamic dispatch).
  - Rules: identical signature; return same or **covariant**; access **same or wider** (never narrower); **no broader checked exceptions**; can't override `static`/`final`/`private`; use `@Override`.
- **One-liner:** *Overloading = compile-time, declared type. Overriding = runtime, actual object.*

---

## Module 6 — Interfaces vs Abstract Classes ⭐ favorite design Q

- **Interface** = contract of type/behavior. Methods implicitly `public abstract`; fields implicitly `public static final` (constants). **No instance state.** A class must implement all abstract methods.
- **Java 8/9 additions:** `default` methods (body in interface, inherited/overridable), `static` methods (factories/utils), `private` methods (Java 9, shared helpers).
  - **Why default methods?** Backward compatibility — let JDK add `Collection.stream()` without breaking existing implementors.
- **Abstract class** = partial implementation **with instance state + constructors**; can't be instantiated; mix of abstract + concrete methods.
- **Multiple inheritance of TYPE not STATE:** implement many interfaces, extend only one class. Banned multiple class inheritance avoids the diamond problem for **state**.
- **Diamond problem (default methods):** two interfaces with same `default` method → **won't compile** until you override; resolve with `Interface.super.method()` (e.g. `A.super.hi()`).
- **Durable difference (say this in interviews):** even after Java 8, abstract class has **instance state + constructors**; interface does not. Need shared fields/construction → abstract class; need a capability, esp. across unrelated types or multiple → interface.
- **When to use:** interface = capability ("can-do", `Comparable`/`Runnable`), abstract class = shared state+code with strong "is-a". Modern default: **prefer interfaces**.
- **Marker interface:** no methods, tags a type (`Serializable`) for `instanceof` checks.
- **PHP bridge:** Java `default` methods ≈ **PHP traits**; `A.super.hi()` ≈ PHP trait `insteadof`/`as`.
- **Under the hood:** interface calls use `invokeinterface` bytecode. Single-abstract-method interface = **functional interface** → basis of lambdas (Module 16).

---

## Module 7 — equals() & hashCode() ⭐ extremely frequent

- Every class extends `Object`. Default `equals()` = reference (`==`); default `hashCode()` = identity; default `toString()` = `ClassName@hex`.
- **equals() contract:** reflexive, symmetric, transitive, consistent, `x.equals(null)`=false.
- **hashCode() contract:** equal objects (by equals) MUST have same hashCode; unequal MAY collide.
- **GOLDEN RULE: override `equals()` → MUST override `hashCode()`.** Else hash collections break silently.
  - **Mechanism:** hash collection = (1) `hashCode()` picks **bucket** → (2) `equals()` matches **within** bucket. Broken hashCode → equal objects in different buckets → `map.get` returns null, `set` keeps duplicates (size grows). No exception — silent bug.
- **Canonical equals():** `this==o` → `null || getClass()!=o.getClass()` → cast → compare fields.
- **Canonical hashCode():** `Objects.hash(sameFields)`. `Objects.equals(a,b)` = null-safe field compare.
- **#1 gotcha:** `equals(MyType)` (wrong param type) = **overload not override** — collections still call `Object.equals`. `@Override` catches it (won't compile). Real signature = `equals(Object)`.
- **getClass() vs instanceof:** `getClass` = strict, symmetric, but subclass never equals superclass (breaks Liskov). `instanceof` = lenient but can **break symmetry** when subclass adds fields (`p.equals(cp)`=true, `cp.equals(p)`=false). → make value types `final` / use `getClass`.
- **Mutable-key trap:** mutating a field used in `hashCode()` while object is a HashMap key → entry stranded in old bucket → `get` returns null. **Hash keys must be immutable** (why String/wrappers are ideal keys).
- **Records (Java 16+):** auto-generate equals/hashCode/toString/accessors/constructor. `toString` format = `Name[x=1, y=2]`. Modern way to write value classes.
- **PHP:** no equals/hashCode contract (maps use string keys). `__toString()` ≈ `toString()`.

---

## Module 8 — Building Immutable Classes

- **Recipe (5 rules):** (1) class `final`; (2) fields `private final`; (3) no setters; (4) init all in constructor; (5) **defensive copy mutable fields — IN (ctor) and OUT (getters)**.
- **`final` alone isn't enough** — it freezes the reference, not the object. Mutable fields (`Date`, `List`, arrays) leak without copies.
- **Two leak points:** (A) constructor stores caller's reference → caller mutates it later; (B) getter returns internal reference → caller mutates it. Fix: `new Date(d.getTime())` in and out.
- **Collections:** `List.copyOf(x)` (Java 10+) = true immutable copy (best). `Collections.unmodifiableList(x)` = **view, not a copy** (original ref can still mutate it). `new ArrayList<>(x)` also decouples.
- **Shallow vs deep:** copying a list protects structure but mutable *elements* stay shared. `List<String>` safe (immutable elements); `List<Date>` needs element copies. Keep elements immutable.
- **Copy BEFORE validate** (avoid TOCTOU — caller mutating between check and store).
- Primitives/wrappers/String need **no** defensive copy (already immutable). Arrays always mutable → `clone()`/`Arrays.copyOf`.
- **Records nuance:** records give final fields but **don't auto-defensive-copy** mutable components → add a **compact constructor** to copy in.
- **Why:** thread-safe for free, safe hash keys, no defensive checks, cacheable, secure.
- **PHP:** 8.1 `readonly` ≈ final fields; no built-in defensive copy (clone manually).
