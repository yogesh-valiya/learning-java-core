# Module 1 — Java Platform & How It Runs

> Phase 0 — Orientation. Priority: **High**. The classic interview opener.

This lesson covers how Java code actually goes from source to running program: the JDK/JRE/JVM distinction, bytecode, the compile→run flow, and the JIT/HotSpot execution model.

---

## 1. The mental model: JVM ⊂ JRE ⊂ JDK

Three acronyms that are constantly confused. They nest inside one another:

| Term | What it is | Rough PHP analogy |
|------|------------|-------------------|
| **JVM** (Java Virtual Machine) | The engine that actually **executes bytecode**. It is a **platform-specific** native binary — there is a different JVM build for Linux, macOS, Windows. | The Zend Engine / opcache VM, but long-running |
| **JRE** (Java Runtime Environment) | JVM **+ the standard class libraries** (`java.lang`, `java.util`, …) needed to *run* compiled Java. | `php` CLI + bundled extensions, no dev tools |
| **JDK** (Java Development Kit) | JRE **+ developer tools**: `javac` (compiler), `javap` (disassembler), debugger, etc. This is what you install to *write and compile* Java. | `php` + Composer + Xdebug |

Key relationship: **JDK contains a JRE, which contains a JVM.**

> Note: Since Java 11, Oracle stopped shipping the JRE as a separate download — the "JDK" now bundles everything. But the conceptual split is still a common interview question, so keep the three definitions crisp.

---

## 2. The compile → run flow

Java uses an **explicit, separate compile step**, unlike PHP where compilation is implicit per request.

```bash
javac Hello.java     # compiles SOURCE  → BYTECODE   (produces Hello.class)
java  Hello          # the JVM loads & executes the bytecode
```

Minimal program:

```java
public class Hello {
    public static void main(String[] args) {
        System.out.println("Hello, Java");
    }
}
```

- `javac Hello.java` produces `Hello.class`.
- `Hello.class` is **not** native machine code — it is **bytecode**, a platform-neutral instruction set.
- `java Hello` starts a JVM, which loads and runs that bytecode.

### What bytecode looks like

Disassembling `Hello.class` with `javap -c Hello.class` shows the JVM instructions the compiler produced:

```
public static void main(java.lang.String[]);
    Code:
       0: getstatic     #7    // Field java/lang/System.out:Ljava/io/PrintStream;
       3: ldc           #13   // String Hello, Java
       5: invokevirtual #15   // Method java/io/PrintStream.println:(Ljava/lang/String;)V
       8: return
```

This `.class` file runs **unchanged** on Linux, macOS, or Windows.

---

## 3. "Write once, run anywhere" — where portability actually lives

- **Bytecode is platform-neutral.** The same `.class` file runs everywhere.
- **The JVM is platform-specific.** Each OS has its own native JVM binary that knows how to translate the neutral bytecode into what that OS/CPU needs.

So the portability boundary is the **bytecode format**, *not* the JVM binary. A `.class` compiled on Windows runs on Linux because both platforms have a JVM that understands the same bytecode.

> Interview phrasing: *"Java is platform-independent at the bytecode level; the JVM is the platform-specific layer that makes that possible."*

---

## 4. What happens when you run `java Hello`

1. **Class loading** — the classloader finds `Hello.class` and loads it into the JVM.
2. **Bytecode verification** — the JVM checks the bytecode is well-formed and not unsafe. This is a **security** step, not merely a correctness check: you cannot hand-craft illegal/malicious bytecode and have the JVM blindly trust it.
3. **Interpretation** — execution begins in the **interpreter**, which reads and executes bytecode instructions one at a time. Slow per-instruction, but starts **instantly**.
4. **JIT compilation** — the JVM profiles execution and detects **"hot"** methods (those called many thousands of times — loops, hot paths). Those hot methods are compiled by the **JIT (Just-In-Time) compiler** into **native machine code**, cached, and reused. Code that runs rarely ("cold" code) stays interpreted.

---

## 5. Is Java compiled or interpreted? → **Both**

This is a classic trick question. The honest answer is *both*:

- **Ahead-of-time:** `javac` compiles source → bytecode before the program runs.
- **At runtime:** the JVM both **interprets** bytecode *and* **JIT-compiles** hot paths → native code.

### Why interpret first instead of JIT-compiling everything on startup?

Because **JIT compilation itself costs CPU time** — analysing bytecode and generating optimized native code is expensive. If the JVM eagerly compiled every method at startup:

- Startup would be noticeably slower (you pay compilation cost before anything runs).
- You would waste that cost on methods that run only once or twice and were never going to be "hot."

So the two-tier strategy is a deliberate trade-off:

- The **interpreter** gives near-zero startup latency.
- The JVM profiles what is actually called a lot, and only pays the expensive JIT cost for **proven-hot** methods, where the runtime savings repay that cost many times over.

This is a **startup-latency vs. steady-state-throughput** trade-off. A consequence: a JVM app is often slower on request #1 than on request #10,000, because the JIT needs time to "warm up."

> **HotSpot** is the name of Oracle's default JVM implementation — named after this hot-path detection.

---

## 6. Contrast with PHP (the shift that matters most)

| | PHP | Java |
|--|-----|------|
| Compile step | Implicit — Zend compiles to opcodes internally per request; OPcache caches those opcodes to skip recompiling | Explicit, separate step (`javac`) producing a persisted, inspectable `.class` file |
| Process model | **Shared-nothing** — each request is a fresh process/thread; no state survives between requests | **Long-running process** — the JVM starts once and keeps running (minutes/days), holding state in memory the whole time |
| Warm-up | None — same speed every request | JIT warm-up; steady-state is faster than the first requests |

The **long-running process** model is the biggest mental shift coming from PHP-FPM. In Java, static fields, connection pools, caches, and thread pools all **persist for the life of the application**. This is precisely *why* Java backends care so much about **memory leaks** and **thread-safety** — problems that mostly die with the request in shared-nothing PHP.

---

## 7. Interview checklist

- **JDK vs JRE vs JVM?** — Know the nesting and one-line role of each.
- **Is Java compiled or interpreted?** — "Both": AOT to bytecode, then interpret + JIT at runtime.
- **Why is Java platform-independent if the JVM isn't?** — The abstraction boundary is the bytecode, not the JVM binary.
- **What is HotSpot?** — Oracle's default JVM, named for hot-path JIT detection.
- **Bonus:** bytecode verification is a security step.
- **Practical consequence of the long-running JVM:** you must manage memory leaks and thread-safety (unlike shared-nothing PHP).

---

## 8. Commands used in this module

```bash
javac Hello.java          # compile source → bytecode (Hello.class)
java  Hello               # run the bytecode on the JVM
javap -c Hello.class      # disassemble the class to view JVM bytecode
java -version             # check the installed runtime version
```
