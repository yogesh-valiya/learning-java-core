# Module 12 — Collections Overview

> Phase 2 — Generics & Collections. Priority: **High**. This module is the map of the framework; Modules 13–15 are the deep dives into specific territory on it (HashMap internals, Set/Map variants, Comparable/Comparator/iterators).

Companion demo: [`module-12-collections-overview/ArrayListVsLinkedListDemo.java`](./module-12-collections-overview/ArrayListVsLinkedListDemo.java)

**PHP bridge, upfront:** PHP has **one** array type doing double duty as list + map + set-ish (ordered hashtable under the hood). Java **splits this into distinct interfaces with real contracts** — a `List` can never silently become a `Set`. The underlying concepts (list/set/map/queue) aren't new; the split into a real type hierarchy with distinct performance contracts is.

---

## 1. The framework hierarchy

```
Iterable<E>                       — anything usable in a for-each loop
    └── Collection<E>             — + add/remove/size/contains/stream...
            ├── List<E>           — ordered, indexed, duplicates OK
            ├── Set<E>            — no duplicates
            └── Queue<E>          — processing order (FIFO-ish)
                    └── Deque<E>  — double-ended (stack + queue)

Map<K,V>                          — separate hierarchy entirely
```

**`Map<K,V>` deliberately does NOT extend `Collection`.** A `Collection` holds single elements; a `Map` holds key→value pairs — a different shape, not a missing feature. (*"Is `Map` a `Collection`?"* → **No.**) Map plugs back into the Collection world for iteration via three views: `map.keySet()` → `Set<K>`, `map.values()` → `Collection<V>`, `map.entrySet()` → `Set<Map.Entry<K,V>>`.

`Set`'s "no duplicates" isn't magic — it's built directly on the **`equals()`/`hashCode()` contract** (Module 7): a `Set` decides "is this a duplicate?" via `equals()`, and uses `hashCode()` to find the bucket to check in the first place. Get that contract wrong on your own class, and a `HashSet` of your objects silently accepts "duplicates."

## 2. The implementations map

| Interface | Common implementations | Ordering | Duplicates | Nulls |
|---|---|---|---|---|
| `List` | `ArrayList`, `LinkedList`, `Vector` (legacy, synchronized) | insertion order, indexed | ✅ yes | ✅ yes, any number |
| `Set` | `HashSet`, `LinkedHashSet`, `TreeSet` | none / insertion / sorted | ❌ no | `HashSet`: 1 null; `TreeSet`: none (NPE on compare) |
| `Queue`/`Deque` | `ArrayDeque`, `LinkedList`, `PriorityQueue` | FIFO / priority / double-ended | ✅ yes | `ArrayDeque`: no nulls |
| `Map` | `HashMap`, `LinkedHashMap`, `TreeMap` | none / insertion / sorted-by-key | keys ❌, values ✅ | `HashMap`: 1 null key; `TreeMap`: none |

`LinkedList` appears under both `List` and `Deque` — it's the one JDK class implementing both simultaneously, a genuine list *and* a legitimate stack/queue. The specific internals behind each row (HashMap buckets, TreeMap ordering, HashSet mechanics) are Modules 13–14; this module just places them correctly on the map.

---

## 3. ArrayList — array-backed

Backed by a resizable `Object[]`. Direct index math for access:

- **`get(index)` = O(1)** — direct array offset.
- **`add(item)` at the end = amortized O(1)** — when the backing array is full, it resizes: allocates a new array at **1.5× capacity** (`oldCapacity + (oldCapacity >> 1)` in OpenJDK), copies everything via `Arrays.copyOf`. That single resize is O(n), but amortized across all the adds between resizes, it averages to O(1). Default capacity 10, lazily allocated on first add in modern JDKs.
- **`add(index, item)` / `remove(index)` in the middle = O(n)** — `System.arraycopy` shifts every element after the gap.
- **Cache-friendly:** contiguous memory — the CPU prefetcher works well with sequential array access, which matters more in practice than the Big-O tables alone suggest.

## 4. LinkedList — node-backed, dual-natured

Doubly-linked `Node { prev, item, next }`.

- **`addFirst`/`addLast`/`removeFirst`/`removeLast` = O(1)** — relink pointers at an end.
- **`get(index)` = O(n)** — walks from whichever end is nearer (JDK picks the closer one; average distance is still n/4).
- **`add(index, item)` by index = still O(n) overall** — traversal to reach the index dominates, even though the insert once there is O(1) relinking.
- **The one place LinkedList's O(1) middle-insert claim is actually true:** already holding a positioned `ListIterator` (mid-iteration) and calling `iterator.add(x)` — no traversal needed, you're already there. A narrow case, not "inserting at index 500,000 is fast."
- **Heavier per-element memory:** each node = object header + `prev` ref + `next` ref + item ref, vs. ArrayList's single tight slot.
- **Poor cache locality:** nodes scattered across the heap — every `next` hop risks a cache miss. This is *why* the measured gap below is so large, not just the O(n) vs O(1) theory.

