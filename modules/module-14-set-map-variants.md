# Module 14 — Set/Map Variants

> Phase 2 — Generics & Collections. Priority: **High**. Builds directly on Module 12's implementations map and Module 13's HashMap internals — every variant here is either literally backed by a `HashMap`, or a real tree.

Companion demo: [`module-14-set-map-variants/SetMapVariantsDemo.java`](./module-14-set-map-variants/SetMapVariantsDemo.java)

---

## 1. HashSet — a HashMap wearing a trenchcoat

`HashSet` has no independent storage. It wraps a `HashMap<E, Object>` internally, and every element is a **key**, mapped to a shared dummy constant (`PRESENT`):

```java
public boolean add(E e) {
    return map.put(e, PRESENT) == null;   // roughly the real implementation
}
```

Everything from Module 13 applies directly: O(1) average add/remove/contains, no ordering guarantee, uniqueness enforced by `equals()`/`hashCode()`.

## 2. LinkedHashSet / LinkedHashMap — a HashMap plus a thread of order

`LinkedHashMap` uses the **same nodes** as `HashMap` (buckets, hashing, everything from Module 13 unchanged) but each node gets two extra pointers, threading a **doubly-linked list** through them in a separate order — insertion order by default. Iteration follows that linked list, not bucket order, so it's predictable without sacrificing hash performance. `LinkedHashSet` wraps a `LinkedHashMap` the same way `HashSet` wraps a `HashMap`.

**The sharper feature:** pass `accessOrder=true` to the constructor, and the linked list re-orders on every `get`/`put` to reflect **access** order instead — every touch moves an entry to the "most recently used" end. Combined with overriding `removeEldestEntry`, this gives an LRU cache in a handful of lines:

```java
Map<Integer, String> lru = new LinkedHashMap<>(16, 0.75f, true) {   // true = access order
    @Override
    protected boolean removeEldestEntry(Map.Entry<Integer, String> eldest) {
        return size() > 3;   // evict once capacity is exceeded
    }
};
```

## 3. TreeSet / TreeMap — a real red-black tree

Backed by an actual `TreeMap` (red-black tree), **always** — not conditionally treeified like a HashMap bucket (Module 13). That means it **requires** a real ordering up front: keys/elements must implement `Comparable`, or a `Comparator` is supplied at construction. This is Module 11's `<T extends Comparable<T>>` pattern showing up in a concrete JDK class. No ordering signal available → `ClassCastException` at insertion time, not silently degraded performance.

Guaranteed **O(log n)** for add/remove/contains — genuinely guaranteed, because a real ordering signal always exists here (that's the price of admission), unlike HashMap's optional, best-effort treeification. Iteration is always in sorted order. `TreeMap`/`TreeSet` also expose **navigation** methods hash-based collections don't have: `firstKey()`/`lastKey()`, `higherKey()`/`lowerKey()`, `ceilingKey()`/`floorKey()`, `headMap()`/`tailMap()`/`subMap()` (range views).

## 4. Ordering, demonstrated

`SetMapVariantsDemo.java` puts the same four strings (with one duplicate) into each Set type:

```
1) HashSet       (unspecified order): [bravo, alpha, delta, charlie]
2) LinkedHashSet (insertion order):   [delta, alpha, charlie, bravo]
3) TreeSet       (sorted order):      [alpha, bravo, charlie, delta]
4) LRU cache keys after touching 1, then adding 4: [3, 1, 4]
```

Row 1 is neither alphabetical nor insertion order — it's whatever the hash/bucket layout produces (Module 13's bucket math), which is exactly why `HashSet` gives **no** ordering guarantee. Row 4 confirms the LRU mechanism: inserted `1,2,3` → touched `1` (now most-recently-used) → inserted `4` pushes size past capacity → `removeEldestEntry` evicts the least-recently-used, which is `2` (not `1` — touching it moved it to the back) → left with `[3, 1, 4]`.

## 5. Null handling

`HashSet`/`HashMap` permit **one** null (key). `LinkedHashSet`/`LinkedHashMap` — same, still hash-based. `TreeSet`/`TreeMap` permit **no** null key — inserting one throws `NullPointerException`, because the tree must call `compareTo` on it to place it, and there's no defined comparison against `null`.

## 6. When to use which

| Need | Use |
|---|---|
| Fastest lookup, don't care about order | `HashSet` / `HashMap` |
| Fast lookup, but iterate in insertion order | `LinkedHashSet` / `LinkedHashMap` |
| Sorted order, or range queries (`headMap`, `higherKey`, etc.) | `TreeSet` / `TreeMap` |
| LRU cache | `LinkedHashMap` with `accessOrder=true` + `removeEldestEntry` |

## PHP contrast

PHP arrays preserve insertion order **always** (they're ordered hashtables by default) — there's no PHP equivalent of "unordered by default, opt into insertion order, opt into sorted order" as three distinct, deliberately different types. Java forces a choice of ordering guarantee, and you pay only for the one you pick.

## Interview checklist

- `HashSet` is a thin wrapper over `HashMap<E, Object>` — same internals, same guarantees, same gotchas from Module 13.
- `LinkedHashMap`/`LinkedHashSet` add a doubly-linked list through the *same* hash nodes — insertion order by default, access order optionally.
- `accessOrder=true` + `removeEldestEntry` override = LRU cache in ~5 lines.
- `TreeSet`/`TreeMap` are a real, always-on red-black tree — genuinely guaranteed O(log n), because `Comparable`/`Comparator` is mandatory, unlike HashMap's best-effort treeification.
- `TreeMap`/`TreeSet` reject `null` (NPE on `compareTo`); `HashMap`/`HashSet` allow one `null` key.
- `TreeMap`/`TreeSet` expose range/navigation methods (`headMap`, `higherKey`, etc.) hash-based collections don't have.
