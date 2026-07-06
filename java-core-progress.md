# Java Core — Progress Tracker

**Goal:** Job-ready for Java backend roles (India), interview-optimized.
**Scope:** Java Core only — Spring Boot is a separate project.
**How to use:** Start each session in a new chat inside the project → paste this file → learn one module → apply the update lines the mentor gives you at the end.

**Marking:** check done with `[x]`; add 🔄 next to the one in progress. Priority: **[H]**igh / **[M]**edium / **[L]**ow.

---

## Roadmap

### Phase 0 — Orientation (fast-track) · ~2 sessions
- [x] 1. Java platform & how it runs — JDK/JRE/JVM, bytecode, JIT, compile→run flow · **[H]** · classic opener Q
- [x] 2. Syntax map for PHP devs + Maven basics — types, packages, `main`, arrays vs collections, Maven lifecycle/deps · **[L]** · fast-tracked cheat-sheet

### Phase 1 — Core language & OOP the Java way · ~6 sessions
- [ ] 3. Primitives, wrappers, autoboxing; `==` vs `.equals()` · **[H]** · constant gotcha source
- [ ] 4. Strings — immutability, string pool, StringBuilder/StringBuffer · **[H]** · very common
- [ ] 5. Classes & OOP mechanics — access modifiers, static, final, this/super, overload vs override rules · **[H]** · map from PHP, focus on Java rules
- [ ] 6. Interfaces vs abstract classes — default/static methods, "multiple inheritance of type" · **[H]** · favorite design Q
- [ ] 7. equals() & hashCode() contract + Object methods (toString, getClass) · **[H]** · extremely frequent
- [ ] 8. Building immutable classes — defensive copying, why it matters · **[M]** · common design task
- [ ] 9. Enums (real Java enums, not PHP-style) + EnumMap/EnumSet · **[M]**
- [ ] 10. Nested & anonymous classes · **[M]** · needed to read real code + understand pre-lambda style

### Phase 2 — Generics & Collections (interview core) · ~5 sessions
- [ ] 11. Generics — bounded types, wildcards (`? extends`/`? super`), PECS, type erasure · **[H]**
- [ ] 12. Collections overview — List/Set/Queue/Map hierarchy; ArrayList vs LinkedList · **[H]**
- [ ] 13. HashMap internals — buckets, hashing, treeify (Java 8), resize, load factor · **[H]** · the #1 internals Q
- [ ] 14. Set/Map variants — HashSet, LinkedHashMap, TreeMap; when to use which · **[H]**
- [ ] 15. Comparable vs Comparator, sorting; iterators, fail-fast vs fail-safe · **[H]**

### Phase 3 — Modern functional Java · ~4 sessions
- [ ] 16. Lambdas & functional interfaces (Function/Predicate/Consumer/Supplier) + method references · **[H]**
- [ ] 17. Streams API — map/filter/reduce, Collectors (groupingBy/joining/toMap), flatMap, parallel · **[H]**
- [ ] 18. Optional — right usage, avoiding null, common mistakes · **[M]**
- [ ] 19. Date/Time API (java.time) — LocalDate/Time, Duration/Period, formatting · **[M]**

### Phase 4 — Exceptions · ~2 sessions
- [ ] 20. Exception handling — checked vs unchecked, try-with-resources, custom exceptions, finally semantics, best practices · **[H]**

### Phase 5 — Concurrency (senior differentiator) · ~6 sessions
- [ ] 21. Threads — lifecycle, Runnable vs Callable, core methods · **[H]**
- [ ] 22. Synchronization & memory model — synchronized, volatile, race conditions, deadlock, wait/notify · **[H]** · big separator
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
- **Just finished:** Module 2 — Syntax map for PHP devs + Maven basics (static typing, int division, packages, `main`, GAV/lifecycle/`~/.m2`)
- **Next up:** Module 3 — Primitives, wrappers, autoboxing; `==` vs `.equals()`
- **Sessions done:** 2

## Struggle log
_Topics that didn't fully click — revisit / spaced repetition._
-

## Interview drill queue
_Questions & gotchas the mentor flagged that I want to re-practice._
-
