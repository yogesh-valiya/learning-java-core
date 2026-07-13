# Module 24 — Concurrent Collections, Atomics & Locks

> Phase 5 — Concurrency. Priority: **High**. Final module of Phase 5 — where Modules 21–23's raw tools get replaced by the purpose-built, production-grade toolkit.

Companion demo: [`module-24-concurrent-collections-atomics-locks/ConcurrentToolsDemo.java`](./module-24-concurrent-collections-atomics-locks/ConcurrentToolsDemo.java)

---

## 1. `ConcurrentHashMap`

Thread-safe `HashMap` (Module 13) alternative — but not via one global lock. Modern `ConcurrentHashMap` uses fine-grained locking scoped to individual bucket heads (plus CAS, §3), so unrelated keys can be accessed concurrently with no contention — a real difference from legacy `Hashtable`/`Collections.synchronizedMap()`, which serialize *all* access behind one lock.

Two more differences from `HashMap`:
- **Iterators never throw `ConcurrentModificationException`** (Module 15 callback) — weakly consistent/fail-safe rather than fail-fast.
- **No null keys or values** — deliberate. In a concurrent map, `get()==null` being ambiguous ("absent" vs "present with null") becomes a genuine race between a `get()` and a disambiguating `containsKey()` call; disallowing null removes the ambiguity entirely.

**The subtle point that actually matters:** a thread-safe *data structure* doesn't make a *compound operation* on it atomic. Confirmed directly in `ConcurrentToolsDemo.java` — 10 threads × 10,000 increments each, expected 100,000:

```
1) manual get+put on ConcurrentHashMap: 24976 (expected 100000)  <- LOST UPDATES
2) map.merge() on ConcurrentHashMap: 100000  <- exactly correct
```

Manual `get()` + `put()` lost **75%** of the updates — each individual call was thread-safe, but the *pair* of calls had a window another thread could interleave into. `merge()`/`putIfAbsent()`/`computeIfAbsent()`/`compute()` perform the whole read-modify-write as one indivisible operation and got it exactly right.

## 2. `BlockingQueue`

Adds **blocking** queue operations: `put()` blocks a producer when full, `take()` blocks a consumer when empty — exactly Module 22's hand-rolled `wait()`/`notifyAll()` bounded producer-consumer, built in. Confirmed: an `ArrayBlockingQueue(3)` correctly coordinated a producer and consumer through all 5 values with no manual synchronization code at all.

- `ArrayBlockingQueue` — fixed capacity, array-backed.
- `LinkedBlockingQueue` — optionally bounded, node-backed.
- `PriorityBlockingQueue` — unbounded, priority-ordered.
- `SynchronousQueue` — zero capacity, direct producer↔consumer handoff (used inside `Executors.newCachedThreadPool()`).

## 3. Atomics (`java.util.concurrent.atomic`)

Lock-free, thread-safe compound operations on a single variable. Confirmed: `AtomicInteger.incrementAndGet()` across the same 10×10,000 concurrent load landed at **exactly 100,000** — no lock required.

**Under the hood:** Compare-And-Swap (CAS) — one atomic hardware instruction: "if the value still equals X, replace with Y, tell me if it worked." `incrementAndGet()` loops internally: read, compute, CAS, retry on failure. This is **optimistic** concurrency (no thread ever blocks) vs `synchronized`'s **pessimistic** locking (a thread parks and waits) — CAS-retry typically outperforms blocking under normal contention. (`LongAdder` exists for *extreme* contention, splitting the count across cells to reduce collisions.)

## 4. `ReentrantLock` and friends

An explicit alternative to `synchronized` with real extra capabilities:

- **`tryLock()`** — non-blocking attempt; `tryLock(timeout, unit)` — bounded wait. Confirmed: while a `ReentrantLock` was held by another thread (verified via `CountDownLatch`, not a guessed sleep), `tryLock(100ms)` correctly returned `false`; after the holder released and fully exited (`join()`ed), `tryLock(1s)` returned `true`.
- **`lockInterruptibly()`** — a thread *waiting* for the lock can be interrupted out of the wait; a plain `synchronized` block offers no such escape.
- **Fairness** (`new ReentrantLock(true)`) — roughly FIFO granting vs default unfair.
- **Reentrant**, same as intrinsic locks (Module 22).
- **Must manually `unlock()` — always in `finally`.** `synchronized` releases automatically on exception; `ReentrantLock` does not. A missing `finally`-unlock is a permanently leaked lock — every other thread waiting on it blocks forever.

```java
lock.lock();
try {
    // critical section
} finally {
    lock.unlock();   // non-negotiable
}
```

**`ReentrantReadWriteLock`** — a shared read lock (concurrent readers) plus an exclusive write lock, avoiding needless serialization of reads for read-heavy, write-rare state.

## PHP contrast

No equivalent — none of Modules 21–24 has real PHP grounding, since PHP has no shared-heap concurrency model.

## Interview checklist

- `ConcurrentHashMap`: fine-grained locking (not one global lock); weakly consistent iterators (no CME); no null keys/values.
- A thread-safe data structure ≠ atomic compound operations on it — use `putIfAbsent`/`computeIfAbsent`/`compute`/`merge`, verified: manual check-then-act lost 75% of updates, `merge()` didn't lose any.
- `BlockingQueue` = Module 22's wait/notify pattern, built in (`put`/`take` block instead of throwing/returning sentinels).
- Atomics use CAS (optimistic, lock-free, retry-on-conflict); `synchronized`/`ReentrantLock` are pessimistic (blocking).
- `ReentrantLock` adds `tryLock`, `lockInterruptibly`, fairness over `synchronized` — but needs manual `unlock()` in `finally`, verified both the blocked (`tryLock`→false) and released (`tryLock`→true) cases.
- `ReadWriteLock` allows concurrent reads, exclusive writes.
