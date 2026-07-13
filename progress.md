# Java Core — Progress Tracker

**Goal:** Job-ready for Java backend roles (India), interview-optimized.
**Scope:** Java Core only — Spring Boot is a separate project.
**How to use:** This is the live tracker. To resume, start a session and say "let's continue" — the `java-core-mentor` skill reads this file and picks up from "Next up". (See `README.md` for the repo map, `SKILL.md` for the teaching method.)

**Marking:** check done with `[x]`; add 🔄 next to the one in progress. Priority: **[H]**igh / **[M]**edium / **[L]**ow.

---

## Roadmap

### Phase 0 — Orientation (fast-track) · ~2 sessions
- [x] 1. Java platform & how it runs — JDK/JRE/JVM, bytecode, JIT, compile→run flow · **[H]** · classic opener Q
- [x] 2. Syntax map for PHP devs + Maven basics — types, packages, `main`, arrays vs collections, Maven lifecycle/deps · **[L]** · fast-tracked cheat-sheet

### Phase 1 — Core language & OOP the Java way · ~6 sessions
- [x] 3. Primitives, wrappers, autoboxing; `==` vs `.equals()` · **[H]** · constant gotcha source
- [x] 4. Strings — immutability, string pool, StringBuilder/StringBuffer · **[H]** · very common
- [x] 5. Classes & OOP mechanics — access modifiers, static, final, this/super, overload vs override rules · **[H]** · map from PHP, focus on Java rules
- [x] 6. Interfaces vs abstract classes — default/static methods, "multiple inheritance of type" · **[H]** · favorite design Q
- [x] 7. equals() & hashCode() contract + Object methods (toString, getClass) · **[H]** · extremely frequent
- [x] 8. Building immutable classes — defensive copying, why it matters · **[M]** · common design task
- [x] 9. Enums (real Java enums, not PHP-style) + EnumMap/EnumSet · **[M]**
- [x] 10. Nested & anonymous classes · **[M]** · needed to read real code + understand pre-lambda style

### Phase 2 — Generics & Collections (interview core) · ~5 sessions
- [x] 11. Generics — bounded types, wildcards (`? extends`/`? super`), PECS, type erasure · **[H]**
- [x] 12. Collections overview — List/Set/Queue/Map hierarchy; ArrayList vs LinkedList · **[H]**
- [x] 13. HashMap internals — buckets, hashing, treeify (Java 8), resize, load factor · **[H]** · the #1 internals Q
- [x] 14. Set/Map variants — HashSet, LinkedHashMap, TreeMap; when to use which · **[H]**
- [x] 15. Comparable vs Comparator, sorting; iterators, fail-fast vs fail-safe · **[H]**

### Phase 3 — Modern functional Java · ~4 sessions
- [x] 16. Lambdas & functional interfaces (Function/Predicate/Consumer/Supplier) + method references · **[H]**
- [x] 17. Streams API — map/filter/reduce, Collectors (groupingBy/joining/toMap), flatMap, parallel · **[H]**
- [x] 18. Optional — right usage, avoiding null, common mistakes · **[M]**
- [x] 19. Date/Time API (java.time) — LocalDate/Time, Duration/Period, formatting · **[M]**

### Phase 4 — Exceptions · ~2 sessions
- [x] 20. Exception handling — checked vs unchecked, try-with-resources, custom exceptions, finally semantics, best practices · **[H]**

### Phase 5 — Concurrency (senior differentiator) · ~6 sessions
- [x] 21. Threads — lifecycle, Runnable vs Callable, core methods · **[H]**
- [x] 22. Synchronization & memory model — synchronized, volatile, race conditions, deadlock, wait/notify · **[H]** · big separator
- [ ] 23. Executors & thread pools — ExecutorService, Future, CompletableFuture, pool types · **[H]**
- [ ] 24. Concurrent collections, atomics & locks — ConcurrentHashMap, BlockingQueue, ReentrantLock, atomics · **[H]**

### Phase 6 — JVM under the hood · ~3 sessions
- [ ] 25. Memory model & GC — heap/stack, generations, GC algorithms, memory leaks, OOM, basic tuning · **[H]** · expected for experienced roles
- [ ] 26. Class loading, reflection & annotations (create + process) · **[M]** · annotations bridge to Spring

