# Module 23 — Executors & Thread Pools

> Phase 5 — Concurrency. Priority: **High**. Builds directly on Module 21's raw `Thread`/`Callable`/`FutureTask` — the managed, production-grade version of exactly that.

Companion demo: [`module-23-executors-thread-pools/ExecutorDemo.java`](./module-23-executors-thread-pools/ExecutorDemo.java)

---

## 1. Why not just create `Thread`s directly

Module 21's approach (`new Thread(task).start()` per task) doesn't scale: OS thread creation has real overhead, no reuse, nothing bounds how many pile up. `ExecutorService` is a managed pool of reusable worker threads plus a task queue.

## 2. Creating one

```java
Executors.newFixedThreadPool(4);   // fixed thread count, unbounded queue
Executors.newCachedThreadPool();    // grows as needed, reuses idle threads
Executors.newSingleThreadExecutor(); // one thread, strict order
Executors.newScheduledThreadPool(2); // delayed / periodic tasks
```

**A real caution:** these convenience factories hide unbounded resource growth — `newFixedThreadPool`'s queue is unbounded (tasks can pile up faster than processed, risking OOM under overload); `newCachedThreadPool`'s thread count is unbounded. Production code increasingly constructs `ThreadPoolExecutor` directly with an explicit bounded queue and rejection policy. (Virtual threads change this calculus further — Module 28.)

## 3. `execute()` vs `submit()` — the gotcha that hides real bugs

`execute(Runnable)` is fire-and-forget — an exception thrown inside goes to the thread's uncaught-exception handling, trivially missed in production.

`submit()` returns a `Future<V>` — but the exception doesn't disappear, it **relocates**: captured silently, surfacing only when `.get()` is called (wrapped in `ExecutionException`, real exception as `getCause()`). Confirmed directly in `ExecutorDemo.java`:

```
2) submitted a failing task, did NOT call get() -- no exception surfaced here
3) calling get() surfaced: ExecutionException, cause: java.lang.RuntimeException: surfaced failure
```

Two tasks threw the identical kind of exception — one vanished completely (never touched), the other surfaced in full (`.get()` called). **Never calling `.get()` on a submitted failing task hides the failure completely** — a very common real "why did my background task silently fail" bug.

```java
future.get();                     // blocks; returns result or throws ExecutionException
future.get(2, TimeUnit.SECONDS);  // bounded wait -> TimeoutException if not done
future.cancel(true);              // attempt cancellation; true = interrupt if running
```

## 4. Shutdown lifecycle

- **`shutdown()`** — graceful: stop accepting new tasks, let queued/running ones finish.
- **`shutdownNow()`** — interrupt running tasks, return unstarted ones.
- **`awaitTermination(timeout, unit)`** — block until termination or timeout.

Confirmed: submitting to a pool after `shutdown()` threw `RejectedExecutionException` immediately. **A real, common bug:** forgetting to call `shutdown()` at all — pool threads aren't daemons by default, so an un-shut-down executor keeps the JVM alive indefinitely even after `main()` returns.

## 5. `CompletableFuture` — modern async composition

```java
CompletableFuture.supplyAsync(() -> 10).thenApply(x -> x + 5).thenApply(x -> x * 2);  // -> 30, confirmed
```

- `supplyAsync`/`runAsync` — start async work (default `ForkJoinPool.commonPool()`, or a supplied executor).
- `.thenApply(fn)` transforms the result; `.thenAccept(consumer)` consumes it; `.thenRun(runnable)` ignores it.
- `.thenCompose(fn)` — for a step that itself returns a `CompletableFuture` (the `flatMap` of this API — avoids a nested `CompletableFuture<CompletableFuture<T>>`).
- `.thenCombine(other, fn)` — merge two independent futures once both finish. Confirmed: `supplyAsync(3).thenCombine(supplyAsync(4), Integer::sum)` → `7`.
- `.exceptionally(fn)` — recover with a fallback value. Confirmed: a `supplyAsync` that throws, followed by `.exceptionally(ex -> -1)`, resolved to `-1` rather than propagating. `.handle((result, ex) -> ...)` handles both outcomes in one place; `.whenComplete(...)` is a side-effect-only observer.
- Every `then*` has an `*Async` twin — the plain version runs on whichever thread completed the previous stage; `*Async` runs on the pool (or a specified executor) instead.

## PHP contrast

Nothing in core PHP maps to this — ReactPHP/Amp promises are third-party event-loop libraries, not a language-runtime feature the way `java.util.concurrent` is.

## Interview checklist

- `ExecutorService` = managed, reusable thread pool + task queue, over hand-rolled `Thread`s.
- `Executors` factories hide unbounded queues/thread growth — a known production risk.
- `execute()` swallows exceptions into the uncaught-handler; `submit()` captures them into the `Future`, surfacing only on `.get()` — verified both the silent-loss and the surfaced-`ExecutionException` cases side by side.
- `shutdown()` (graceful) vs `shutdownNow()` (interrupt + unstarted list); forgetting to shut down keeps the JVM alive; post-shutdown `submit()` throws `RejectedExecutionException` (verified).
- `CompletableFuture`: `thenApply`/`thenAccept`/`thenRun`/`thenCompose`/`thenCombine` plus `exceptionally`/`handle`/`whenComplete`; `*Async` variants control which executor runs the continuation.
