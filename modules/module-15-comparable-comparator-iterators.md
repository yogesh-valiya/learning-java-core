# Module 15 — Comparable vs Comparator, Sorting; Iterators, Fail-Fast vs Fail-Safe

> Phase 2 — Generics & Collections. Priority: **High**. Final module of Phase 2.

Companion demo: [`module-15-comparable-comparator-iterators/ComparatorIteratorDemo.java`](./module-15-comparable-comparator-iterators/ComparatorIteratorDemo.java)

---

## 1. Comparable vs Comparator

**`Comparable<T>`** — `compareTo(T o)` — a class's **one, natural ordering**, defined *inside* the class itself (`implements Comparable<T>`). This is Module 11's `<T extends Comparable<T>>` bound made concrete.

**`Comparator<T>`** — `compare(T o1, T o2)` — an **external, pluggable** ordering, defined *outside* the class. Any number of `Comparator`s can exist for one type, without touching the class. It's a functional interface (one abstract method), so it composes with lambdas and static helpers:

```java
people.sort(Comparator.comparing(Person::lastName).thenComparing(Person::firstName));
```

`Comparator.comparing(keyExtractor)`, `.thenComparing(...)`, `.reversed()`, `Comparator.naturalOrder()`/`.reverseOrder()`, `Comparator.nullsFirst`/`.nullsLast` — this chainable style is how sorting is actually written in modern Java.

**One-liner:** *"Comparable is the class's own, single natural order; Comparator is an external, swappable order you can define as many of as you need."*

## 2. The gotcha: sorted collections use `compareTo`/`compare` EXCLUSIVELY — not `equals()`/`hashCode()`

The sharpest interview trap in this module. A sorted collection (`TreeSet`/`TreeMap`) decides "is this a duplicate?" purely by whether `compareTo`/`compare` returns **0** — `equals()`/`hashCode()` are never consulted.

```java
Set<Person> byAgeOnly = new TreeSet<>(Comparator.comparingInt(Person::age));
byAgeOnly.add(new Person("A", "X", 30));
byAgeOnly.add(new Person("B", "Y", 30));   // a completely different person, same age
```

Confirmed in `ComparatorIteratorDemo.java`: `byAgeOnly.size()` is **1**, not 2 — two clearly-not-`.equals()` people (different names) collapse into "one" entry purely because their ages tie under this comparator. **Say it exactly like this:** *"A sorted collection's notion of equality is `compareTo() == 0`, full stop — if the comparator doesn't reflect the same distinctions `equals()` does, entries silently collapse."* `Comparable` implementations are expected (per Javadoc) to stay "consistent with equals" — nothing enforces it, but violating it produces exactly this bug.

## 3. Sort stability, briefly

`Collections.sort`/`List.sort` on objects uses a modified **TimSort** — O(n log n) and critically **stable** (elements comparing equal keep their original relative order). `Arrays.sort` on a **primitive** array uses a **dual-pivot quicksort** instead — not stable, but stability is meaningless for bare primitives anyway. Precise answer to "is Java's sort stable?": for objects, yes (TimSort); for primitives, the question doesn't really apply.

## 4. Iterators — the only safe way to remove mid-iteration

```java
Iterator<Integer> it = list.iterator();
while (it.hasNext()) {
    if (it.next() == 3) it.remove();   // safe
}
```

`Iterator.remove()` is the **only** safe way to remove an element while iterating — it updates the iterator's own bookkeeping as part of the removal. Modern code usually reaches for `list.removeIf(condition)` instead, which does the same thing underneath.

## 5. Fail-fast vs fail-safe

Most JDK collections (`ArrayList`, `HashMap`, `HashSet`, ...) are **fail-fast**: a `modCount` increments on every structural change. An iterator captures `expectedModCount` at creation; every `next()` re-checks it, throwing `ConcurrentModificationException` the moment they diverge — except when the change came through the iterator's own `remove()`, which updates both counters together. Documented explicitly as **best-effort**, not a guarantee — it exists to surface bugs fast, not to provide any real correctness/thread-safety guarantee.

The classic bug, confirmed live in the demo:

```java
for (Integer n : nums) {
    if (n == 3) nums.remove(n);   // modifies the list directly, bypassing the iterator
}
// -> ConcurrentModificationException on the next next() call
```

**Fail-safe** collections (`CopyOnWriteArrayList`, `ConcurrentHashMap`'s iterators) never throw this — they iterate over a snapshot or use weakly-consistent traversal, tolerating concurrent modification. Trade-off: the iteration might not reflect the very latest changes. (Full depth on these is Phase 5 — concurrency; the vocabulary lands now because it pairs directly with fail-fast.)

## PHP contrast

PHP's `usort`/`uasort` take a callback — functionally close to a `Comparator` — but PHP has no `Comparable`-style "natural ordering baked into the class" convention, and no fail-fast iteration concept (PHP arrays don't throw on modification during `foreach`; they have their own different surprises around array copying instead).

## Interview checklist

- Comparable = one natural order, in the class. Comparator = any number of external, swappable orders.
- `Comparator.comparing().thenComparing().reversed()` chaining is the modern idiom — know it cold.
- Sorted collections use `compareTo`/`compare` exclusively for equality — mismatched against `equals()`, entries silently collapse.
- Object sort (`TimSort`) is stable; primitive sort (dual-pivot quicksort) is not, but stability is moot for primitives.
- `Iterator.remove()` (or `removeIf`) is the only safe way to remove mid-iteration.
- Fail-fast (`modCount` + `ConcurrentModificationException`) is best-effort bug detection, not a guarantee.
- Fail-safe collections (`CopyOnWriteArrayList`, `ConcurrentHashMap`) trade CME-freedom for possibly-stale iteration.