### Phase 7 — Rounding out · ~3 sessions
- [ ] 27. I/O & serialization — File/stream I/O, NIO basics, Serializable/transient pitfalls · **[M]**
- [ ] 28. Modern Java 11–21 — var, records, sealed classes, pattern matching, switch expressions, text blocks, virtual threads (overview) · **[M]** · increasingly asked

### Phase 8 — Interview consolidation · ~2 sessions
- [ ] 29. Rapid-fire "X vs Y" + coding drills + Java-8-vs-modern talking points + prep to start the Spring Boot project · **[H]**

**Total: ~29 modules, ≈30 sessions.**

---

## Deliberately skipped / fast-tracked (given 6 yrs PHP)
- **OOP theory** (encapsulation/inheritance/polymorphism definitions) — only Java's *implementation* is taught, not the concepts.
- **Design patterns as a subject** — referenced where Java idioms use them (Builder, Singleton, Iterator), not taught from scratch.
- **Loops/conditionals/operators** — collapsed into the Module 2 cheat-sheet.
- **Raw JDBC & SQL** — kept minimal; Spring Data replaces hand-written DAOs in the next project. You'll know the concept, not grind it.
- **Deep Maven config, HTTP/REST theory, Git** — already yours or deferred to the Spring project.

**Spring boundary:** DI/IoC container, Spring MVC, Spring Data, Boot autoconfig = the separate project. Core stops at plain-Java annotations + reflection so you understand what Spring does underneath.

---

## Current status
- **Just finished:** Module 22 — Synchronization & memory model (race conditions measured, synchronized fix measured, volatile-visibility-not-atomicity measured, static-vs-instance lock separation proven, deadlock triggered+detected via ThreadMXBean, wait/notify producer-consumer).
- **Next up:** Module 23 — Executors & thread pools (ExecutorService, Future, CompletableFuture, pool types)
- **Sessions done:** 22

## Struggle log
_Topics that didn't fully click — revisit / spaced repetition._
-

