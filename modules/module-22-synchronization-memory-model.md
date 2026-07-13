# Module 22 — Synchronization & Memory Model

> Phase 5 — Concurrency. Priority: **High**. The roadmap flags this one specifically: **the big separator** between "knows Java syntax" and "can be trusted with concurrent code."

Companion demos:
- [`module-22-synchronization-memory-model/SynchronizationDemo.java`](./module-22-synchronization-memory-model/SynchronizationDemo.java) — race conditions, `volatile` vs `synchronized`, reentrancy, static-vs-instance locks
- [`module-22-synchronization-memory-model/DeadlockAndWaitNotifyDemo.java`](./module-22-synchronization-memory-model/DeadlockAndWaitNotifyDemo.java) — detected deadlock, `wait`/`notify` producer-consumer

---

## 1. Race conditions — the actual problem

A race condition happens when multiple threads read and write shared mutable state without coordination, and the outcome depends on the unpredictable interleaving of their operations. `count++` **looks like one operation but isn't** — it's three: read, add 1, write back. Two threads can each read the same value before either writes back, and an increment gets silently **lost**.

Confirmed in `SynchronizationDemo.java` — 10 threads × 100,000 increments each, expected total 1,000,000:

```
1) expected total: 1000000
2) plain int (unsynchronized): 553637  <- LOST UPDATES
```

Nearly half the increments vanished. This is a deterministic, reproducible bug at this scale — not a rare edge case.

## 2. `synchronized` — mutual exclusion

Every object carries an intrinsic lock ("monitor"). `synchronized` acquires it on entry, releases it on exit — including via an exception.

```java
synchronized void increment() { count++; }        // locks on 'this'
synchronized (someObject) { ... }                  // locks on an arbitrary object — finer-grained
static synchronized void factoryMethod() { ... }    // locks on the Class object, NOT an instance!
```

Confirmed the fix: the same 10×100,000 increments, guarded by `synchronized`, landed at **exactly** 1,000,000 (`SynchronizationDemo.java`, output 4).

**Instance `synchronized` and `static synchronized` use different locks** (`this` vs `ClassName.class`) — they do not exclude each other. Proven directly: a thread held an instance method's lock (via a `CountDownLatch` guaranteeing it had genuinely acquired it), and a `static synchronized` method on the same class returned in **0 ms** without waiting on it at all.

**Reentrancy:** Java's intrinsic locks are reentrant — a thread already holding a lock can enter another `synchronized` block on the same lock without self-deadlocking (a per-thread hold count is tracked). Confirmed: `reentrantOuter()` calling `reentrantInner()` (same object, same thread) completed with no issue.

## 3. `volatile` — visibility, not atomicity

`volatile` guarantees a write by one thread is immediately visible to other threads' reads — it stops a thread from working off a stale cached value. It does **not** make a compound read-modify-write atomic. Confirmed directly — the *exact same race* that broke the plain `int` also broke a `volatile int`:

```
3) volatile int (visibility only): 315236  <- LOST UPDATES (volatile != atomic)
```

Visibility of each individual read/write doesn't stop two threads from interleaving across the three-step increment. **Rule of thumb:** `volatile` for simple independent flags (`volatile boolean running`); `synchronized` or `Atomic*` types (Module 24) for anything involving a compound update.

## 4. Deadlock

Classic shape: Thread A holds Lock1, waits for Lock2; Thread B holds Lock2, waits for Lock1 — circular wait, neither proceeds. Fix: **always acquire multiple locks in the same global order** everywhere in the codebase, which makes circular wait structurally impossible.

Triggered and detected programmatically via the JDK's own `ThreadMXBean.findDeadlockedThreads()`:

```
1) ThreadMXBean detected 2 deadlocked threads:
   t1-locksA-then-B is blocked on java.lang.Object@..., owned by t2-locksB-then-A
   t2-locksB-then-A is blocked on java.lang.Object@..., owned by t1-locksA-then-B
```

The circular ownership is exactly the bug: each thread is blocked on a lock the *other* one owns.

## 5. `wait()`/`notify()`/`notifyAll()`

The classical mechanism for one thread to wait on a condition another thread will make true.

- Must be called inside a `synchronized` block on the **same object**, or `IllegalMonitorStateException`.
- `wait()` **releases the monitor** (the key difference from `sleep()`, which holds all locks — Module 21) and suspends until `notify()`/`notifyAll()` is called on that object *and* the thread re-acquires the lock.
- `notify()` wakes **one** arbitrary waiting thread; `notifyAll()` wakes **all** of them. `notifyAll()` is the safer default — `notify()` risks waking the "wrong" thread and starving a legitimately-waiting one.
- **Always guard `wait()` with a `while` loop checking the real condition, never `if`** — the JVM is explicitly permitted to produce a **spurious wakeup** (a documented, real behavior, not hypothetical).

Confirmed with a bounded (`CAPACITY = 3`) producer-consumer queue: the producer correctly blocked once the queue filled, the consumer drained it, both finished cleanly with a `while`-guarded `wait()`/`notifyAll()` pair. Modern code reaches for `BlockingQueue` (Module 24) instead of hand-rolling this, but the mechanism underneath is identical.

## PHP contrast

None of this has a PHP analogue — there's no shared mutable heap across simultaneous execution paths in ordinary PHP.

## Interview checklist

- A race condition is a real, reproducible bug from unsynchronized compound operations — verified at ~45% lost updates in this demo.
- `synchronized` = mutual exclusion via an object's intrinsic monitor; reentrant; instance lock (`this`) ≠ static lock (`ClassName.class`) — proven not to exclude each other.
- `volatile` = visibility only — proven it does **not** fix a compound-operation race.
- Deadlock requires circular wait; fix = a consistent global lock-acquisition order; `ThreadMXBean.findDeadlockedThreads()` can detect one programmatically.
- `wait()` releases the monitor (unlike `sleep()`); always guard with `while`, never `if` — spurious wakeups are real and documented.
- `notifyAll()` is the safer default over `notify()`.
