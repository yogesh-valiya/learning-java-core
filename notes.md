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