## Interview drill queue
_Questions & gotchas the mentor flagged that I want to re-practice._
- `==` vs `.equals()` — `==` compares references for objects; always `.equals()` for value
- String pool: literals are shared/interned; `new String()` forces a new object
- Integer cache −128..127: `Integer ==` passes for small values, fails above 127 (prod bug)
- Unboxing a `null` wrapper into a primitive → NPE (no visible method call)
- String immutability: 4 reasons (pool safety, thread-safety, cached hashCode, security)
- `String +=` in a loop = O(n²) → use StringBuilder (measured ~1400× faster for 100k chars)
- StringBuilder (not thread-safe, default) vs StringBuffer (synchronized, legacy)
- Default access = package-private (no PHP equivalent); Java `protected` also grants package access
- `final` reference ≠ immutable object (can still mutate the object)
- Static methods are hidden (declared type), instance methods overridden (runtime type)
- Constructor chaining: implicit `super()`; no-arg-parent gotcha; super-first construction order
- Overloading = compile-time/declared type; Overriding = runtime/actual object
- Interface vs abstract class: durable diff = abstract class has state + constructors
- Why default methods (Java 8): backward compat (e.g. Collection.stream())
- Diamond problem: two same-name default methods → must override, use `Interface.super.method()`
- Override equals → must override hashCode (bucket-then-equals; else silent break)
- `equals(MyType)` = overload not override; `@Override` catches it (real sig = `equals(Object)`)
- getClass (symmetric, breaks Liskov) vs instanceof (can break symmetry) — make value types final
- Mutable-key trap: mutating a hashCode field strands a HashMap entry → keys must be immutable
- Immutable class: `final` freezes the reference, not the object → defensive-copy mutable fields in AND out
- `List.copyOf` = true copy; `Collections.unmodifiableList` = view (original can still mutate)
- Records don't auto-copy mutable components → add a compact constructor
- Enums: `==` is preferred (singleton, null-safe, compile-checked)
- Per-constant abstract method bodies beat switch (compiler-enforced completeness)
- EnumMap (ordinal array, declaration order) / EnumSet (bitvector) > HashMap/HashSet for enum keys
- Never persist ordinal() (use name()/code); single-element enum = best Singleton
- Static nested (no outer ref) vs inner (hidden outer ref, needs `outer.new Inner()`)
- Inner class memory leak: pins outer alive → make it `static` if outer not needed
- Effectively final capture: local = frozen copy; outer field = live read
- Bounded type param = permission slip (enables method calls), not just a filter; unbounded `<T>` defaults to `<T extends Object>` — that's the bound, not "inference"
- Invariance: `List<Integer>` is not a `List<Number>` — prevents smuggling a wrong type in via a supertype reference
- PECS: Producer `extends` (read from it), Consumer `super` (write to it) — `Collections.copy(dest super, src extends)`
- Type erasure: generics erased at compile time for backward compat; `Box<String>`/`Box<Integer>` share one runtime class
- Erasure consequences: no `new T()`, no `instanceof List<String>`, no generic arrays, no overload-by-type-param-only, no `T` in `static` context
- Bridge methods: compiler-generated erased-signature overload preserving polymorphism after overriding a generic method
- Raw types bypass all checks; the `ClassCastException` fires at the read site (compiler-inserted cast), not where the bad value was stored
- Map does NOT extend Collection (pairs vs. single elements); reachable via keySet()/values()/entrySet()
- Set uniqueness = equals()/hashCode() contract, not interface magic
- LinkedList implements both List AND Deque at once — genuinely dual-purpose
- ArrayList: O(1) get, amortized O(1) append (1.5x resize); LinkedList: O(1) at ends, O(n) get, true O(1) insert ONLY via a positioned iterator
- RandomAccess marker interface: ArrayList has it, LinkedList doesn't; JDK algorithms branch on it
- `get(i)` loop over LinkedList = O(n²) anti-pattern (measured ~318x slower than iterator) — always for-each/iterator unless RandomAccess is guaranteed
- Modern default: prefer ArrayList almost always; prefer ArrayDeque over LinkedList for real queue/stack/deque use
- Casting a Queue reference to List for index access is a smell — only works if the concrete class implements both; breaks on ArrayDeque
- HashMap bucket index = (capacity-1) & hash (bitmask, not modulo) — requires power-of-two capacity
- hash() spreading (h ^ h>>>16) folds high bits down since bucket indexing only reads low bits
- Treeify at 8 entries + capacity>=64 — defense against bad/malicious hashCode, not normal-case behavior
- Treeification needs a real ordering signal (differing hashes or Comparable) — constant hashCode for every key still degrades far past O(log n)
- Constant-but-consistent bad hashCode = performance bug; inconsistent hashCode = correctness bug (silently unreachable entries)
- Load factor 0.75 = space/time tradeoff; resize doubles capacity, splits buckets via one new bit (lo/hi), no full rehash
- new HashMap<>(n) rounds UP to next power of two (tableSizeFor)
- Pre-size HashMap when count is known to skip resize-copy cascade — benchmark via separate JVM processes, not in-process back-to-back (JIT/GC bleed gives unreliable results)
- HashSet is a thin wrapper over HashMap<E,Object> (dummy PRESENT value) — same guarantees/gotchas as HashMap
- LinkedHashMap threads a doubly-linked list through the same hash nodes; accessOrder=true + removeEldestEntry override = LRU cache
- TreeSet/TreeMap are a real always-on red-black tree — genuinely guaranteed O(log n) since Comparable/Comparator is mandatory (unlike HashMap's best-effort treeify)
- TreeMap/TreeSet reject null (NPE on compareTo); HashMap/HashSet allow one null key
- Comparable (compareTo) = one natural order, in the class; Comparator (compare) = external, pluggable, any number per type
- Comparator chaining idiom: comparing().thenComparing().reversed() — know this syntax cold
- Sorted collections (TreeSet/TreeMap) use compareTo/compare EXCLUSIVELY for equality, not equals/hashCode — mismatched, entries silently collapse (measured: 2 different Persons, same age -> TreeSet size 1)
- Object sort (TimSort) is stable; primitive Arrays.sort (dual-pivot quicksort) is not, but stability is moot for primitives
- Iterator.remove() (or removeIf) is the only safe way to remove mid-iteration
- Fail-fast (modCount + ConcurrentModificationException) = best-effort bug detection, NOT a correctness guarantee
- Fail-safe (CopyOnWriteArrayList, ConcurrentHashMap) trades CME-freedom for possibly-stale iteration
- Lambda 'this' = enclosing instance (lexical); anonymous class 'this' = its own instance (verified: getSimpleName() empty for anonymous, enclosing class name for lambda)
- Lambdas compile via invokedynamic/LambdaMetafactory at runtime, not a .class file per lambda like anonymous classes
- andThen = receiver runs first, then argument; compose = argument runs first, then receiver
- Unbound-instance method reference (String::toUpperCase) — the lambda's parameter BECOMES the receiver, easy to confuse with a static reference
- Stream = lazy pipeline, not a data structure; single-use (2nd terminal op throws IllegalStateException)
- Processing is vertical (one element through ALL stages) and short-circuits on findFirst/anyMatch/limit — not stage-by-stage over the whole collection
- map = 1-to-1; flatMap = 1-to-many + flattens
- reduce's 3-arg overload (identity, accumulator, combiner) exists for parallel streams
- groupingBy + downstream collector (counting/mapping/nested groupingBy) reshapes each bucket — the practical power move
- Parallel streams aren't automatically faster; shared mutable state in the lambda is a race condition waiting to happen
- Optional is a RETURN-TYPE signal for possible absence, not a general null replacement — never use as a field or parameter
- orElse(x) evaluates x eagerly always; orElseGet(supplier) only invokes on empty — measured: orElse ran an "expensive" call even when present, orElseGet did not
- isPresent()+get() is a null-check with extra steps — use map/filter/orElse/orElseThrow/ifPresentOrElse instead
- Never wrap a collection in Optional (Optional<List<T>>) — return an empty collection instead
- java.time replaced Date/Calendar for immutability, thread-safety, and fixing 0-indexed months
- Duration (time-based, exact) vs Period (date-based, calendar-aware) — NOT interchangeable: measured Period.ofMonths(1) on Jan 31 clamps to Feb 28, Duration.ofDays(1) gives a fixed 24h (Feb 1)
- java.time types are immutable, same reassignment discipline as String
- DateTimeFormatter is thread-safe; old SimpleDateFormat is NOT (shared instance across threads silently corrupts results)
- ChronoUnit.between = single-unit raw count; Period.between = full calendar breakdown (years+months+days)
- PHP has no checked exceptions — Java's checked/unchecked split and compiler enforcement is PHP-dev-new territory
- try-with-resources closes multiple resources in REVERSE declaration order
- If try throws AND close() also throws, the ORIGINAL exception wins and propagates; close()'s exception is suppressed (addSuppressed/getSuppressed), not swapped in
- Custom exceptions must chain the cause (super(message, cause)) — never swallow the original when wrapping
- return/throw INSIDE finally silently swallows any in-flight exception with zero trace — verified, never do this
- Checked exceptions are controversial — interact badly with lambdas/streams (can't throw checked from most functional interfaces without wrapping)
- JVM threads share ONE heap (unlike PHP's shared-nothing per-request model) — root cause of every concurrency bug
- .run() is a plain method call on the current thread (no new thread); only .start() spawns a real thread — verified via thread names
- Callable<V> returns a value/can throw checked exceptions; Runnable can't. FutureTask bridges a Callable onto a Thread, .get() retrieves the result
- Thread lifecycle: NEW -> RUNNABLE -> (BLOCKED/WAITING/TIMED_WAITING) -> TERMINATED; terminated threads can't restart (IllegalThreadStateException)
- sleep() does NOT release held locks (contrast wait(), which does); interrupt() is cooperative only — no-op on a thread not blocked in an interruptible call unless it checks isInterrupted() itself
- Race condition measured: 10 threads x 100k increments on a plain int, expected 1M, got ~553k (lost updates) — count++ is 3 non-atomic steps
- synchronized fixes it exactly (measured 1,000,000/1,000,000); volatile does NOT (measured ~315k, still lost updates) — volatile is visibility only, not atomicity
- Instance synchronized and static synchronized use DIFFERENT locks (this vs ClassName.class) — proven not to exclude each other
- Reentrant locks: same thread can re-enter a synchronized block on the same lock without self-deadlocking (per-thread hold count)
- Deadlock = circular wait; fix = consistent global lock-acquisition order; ThreadMXBean.findDeadlockedThreads() can detect one programmatically
- wait() releases the monitor (unlike sleep()); ALWAYS guard wait() in a while loop not if — spurious wakeups are real and documented
