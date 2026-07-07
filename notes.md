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
