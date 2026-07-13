# Module 21 — Threads

> Phase 5 — Concurrency. Priority: **High**. Opens the phase the roadmap calls the senior differentiator.

Companion demo: [`module-21-threads/ThreadDemo.java`](./module-21-threads/ThreadDemo.java)

---

## 0. The mental shift

**PHP is shared-nothing** — every request gets a fresh interpreter, fresh memory; no state survives between requests, no two requests ever touch the same variable at the same time. **A JVM is one long-running process, and threads within it share the same heap** — the same objects, the same static fields, genuinely at the same time. There is no PHP experience that maps onto "two threads incrementing the same counter simultaneously." This phase is about reasoning over state multiple execution paths can touch concurrently — a concern that essentially doesn't exist in typical PHP work.

## 1. Creating a thread

```java
// (A) extend Thread, override run() -- burns your one superclass slot, rarely used
class Worker extends Thread {
    public void run() { System.out.println("working"); }
}

// (B) implement Runnable, hand it to a Thread -- the normal approach
Runnable task = () -> System.out.println("working");
Thread t = new Thread(task);
t.start();
```

`Runnable` separates *the task* from *the thing that executes it* — reusable across `Thread`, `ExecutorService` (Module 23), anywhere. Extending `Thread` directly is mostly legacy.

## 2. `start()` vs `run()` — the gotcha that catches almost everyone once

```java
Thread t = new Thread(() -> System.out.println("running on: " + Thread.currentThread().getName()));
t.run();     // NOT a new thread -- an ordinary method call on the CURRENT thread
t.start();   // spawns a new OS-backed thread, which then calls run() on it
```

Confirmed in `ThreadDemo.java`: `t1.run()` printed `"run() executes on: main"`; `t2.start()` printed `"start() executes on: Thread-1"` — a genuinely different thread. `.run()` compiles and executes without error, which is exactly the trap: it silently does the wrong thing.

## 3. `Runnable` vs `Callable<V>`

| | `Runnable` | `Callable<V>` |
|---|---|---|
| Method | `void run()` | `V call()` |
| Return value | none | yes, `V` |
| Checked exceptions | can't declare any | can throw them |

A `Callable` can't be handed directly to a `Thread` constructor — the bridge is `FutureTask<V>`, which implements both `Runnable` and `Future<V>`: wrap the `Callable`, hand the `FutureTask` to a `Thread`, then call `.get()` to retrieve the result (blocking until ready). Confirmed: a `Callable<Integer>` sleeping 50ms then returning `6*7`, run via `Thread(futureTask)`, produced `futureTask.get() == 42`. This is the direct predecessor to `ExecutorService.submit()` (Module 23).

## 4. Thread lifecycle — `Thread.State`

```
NEW -> RUNNABLE -> (BLOCKED | WAITING | TIMED_WAITING) -> TERMINATED
```

- **NEW** — created, not started.
- **RUNNABLE** — started; executing or merely eligible to (Java doesn't distinguish the two — OS scheduling is invisible to this model).
- **BLOCKED** — waiting to acquire a monitor lock held by another thread (Module 22).
- **WAITING** — waiting indefinitely: `Object.wait()` (no timeout), `Thread.join()` (no timeout), `LockSupport.park()`.
- **TIMED_WAITING** — same, bounded: `Thread.sleep(ms)`, `wait(ms)`, `join(ms)`.
- **TERMINATED** — `run()` completed. **Cannot be restarted** — confirmed: calling `.start()` again on a terminated thread threw `IllegalThreadStateException`. A `Thread` object is one-shot.

Confirmed the full transition live: `NEW` before `.start()` → `TIMED_WAITING` while the thread was inside `Thread.sleep(300)` → `TERMINATED` after `.join()` returned.

## 5. Core methods

- **`join()`** — caller blocks until the target thread reaches `TERMINATED`. The classical "wait for a thread to finish."
- **`sleep(ms)`** — pauses the *current* thread. **Does not release any locks it holds** (contrast: `wait()` does release the monitor — Module 22).
- **`interrupt()`** — a **cooperative** signal, not a forcible stop. Sets the target's interrupt flag; a thread blocked in `sleep()`/`wait()`/`join()` wakes immediately with `InterruptedException` (confirmed: interrupting a sleeping thread produced exactly this). A thread running ordinary code gets nothing automatically — it must check `isInterrupted()` itself. Java has no forcible thread-kill (`Thread.stop()` is deprecated — killing mid-operation can corrupt shared state).
- **`setDaemon(true)`** (before `.start()`) — background thread; doesn't keep the JVM alive by itself.

## PHP contrast

No close equivalent — PHP-FPM workers are separate OS processes with separate memory, not threads sharing a heap; the pthreads extension is niche. Genuinely new territory.

## Interview checklist

- Threads in one JVM share the same heap — the root reason concurrency bugs exist; no PHP-request analogy.
- Prefer `Runnable` + `Thread` over extending `Thread`.
- `.run()` is a plain method call on the current thread; only `.start()` spawns a real thread.
- `Callable<V>` returns a value / can throw checked exceptions; `Runnable` can't. `FutureTask<V>` bridges a `Callable` onto a `Thread`.
- Lifecycle: NEW → RUNNABLE → (BLOCKED/WAITING/TIMED_WAITING) → TERMINATED; terminated threads can't restart.
- `sleep()` doesn't release locks; `wait()` does (Module 22).
- `interrupt()` is cooperative — only acts if the target is blocked in an interruptible call or explicitly checks its flag.
- Daemon threads don't keep the JVM alive; must be set before `.start()`.