## 5. The `RandomAccess` marker and the `get(i)`-loop anti-pattern

`ArrayList` implements the marker interface `RandomAccess`; `LinkedList` does not. Library code (e.g. `Collections.binarySearch`) checks `instanceof RandomAccess` at runtime to pick an index-loop strategy vs. an iterator-based one.

Measured in `ArrayListVsLinkedListDemo.java`, n = 40,000, after JIT warm-up:

```
1) instanceof RandomAccess - ArrayList: true
2) instanceof RandomAccess - LinkedList: false
3) ArrayList  get(i) loop:        2.62 ms
4) LinkedList get(i) loop:      860.14 ms
5) LinkedList iterator loop:      2.71 ms
6) LinkedList get(i)-loop is ~318x slower than its own iterator loop
```

`for (int i = 0; i < list.size(); i++) list.get(i)` over a `LinkedList` is **O(n²)** — each `get(i)` independently re-walks up to n/4 nodes from an end, n times over. Swap to a `for-each`/iterator and it drops to O(n) — **318× faster on identical data**, because the iterator follows `next` once, in order, never re-walking anything. Same list, same elements — the only variable is *how the data was asked for*.

## 6. ArrayList vs LinkedList — summary

| | ArrayList | LinkedList |
|---|---|---|
| Backing | resizable `Object[]` | doubly-linked `Node<E>` |
| `get(index)` | O(1) | O(n) |
| add/remove at an end | amortized O(1) | O(1) |
| add/remove at arbitrary index | O(n) (shift) | O(n) (traversal dominates) |
| add/remove via **positioned** iterator | O(n) (shift still applies) | **O(1)** (the real win case) |
| Memory/element | low | high (node + 2 refs + header) |
| Cache locality | good | poor |
| Implements | `List`, `RandomAccess` | `List`, `Deque` |

**Modern practical answer:** prefer `ArrayList` almost always — even for insert/delete-heavy workloads — because cache locality wins on real hardware, and `LinkedList`'s true O(1) insert needs an already-positioned iterator, a narrow case. For genuine `Deque`/stack/queue needs, prefer **`ArrayDeque`** (circular array, no node overhead) over `LinkedList` in modern Java — `LinkedList` is rarely the right default for anything anymore; it mostly survives for API compatibility.

## 7. Gotcha: casting a `Queue` reference to `List`

```java
Queue<Job> jobs = new LinkedList<>();
for (int i = 0; i < jobs.size(); i++) {
    process(((List<Job>) jobs).get(i));   // works, but for the wrong reasons
}
```

This compiles and runs **only because `LinkedList` happens to implement both `Queue` and `List`.** It's a landmine: swap the implementation to the modern recommendation, `new ArrayDeque<>()`, and this line throws `ClassCastException` at runtime — `ArrayDeque` does not implement `List`. A variable declared `Queue` should be used through `Queue`'s own methods (`poll()`/`peek()`) or a plain iterator/for-each — never index access via a cast. The cast itself is the code smell, independent of the O(n²) performance bug it also happens to cause.

---

## 8. PHP contrast

- PHP's single array type blurs list/set/map/queue into one structure with runtime-flexible behavior; Java's split means the **declared interface is a contract** — a `List` guarantees index access is meaningful, a `Queue` doesn't.
- Neither PHP array internals nor userland code expose an equivalent to `RandomAccess` — there's no standard way to ask "will indexing into this be fast?" because PHP arrays are always effectively O(1)-ish hashtable/array hybrids under the hood.

## 9. Interview checklist

- **Map is not a Collection** — different shape (pairs vs. single elements) — but exposes `keySet()`/`values()`/`entrySet()` back into the Collection world.
- **Set's uniqueness is enforced by `equals()`/`hashCode()`**, not by the interface itself.
- **`LinkedList` implements both `List` and `Deque`** — it's a genuine dual-purpose structure, not just a `List`.
- **ArrayList:** array-backed, O(1) `get`, amortized O(1) append (1.5× growth), O(n) middle insert/remove, cache-friendly.
- **LinkedList:** node-backed, O(1) at the ends, O(n) random access, true O(1) middle-insert *only* via an already-positioned iterator, poor cache locality, heavier per-element memory.
- **`RandomAccess`** — marker interface `ArrayList` implements and `LinkedList` doesn't; lets library algorithms branch to the efficient strategy for each.
- **The `get(i)`-loop anti-pattern on `LinkedList`** turns an O(n) traversal into O(n²) — always prefer iterator/for-each when the concrete type isn't guaranteed `RandomAccess`.
- **Modern default: prefer `ArrayList`**; prefer `ArrayDeque` over `LinkedList` for actual queue/stack/deque use cases.
- **Casting a `Queue` to `List` to get index access is a smell** — it only works if the concrete class happens to implement both, and breaks the moment that implementation changes.
