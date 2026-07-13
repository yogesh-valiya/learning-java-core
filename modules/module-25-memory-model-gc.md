# Module 25 — Memory Model & GC

> Phase 6 — JVM Under the Hood. Priority: **High**. Expected knowledge for experienced roles specifically — where "I write Java" becomes "I understand what Java is actually doing."

Companion demos:
- [`module-25-memory-model-gc/StackOverflowDemo.java`](./module-25-memory-model-gc/StackOverflowDemo.java)
- [`module-25-memory-model-gc/HeapOOMDemo.java`](./module-25-memory-model-gc/HeapOOMDemo.java)

---

## 1. Heap vs Stack vs Metaspace

- **Stack** — one per thread. Local variables and method call frames: primitives directly, objects as **references** (the object itself is never on the stack). Strictly LIFO, reclaimed immediately on method return, no GC involved. Bounded — exceed it and get `StackOverflowError`. Confirmed live: unbounded recursion threw `StackOverflowError` after **16,118** frames.
- **Heap** — one per JVM, shared across all threads. Every object lives here, GC-managed. Nothing is reclaimed just because a method returns — an object survives exactly as long as something can still reach it.
- **Metaspace** (Java 8+, replaced PermGen) — class metadata (bytecode, constant pool). Lives in **native (off-heap) memory**, not bounded by `-Xmx` — grows until `-XX:MaxMetaspaceSize` or the OS runs out. Fixed the classic "PermGen space" OOM from repeated app-server hot-redeploys.

## 2. The generational hypothesis

Most objects die young. The heap exploits this:

- **Young generation** (Eden + two Survivor spaces) — everything allocated here first; collected frequently via **Minor GC** (fast — most young objects are already garbage).
- **Old/Tenured generation** — objects surviving enough Minor GC cycles get **promoted** (tracked via a per-object age counter). Collected less often via **Major/Full GC** — far more expensive, scanning a much larger region.

## 3. GC algorithms — the practical landscape

| Collector | Character |
|---|---|
| Serial | single-threaded, full stop-the-world |
| Parallel | multi-threaded stop-the-world, throughput-focused |
| **G1** | **default since Java 9** — region-based, predictable pause targets, mostly concurrent |
| ZGC / Shenandoah | sub-millisecond pauses even on huge heaps, fully concurrent — latency-critical services |

**Stop-the-world (STW):** application threads pause during certain GC phases — a real cost even for "concurrent" collectors, which still have shorter STW phases. "Concurrent" means most work happens alongside the app, not zero pausing.

## 4. Memory leaks — yes, really, even with a GC

A GC only reclaims what's **truly unreachable**. If anything still holds a reference, the GC can't touch it — a Java "leak" is unintentional reachability, not a different mechanism than a C-style leak in practical effect. Real patterns:

- A `static` collection used as a cache with **no eviction** — only ever grows.
- Registered listeners/callbacks never unregistered — direct callback to Module 10's inner-class leak (same "something long-lived holds a reference" shape).
- Unclosed resources — direct callback to Module 20.
- `ThreadLocal` values set but never removed on a **pooled** thread — the thread outlives the logical request, silently carrying the reference into the next unrelated task. A classic, still-current app-server leak source.

## 5. `OutOfMemoryError` varieties, and a related `Error` that isn't one

- **`OutOfMemoryError: Java heap space`** — heap genuinely exhausted. Confirmed live under a constrained `-Xmx32m`: `OutOfMemoryError: Java heap space` after allocating ~30 MB into a `List<byte[]>` that kept every chunk reachable.
- **`OutOfMemoryError: Metaspace`** — too many loaded classes, often a classloader leak.
- **`OutOfMemoryError: GC overhead limit exceeded`** — the JVM spent ~98%+ of time in GC reclaiming almost nothing; its way of saying "I'm thrashing."
- **`StackOverflowError`** — a sibling `Error`, not an `OutOfMemoryError`, but conceptually related: resource exhaustion, on the (much smaller) stack instead.

**A genuinely instructive accident while building the heap-OOM demo:** the first version's `catch (OutOfMemoryError e)` block tried to `System.out.println(...)` immediately — and that `println` needed to allocate a `String`, which threw a **second** `OutOfMemoryError`, uncaught, crashing the program before the log line ever printed. At the true edge of heap exhaustion, even the code handling the failure can fail the same way. The fix: release the held memory (`holder.clear(); holder = null;`) *before* attempting any further allocation in the handler.

## 6. Basic tuning, practically

- `-Xms`/`-Xmx` — initial/maximum heap size.
- `-XX:+UseG1GC` (or another collector flag).
- `-XX:MaxMetaspaceSize` — bound Metaspace explicitly.
- `-XX:+HeapDumpOnOutOfMemoryError` — auto-dump the heap on OOM, the standard first step for diagnosing a real production leak.

## PHP contrast

PHP's per-request reference counting + cycle collector scopes most memory to the request's lifetime by default — most of §4's leak patterns (a growing static cache, a `ThreadLocal` on a pooled thread) don't have a direct PHP equivalent.

## Interview checklist

- Stack = per-thread, LIFO, automatic reclaim, bounded (`StackOverflowError`, confirmed at 16,118 frames here). Heap = shared, GC-managed. Metaspace = off-heap class metadata, replaced PermGen.
- Generational hypothesis: young gen collected often/cheaply (Minor GC); old gen collected rarely/expensively (Major/Full GC); promotion via an age counter.
- G1 is the default collector since Java 9; ZGC/Shenandoah target sub-millisecond pauses; every collector has some stop-the-world phase.
- A GC prevents nothing on its own — a "leak" is unintended reachability. Know the patterns: unbounded static caches, unremoved listeners, unclosed resources, `ThreadLocal` on pooled threads.
- `OutOfMemoryError` has distinct flavors (heap — confirmed live, Metaspace, GC-overhead) pointing to different root causes; even the OOM-handling code itself can OOM if it allocates before freeing anything.
