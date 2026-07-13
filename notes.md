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

---

## Module 9 — Enums

- **Enum = full class; each constant = a singleton instance.** Can have fields, (private) constructor, methods.
- **Built-ins:** `name()`, `ordinal()` (0-based), `values()` (array), `valueOf("X")` (throws `IllegalArgumentException` if unknown). `toString()` defaults to `name()`. Comparable + Serializable.
- **`==` is PREFERRED for enums** (opposite of normal objects!): singleton → identity=value, null-safe, compile-checked. `.equals()` also works but `==` wins.
- `switch` case labels are **unqualified** (`MONDAY`, not `Day.MONDAY`).
- **Per-constant method bodies** via an abstract method → each constant implements it. Beats `switch`: adding a constant **won't compile** until you give it behavior (compiler-enforced completeness). Standard "avoid switch" answer.
- **Enums can implement interfaces** (can't extend a class — already extends `java.lang.Enum`).
- **EnumMap** = Map keyed by enum, backed by an **array indexed by ordinal()** (no hashing; iterates in **declaration order**, not insertion). **EnumSet** = **bitvector** (a `long` for ≤64). Both faster/compact than HashMap/HashSet for enum keys/elements.
- **Under the hood:** compiler makes `final class extends Enum`; constants are `public static final`, built in a static initializer at class load. Singleton survives serialization + reflection → single-element enum = best **Singleton** (Effective Java).
- **Gotchas:** never persist `ordinal()` (reordering corrupts data → use `name()`/explicit code); keep enum fields `final` (constants are shared singletons = global state).
- **PHP:** 8.1+ has real enums (pure + backed `->value` ≈ Java enum w/ field); pre-8.1 = untyped class constants.

---

## Module 10 — Nested & Anonymous Classes

- **4 kinds:** static nested, inner (non-static), local, anonymous.
- **Static nested** = no link to outer instance; `new Outer.StaticNested()`. Like a namespaced top-level class (e.g. `Map.Entry`).
- **Inner (non-static)** = **hidden reference to an outer instance**; accesses outer's instance fields; needs `outer.new Inner()` syntax.
- **Anonymous class** = inline unnamed class+instance implementing an interface/abstract class (`new Runnable(){...}`). **Pre-lambda way to pass behavior**; lambda ≈ anonymous impl of a functional interface.
- **Effectively final:** captured local vars must be assigned once (copied into synthetic fields). **Captured local = frozen copy; outer instance field = live read** through the outer reference.
- **Under the hood:** compiler emits `Outer$Inner.class`, `Outer$1.class` (anonymous). Inner class has a synthetic outer-reference field; static nested does not.
- **⚠️ Memory-leak gotcha:** inner instance pins its outer alive (can't GC) if it outlives it (long-lived listener/callback/returned Iterator). **Fix: make it `static` if outer isn't needed** (Effective Java: prefer static nested).
- **PHP:** anonymous classes since PHP 7; closures `function() use($x){}` ≈ capture. No class-in-class/outer binding.

---

## Module 11 — Generics ⭐ high-frequency (Phase 2 opener)

- **No PHP equivalent** — PHPStan/Psalm `@template` is advisory-only, erased at runtime. Genuinely new mechanics.
- **Why generics:** pre-Java-5 collections stored raw `Object` → manual casts, `ClassCastException` far from the real bug. Generics move the error to **compile time**, at the bad call.
- **Generic class:** `class Box<T> { T value; ... }`, instantiated as `Box<String>`. **Generic method:** own `<T>` before return type, independent of the class — `T` is **inferred** from the call arg.
- **Naming convention:** `T` Type, `E` Element, `K`/`V` Key/Value, `N` Number, `R` Return.
- **Bounded type parameter** (`<T extends Comparable<T>>`): the bound is a **permission slip, not just a filter** — it's what lets the method body call `.compareTo()`. Unbounded `<T>` defaults to `<T extends Object>` (NOT "inferred as Object" — that's a different mechanism/call-site concern; the bound is what's visible in the method body regardless of any call site).
  - `extends` used for both classes AND interfaces in a bound (no `implements` in generics).
  - Multiple bounds: `<T extends Comparable<T> & Serializable>` (class first, then interfaces, `&`-joined).
  - Self-referential bound `<T extends Comparable<T>>` is real JDK pattern — `Enum<E extends Enum<E>>`.
- **Invariance:** `List<Integer>` is **NOT** a `List<Number>` even though `Integer IS-A Number`. If it were allowed, `List<Number> nums = ints; nums.add(3.14);` would smuggle a `Double` into a real `List<Integer>` — compile-time safety hole. Java just refuses the assignment.
- **Wildcards:**
  - `List<? extends Number>` — unknown subtype; read as `Number` ✅, write ❌ (might secretly be `List<Integer>`).
  - `List<? super Integer>` — unknown supertype; write `Integer` ✅, read only as `Object` ⚠️.
  - `List<?>` — unknown type; read as `Object` only, write ❌ (except `null`).
  - `extends` = safe reads/blocked writes; `super` = safe writes/crippled reads. Never both open on one wildcard — by design.
- **PECS (Producer Extends, Consumer Super):** producer (you read from it) → `extends`; consumer (you write to it) → `super`. Canonical: `Collections.copy(List<? super T> dest, List<? extends T> src)`.
- **Type erasure:** generics are **compile-time only** — erased from bytecode for **backward compatibility** with pre-Java-5 `.class` files/JVM. Unbounded `T` → `Object`; bounded `<T extends Foo>` → `Foo`. `Box<String>` and `Box<Integer>` are the **same runtime class** (`getClass() ==` → `true`).
- **Consequences of erasure (frequent gotcha list):**
  - No `new T()` (no runtime type info).
  - No `instanceof List<String>` (only `List<?>` / raw `List`).
  - No `new T[10]` / `new List<String>[10]` (arrays check element type on every store at runtime; generics don't → heap pollution risk).
  - No overloading `f(List<String>)` vs `f(List<Integer>)` (same erasure = duplicate method, compile error).
  - No class `T` inside `static` members (static belongs to the class, not a parameterized instance).
  - **Bridge methods:** compiler auto-generates a synthetic erased-signature overload (e.g. `compareTo(Object)`) so polymorphism survives erasure when you override with a narrower type (`compareTo(MyType)`). Visible via `javap -p`.
- **Raw types (no `<>`):** opt completely out of generics checking — legal only for backward compat, **never use in new code**. `Box raw = new Box("x"); raw.set(42);` compiles (erasure = `Object` param). Assigning raw → parameterized ref = unchecked warning but compiles. The `ClassCastException` fires **at the read site** (compiler-inserted cast based on the *reference's* declared type), NOT where the bad value was stored — same "failure far from cause" problem generics exist to prevent, snuck back in.

---

## Module 12 — Collections Overview ⭐ high-frequency

- **Hierarchy:** `Iterable` → `Collection` → `List` / `Set` / `Queue` (→ `Deque`). **`Map` is separate — does NOT extend `Collection`** (pairs vs. single elements, different shape, not a missing feature). Map exposes `keySet()`→`Set<K>`, `values()`→`Collection<V>`, `entrySet()`→`Set<Map.Entry<K,V>>` to plug back into the Collection world.
- **`Set`'s uniqueness = `equals()`/`hashCode()` contract (Module 7), not interface magic.** Broken contract on your class → `HashSet` silently keeps "duplicates."
- **Implementations map:** List→ArrayList/LinkedList/Vector; Set→HashSet/LinkedHashSet/TreeSet; Queue/Deque→ArrayDeque/LinkedList/PriorityQueue; Map→HashMap/LinkedHashMap/TreeMap.
- **`LinkedList` implements BOTH `List` and `Deque`** simultaneously — genuinely dual-purpose, not just a list.
- **ArrayList:** resizable `Object[]`. `get(index)`=O(1). `add` at end=amortized O(1) (resize at **1.5×** cap, `Arrays.copyOf`, default cap 10). Middle insert/remove=O(n) (`arraycopy` shift). Cache-friendly (contiguous memory).
- **LinkedList:** doubly-linked `Node{prev,item,next}`. Ends (`addFirst/Last`, `removeFirst/Last`)=O(1). `get(index)`=O(n) (walks from nearer end, avg n/4). Index-based middle insert=**still O(n) overall** — traversal to reach the spot dominates. **True O(1) insert ONLY via an already-positioned `ListIterator`** — not "inserting at index N is fast." Heavier memory/element (node+2 refs+header). Poor cache locality (scattered nodes, pointer chasing).
- **`RandomAccess` marker:** `ArrayList` implements it, `LinkedList` doesn't. JDK algorithms (`Collections.binarySearch`) check `instanceof RandomAccess` to pick index-loop vs iterator strategy.
- **`get(i)`-loop anti-pattern:** looping `for(i=0;i<list.size();i++) list.get(i)` over a `LinkedList` = **O(n²)** (each call re-walks up to n/4 nodes, n times). Swap to for-each/iterator = O(n). **Measured: ~318× slower** on identical 40k-element data — same list, only the access pattern changed.
- **Modern default: prefer ArrayList almost always** (cache locality wins in practice, even for insert-heavy workloads). For real queue/stack/deque needs, **prefer `ArrayDeque` over `LinkedList`** (circular array, no node overhead) — LinkedList rarely the right default anymore.
- **Gotcha:** casting a `Queue` reference to `List` to get index access (`((List<Job>) jobs).get(i)`) only works because `LinkedList` happens to implement both. Swap to `ArrayDeque` (the modern recommendation) → `ClassCastException` at runtime. The cast itself is the smell — use `Queue`'s own methods (`poll()`/`peek()`) or a plain iterator instead.
- **PHP:** one array type blurs list/set/map/queue into one hybrid structure; Java's split means the declared interface is a real contract (a `List` promises meaningful indexing, a `Queue` doesn't). No PHP equivalent of `RandomAccess`.

---

## Module 13 — HashMap Internals ⭐ THE #1 internals question

- **Structure:** array of buckets (`Node<K,V>[] table`). `put`/`get` = compute bucket from hash, then `equals()`-scan within that bucket.
- **Bucket index = bitmask, not modulo:** `(capacity-1) & hash`. Only works because capacity is **always a power of two** — a mask is only equivalent to modulo when it's a contiguous run of 1-bits. Non-power-of-two capacity → some indices become mathematically unreachable.
- **`hash()` spreading:** `h ^ (h >>> 16)` — folds high bits down since bucket indexing only reads low bits; defends against hashCodes that differ mainly in high bits. Can't fix hashes already close in the low bits (e.g. 1 vs 17 still collide at capacity 16).
- **Collision handling:** pre-Java-8 = linked list per bucket (O(n) worst case). Java 8+ **treeifies** a bucket into a red-black tree at **8 entries** (`TREEIFY_THRESHOLD`), but only if capacity ≥ **64** (`MIN_TREEIFY_CAPACITY`) — else resizes instead. Un-treeifies at ≤6 during a resize.
- **Why 8:** Poisson-distribution argument in the JDK source — a healthy hash function almost never produces a bucket that large. Treeification = defense against a bad/malicious hashCode (hash-flooding), not normal-case behavior.
- **Tree ordering:** compares `hash` first (real signal) → `Comparable` if available → else a class-name/identity-hash tiebreak (keeps the tree valid, but has NO relation to the keys, so can't prune a search).
- **Key nuance:** a hashCode that's **constant** for every key still gets treeified, but degrades **far past O(log n)** (measured: per-lookup cost roughly doubles-to-sextuples every time n doubles) — because there's no real ordering signal, only the meaningless tiebreak. Treeification defends ordinary bucket collisions (different hashes, same bucket via mask truncation), NOT a hashCode that's constant for every input.
- **Contract distinction:** constant-but-consistent bad hashCode = **performance** bug only (contract intact — consistent, equal keys share a hash). **Inconsistent** hashCode (varies per call for the same key) = **correctness** bug — put/get compute different buckets, entry becomes silently unreachable. Don't conflate the two.
- **Real defense against pathological hashing:** write an actual hashCode() (combine fields with a multiplier, or `Objects.hash(...)`) so colliding buckets still have differing hash *values* for the tree to sort by.
- **Resizing:** default capacity 16, default load factor **0.75**, threshold = capacity×loadFactor. Load factor 0.75 = space/time tradeoff (too high → deep buckets before resize; too low → wasteful, too-eager resizing).
- **Java 8 resize optimization:** new capacity is always exactly 2× old, so the mask gains exactly one new bit. Each entry's new bucket is either its old index or `oldIndex+oldCapacity`, decided by that one new bit — resize splits each bucket into lo/hi lists and relinks, no full rehash needed.
- **`tableSizeFor`:** initial-capacity constructor arg always rounds **up** to the next power of two (`new HashMap<>(50)` → capacity 64).
- **Practical tip:** pre-size (`new HashMap<>((int)(n/0.75f)+1)`) when the count is known upfront — skips the resize-copy cascade. (Measure this via **separate JVM processes**, not back-to-back in one process — in-process timing let JIT/GC state bleed between configs and gave backwards results.)
- **PHP:** array/hashtable hybrid exposes none of this — no capacity, no load factor, no visible resize policy. Java makes every tradeoff explicit and tunable.

---

## Module 14 — Set/Map Variants

- **HashSet = thin wrapper over `HashMap<E,Object>`** (`add(e)` → `map.put(e, PRESENT)`). Same internals/guarantees as Module 13: O(1) average, no ordering.
- **LinkedHashMap/LinkedHashSet:** same hash nodes as HashMap, PLUS a doubly-linked list threaded through them for predictable iteration. Default = **insertion order**. `accessOrder=true` constructor flag = re-orders on every get/put to **access order** (most-recently-used moves to the end).
- **LRU cache in ~5 lines:** `new LinkedHashMap<>(cap, 0.75f, true) { removeEldestEntry() { return size() > N; } }` — accessOrder=true + override removeEldestEntry.
- **TreeSet/TreeMap = a REAL, always-on red-black tree** (not conditional like HashMap's treeify). Requires `Comparable` or a supplied `Comparator` up front — no ordering signal → `ClassCastException` at insertion, not silent degradation. Genuinely guaranteed O(log n) (unlike HashMap's best-effort treeification from Module 13).
- **Navigation methods** (TreeMap/TreeSet only): `firstKey/lastKey`, `higherKey/lowerKey`, `ceilingKey/floorKey`, `headMap/tailMap/subMap` (range views).
- **Null handling:** HashSet/HashMap/LinkedHash* allow one null (key). **TreeSet/TreeMap reject null** — NPE on `compareTo`.
- **Demo confirms:** HashSet order is neither insertion nor sorted (pure hash/bucket layout) — proof there's truly no ordering guarantee.
- **When to use which:** don't care about order → Hash*; need insertion order → LinkedHash*; need sorted/range queries → Tree*; need LRU → LinkedHashMap(accessOrder=true).
- **PHP:** arrays are always insertion-ordered by default — no PHP equivalent of choosing between unordered/insertion-ordered/sorted as distinct types with different cost tradeoffs.

---

## Module 15 — Comparable vs Comparator; Iterators, Fail-Fast vs Fail-Safe

- **Comparable** (`compareTo`) = one natural order, defined INSIDE the class. **Comparator** (`compare`) = external, pluggable order(s), defined OUTSIDE — any number per type, functional interface.
- **Modern chaining idiom:** `Comparator.comparing(Person::lastName).thenComparing(Person::firstName)`; also `.reversed()`, `naturalOrder()`/`reverseOrder()`, `nullsFirst`/`nullsLast`.
- **⚠️ Sharpest gotcha:** sorted collections (`TreeSet`/`TreeMap`) decide "duplicate" via **`compareTo`/`compare` == 0 EXCLUSIVELY** — `equals()`/`hashCode()` are never consulted. Two clearly-different (`!equals`) objects that tie under the comparator **silently collapse into one entry**. Measured: two different `Person`s with the same age, in a `TreeSet<Person>` ordered by age → size 1, not 2.
- **Sort stability:** object sort (`Collections.sort`/`List.sort`) = modified **TimSort**, O(n log n), **stable**. Primitive `Arrays.sort` = dual-pivot quicksort, **not stable** (moot — primitives have no identity beyond value).
- **`Iterator.remove()`** = the only safe way to remove mid-iteration (updates iterator's own bookkeeping). Modern equivalent: `list.removeIf(condition)`.
- **Fail-fast:** `modCount` incremented on every structural change; iterator checks `expectedModCount` on each `next()`, throws `ConcurrentModificationException` on mismatch — **best-effort bug detection, NOT a correctness guarantee**. Classic bug: `for(x : list) list.remove(x)` → CME. Fix: `Iterator.remove()` or `removeIf`.
- **Fail-safe:** `CopyOnWriteArrayList`, `ConcurrentHashMap` iterators — snapshot/weakly-consistent traversal, never throws CME, but may not reflect the very latest concurrent changes. (Full depth in Phase 5.)
- **PHP:** `usort`/`uasort` callback ≈ Comparator, but no Comparable-style natural-order convention, and no fail-fast iteration concept at all.

**PHASE 2 COMPLETE** (Modules 11-15: Generics, Collections overview, HashMap internals, Set/Map variants, Comparable/Comparator + iterators).

---

## Module 16 — Lambdas & Functional Interfaces

- **Functional interface** = exactly one abstract method (SAM); `@FunctionalInterface` enforces it at compile time (optional, not required for lambdas to work).
- **Lambda vs anonymous class — 2 real differences (not just syntax):**
  - Compilation: anonymous class → own `.class` file at compile time (`Outer$1.class`); lambda → `invokedynamic` + `LambdaMetafactory`, generated at runtime, no per-lambda `.class` file.
  - **`this` binding:** lambda's `this` = the ENCLOSING instance (lexical, no new scope); anonymous class gets its OWN `this`. Verified: `this.getClass().getSimpleName()` inside a lambda printed the enclosing class name; inside an anonymous class it printed **empty** (anonymous classes have no simple name at all, even reflectively).
  - Captured-variable rule is identical to Module 10 (effectively final locals = frozen copy; outer fields = live read).
- **4 core interfaces:** `Function<T,R>.apply`, `Predicate<T>.test`, `Consumer<T>.accept`, `Supplier<T>.get`.
- **Chaining:** `andThen` = receiver runs FIRST, then argument. `compose` = argument runs FIRST, then receiver. `Predicate.and/or/negate`.
- **Primitive specializations** (`IntPredicate`, `ToIntFunction`, etc.) avoid autoboxing — same cost Module 3 warned about, now showing up in `java.util.function`.
- **4 method-reference kinds:** static (`Integer::parseInt`), bound-instance (`System.out::println` — receiver fixed, param = argument), **unbound-instance** (`String::toUpperCase` — param BECOMES the receiver, easy to mix up with static), constructor (`ArrayList::new`).
- **PHP:** closures capture via explicit `use($x)`; no equivalent of the lambda/anonymous-class `this`-binding split since PHP only has one way to define inline behavior.

---

## Module 17 — Streams API

- **Stream = lazy pipeline, not a data structure.** Nothing runs until a terminal op fires. **Single-use** — a 2nd terminal op on the same stream throws `IllegalStateException`.
- **Intermediate** (map/filter/flatMap/sorted/distinct/limit/skip/peek) = lazy, returns a new Stream. **Terminal** (collect/reduce/forEach/count/anyMatch/findFirst/toArray) = triggers execution, produces a real result.
- **⭐ The model that matters:** processing is **vertical** (one element pulled through ALL stages before the next element starts), not horizontal/stage-by-stage. `findFirst`/`anyMatch`/`limit` **short-circuit** — stop the whole pipeline the instant satisfied. Verified: `peek` on an 8-element list saw only elements 1-2 before `findFirst` ended everything; 3-8 never touched by ANY stage.
- **map vs flatMap:** map = 1-to-1. flatMap = 1-to-many + flattens (`Stream<List<T>>` → `Stream<T>` via `.flatMap(List::stream)`).
- **reduce:** 3 overloads — `reduce(BinaryOperator)`→Optional; `reduce(identity,BinaryOperator)`→T; `reduce(identity,accumulator,combiner)` — 3-arg exists for PARALLEL streams (combiner merges per-thread partial results).
- **Collectors:** `toList`/`joining(delim)`; `groupingBy(classifier)` → Map<K,List<T>>; `groupingBy(classifier, downstream)` — pair with `counting()`/`mapping()`/nested `groupingBy` to reshape each bucket; `partitioningBy(predicate)` → always exactly `Map<Boolean,List<T>>`.
- **Primitive streams** (IntStream/LongStream/DoubleStream): avoid autoboxing per element — same Module 3 cost, `mapToInt`/`mapToObj`/`.boxed()` convert at the boundary.
- **Parallel streams:** `ForkJoinPool.commonPool()`-backed; NOT automatically faster (coordination overhead can lose to sequential on small/cheap work); shared mutable state touched inside the lambda = race condition. Full depth in Phase 5.
- **PHP:** `array_filter`/`array_map`/`array_reduce` run eagerly, full intermediate array at each step (horizontal) — no laziness, no short-circuiting, no single-use restriction.

---

## Module 18 — Optional

- **Purpose:** a RETURN-TYPE signal that a result might be absent — makes it visible in the signature, unlike `null`. NOT a general null-replacement.
- **Creation:** `Optional.of(v)` throws NPE immediately if v is null (fail-fast); `Optional.ofNullable(v)` → empty instead; `Optional.empty()`.
- **Anti-pattern:** `if (opt.isPresent()) opt.get();` = null-check with extra steps, defeats the purpose. Use `map`/`filter`/`orElse`/`orElseThrow`/`ifPresent`/`ifPresentOrElse` instead.
- **⚠️ orElse vs orElseGet:** `orElse(x)` evaluates `x` EAGERLY, always, even when present (Java evaluates args before the call). `orElseGet(supplier)` only invokes the supplier when actually empty. Measured: `.orElse(expensiveCall())` on a PRESENT optional still ran `expensiveCall()`; `.orElseGet(() -> expensiveCall())` did not. Real perf/correctness bug, not style.
- **Common mistakes:** Optional as a field or method parameter (not Serializable, awkward for callers — return-type only); `.get()` without checking → `NoSuchElementException` (same crash pattern, new name); wrapping a collection in Optional (`Optional<List<T>>`) — just return an empty collection instead.
- **Good use:** chained `map()` calls replace nested null-check pyramids, short-circuiting to `orElse(default)` cleanly.
- **PHP:** nullsafe `?->` (PHP 8) covers chained access but is a language-level null-propagation operator, not a distinct type — doesn't force a signature to declare possible absence. No standard Optional/Option type in PHP.

---

## Module 19 — Date/Time API (java.time)

- **Why it exists:** old `Date`/`Calendar` = mutable, not thread-safe, 0-indexed months (`Calendar.JANUARY==0`). Java 8's `java.time` (JSR-310) = immutable, thread-safe, unambiguous. **Months are 1-indexed** here.
- **Core types, one concept each:** `LocalDate` (date only), `LocalTime` (time only), `LocalDateTime` (date+time, no zone), `ZonedDateTime` (date+time+zone), `Instant` (UTC-timeline point, machine timestamp).
- **⭐ Duration (time-based, exact) vs Period (date-based, calendar-aware):** NOT interchangeable. Measured: `Period.ofMonths(1)` added to Jan 31 2026 → **Feb 28** (clamped to calendar). `Duration.ofDays(1)` (fixed 24h) added to Jan 31 09:00 → **Feb 1, 09:00**. Same "1 unit," genuinely different results.
- **Immutability = same rule as String (Module 4):** `plusDays`/`withYear`/etc. return a NEW instance; forgetting to reassign is a silent no-op.
- **`DateTimeFormatter`** is immutable/thread-safe (safe as shared `static final`) — fixes a REAL bug in old `SimpleDateFormat`, which is mutable and NOT thread-safe (shared instance across threads → silently corrupted parse/format results, not a crash).
- **`ChronoUnit.DAYS.between(a,b)`** = single-unit raw count. **`Period.between(a,b)`** = full calendar breakdown (years+months+days).
- **PHP:** `DateTime` (mutable mistake) vs `DateTimeImmutable` (the fix) mirrors this exactly; `DateInterval` ≈ Duration+Period combined.

**PHASE 3 COMPLETE** (Modules 16-19: Lambdas & functional interfaces, Streams API, Optional, Date/Time API).

---

## Module 20 — Exception Handling

- **Hierarchy:** `Throwable` → `Error` (JVM-level, don't catch) + `Exception` → `RuntimeException` (**unchecked**) / everything else (**checked**, compiler-enforced: catch or `throws`).
- **PHP has NO checked exceptions** — every PHP exception is "unchecked" in Java's sense. This enforcement mechanism is Java-specific.
- **Checked exceptions are controversial:** interact badly with lambdas/streams (can't throw checked from most functional interfaces without wrapping) — modern code often favors unchecked even for recoverable cases.
- **try-with-resources:** any `AutoCloseable`; multiple resources close in **REVERSE** declaration order (verified: r2 before r1). If try throws AND close() also throws, the **original exception wins**, close()'s exception is attached as **suppressed** (`addSuppressed`/`getSuppressed`), NOT swapped in — fixes the old finally-masks-original-exception bug.
- **Custom exceptions:** extend `Exception` (checked) or `RuntimeException` (unchecked); ALWAYS chain the cause (`super(message, cause)`) — never swallow the original when wrapping.
- **⚠️ finally's worst gotcha:** `finally` always runs, but a `return`/`throw` INSIDE finally silently swallows any in-flight exception with zero trace. Verified: a method that throws in try but `return`s in finally returns normally — the exception vanishes completely. Never put `return` in `finally`.
- **Best practices:** catch specific not broad; never swallow silently (empty catch); don't use exceptions for control flow; chain causes; prefer try-with-resources over manual finally-cleanup.

**PHASE 4 COMPLETE** (Module 20: Exception handling).
