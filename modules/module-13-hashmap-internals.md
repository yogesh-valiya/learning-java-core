# Module 13 — HashMap Internals

> Phase 2 — Generics & Collections. Priority: **High**. The single most-tested internals question in Java interviews. Builds on Module 7 (`equals()`/`hashCode()`) and Module 12 (`Set` uniqueness).

Companion demos:
- [`module-13-hashmap-internals/HashBucketDemo.java`](./module-13-hashmap-internals/HashBucketDemo.java) — bucket indexing and hash spreading
- [`module-13-hashmap-internals/TreeifyDemo.java`](./module-13-hashmap-internals/TreeifyDemo.java) / [`TreeifyScalingDemo.java`](./module-13-hashmap-internals/TreeifyScalingDemo.java) — collision handling and treeification limits
- [`module-13-hashmap-internals/ResizeDemo.java`](./module-13-hashmap-internals/ResizeDemo.java) — resize cost and pre-sizing

---

## 1. Structure & bucket indexing

A `HashMap` is an array of buckets: `Node<K,V>[] table`. `put`/`get` boil down to two steps: (1) compute the bucket from the key's hash, (2) scan within that bucket using `equals()`.

```java
static int bucketIndex(int hash, int capacity) {
    return (capacity - 1) & hash;   // bitmask, NOT modulo
}
```

Capacity is always a power of two **specifically** so `& (capacity-1)` can substitute for `% capacity` — a mask only behaves like modulo when it's a contiguous run of 1-bits, which only happens for `2^k - 1`. For a non-power-of-two capacity (e.g. 12 → mask `0b1011`), some bit positions in the mask are 0, so entire indices become mathematically unreachable no matter how well-distributed the hashes are.

Before indexing, HashMap applies its own `hash()`: `h ^ (h >>> 16)`. Bucket indexing only ever looks at the **low** bits of the hash; many `hashCode()` implementations vary mostly in their **high** bits. Spreading XORs the high bits down so high-bit differences actually influence which bucket a key lands in. It can't manufacture separation for two hash codes that are already close together in the low bits — that's an inherent property of masking, not something spreading fixes.

## 2. Collision handling & treeification

Pre-Java-8: each bucket is a linked list — collisions just append, worst case O(n) per lookup.

Java 8+: a bucket **treeifies** into a red-black tree once it hits **`TREEIFY_THRESHOLD` = 8** entries, provided the table is at least **`MIN_TREEIFY_CAPACITY` = 64** buckets — below that, the JDK resizes instead, betting more buckets fixes the skew without tree overhead. It un-treeifies back to a list if a later resize shrinks the bucket to ≤6.

**Why 8:** the JDK's own source comments justify it with a Poisson-distribution argument — under a *reasonably good* hash function, a bucket organically reaching 8 entries is astronomically unlikely. Treeification exists as a defense against a **bad or malicious** `hashCode()` (hash-flooding is a real DoS vector), not something that should trigger in normal operation.

**The nuance that actually matters:** a red-black tree needs a real ordering to search efficiently. Within a treeified bucket, nodes are ordered by comparing `hash` values first (a real signal, when hashes differ), then by `Comparable` if the key implements it, and **only if neither applies**, a fallback tiebreak (class name, then identity hash) exists purely to keep the tree structurally valid — that fallback carries no relationship to the actual keys, so it can't be used to prune a search.

**Consequence:** a `hashCode()` that returns the same constant for every key still gets treeified, but search degrades far past O(log n) — confirmed empirically in `TreeifyScalingDemo`: per-lookup cost roughly doubles-to-sextuples every time `n` doubles, instead of staying flat the way real O(log n) would. **Treeification protects against ordinary bucket collisions (different hash values, same bucket via mask truncation) — it is not a full defense against a hashCode that's constant for every input.**

One contract distinction worth keeping precise:
- A **constant** `hashCode()` (always returns e.g. `42`) does **not** violate the `equals()`/`hashCode()` contract — it's consistent, and equal keys trivially share a hash. That's a **performance** bug.
- An **inconsistent** `hashCode()` (different value on repeated calls for the same unchanged key) **does** violate the contract — `put` and a later `get` compute different buckets for what should be the same key, and the entry becomes silently unreachable. That's a **correctness** bug.

These are two different failure modes. The actual defense against the pathological constant-hash case is unglamorous: write a real `hashCode()` — combine the relevant fields with a multiplier, or use `Objects.hash(...)` — so that even when buckets collide, the underlying hash *values* still differ enough for the tree to have something to order by.

## 3. Resizing & load factor

Three governing numbers: default capacity **16**, default load factor **0.75**, resize threshold = `capacity × loadFactor`. Cross it, and the next `put` doubles capacity and redistributes every entry.

**Why 0.75:** a space/time tradeoff — too high and buckets grow deep before resizing (time cost); too low and the map resizes (and wastes memory) too eagerly (space cost). Same Poisson reasoning as the treeify threshold underlies the JDK's choice of 0.75 as the balance point.

**The Java 8 resize optimization:** since new capacity is always exactly **2× old capacity**, the bucket mask gains exactly one new bit. Every entry's new bucket is either its old index, or `oldIndex + oldCapacity` — decided purely by that one new bit of its already-computed hash. Resize splits each old bucket into a "lo" list (new bit = 0, stays) and a "hi" list (new bit = 1, moves), and relinks — no hash recomputation, no full re-bucketing.

**`tableSizeFor`:** a constructor's initial-capacity argument is always rounded **up** to the next power of two — `new HashMap<>(50)` allocates capacity 64, not 50.

**Practical takeaway:** pre-size (`new HashMap<>((int)(expectedSize / 0.75f) + 1)`) when the entry count is known upfront — it skips the resize-copy cascade entirely during bulk inserts. Measured consistently faster in `ResizeDemo` (isolated-process trials — an in-process back-to-back timing loop gave noisy, unreliable, even backwards results here, since JIT/GC state bled between the two configurations measured in sequence; measuring each configuration as an independent JVM process removed that contamination). The mechanism is what matters — not a specific multiplier.

## PHP contrast

PHP's array/hashtable hybrid exposes none of this to userland — no capacity, no load factor, no visible resize policy. Java's `HashMap` makes every one of these tradeoffs an explicit, tunable constructor parameter.

## Interview checklist

- Bucket index = bitmask (`(capacity-1) & hash`), not modulo — requires power-of-two capacity.
- `spread()` (`h ^ (h>>>16)`) defends against `hashCode()`s that vary mainly in high bits; can't fix already-close low bits.
- Treeify at 8 entries + capacity ≥ 64 — a defense against a bad/malicious hash, not normal-case behavior; un-treeifies at ≤6 during a resize.
- Treeification needs a real ordering signal (differing hashes or `Comparable`) — a constant hashCode for every key still degrades badly despite being "treeified."
- Constant-but-consistent bad `hashCode()` = performance bug only; inconsistent `hashCode()` = correctness bug (entries become silently unreachable).
- Load factor 0.75 = deliberate space/time tradeoff.
- Resize doubles capacity and splits each bucket via a one-bit check (lo/hi) — no full rehash.
- `new HashMap<>(n)` rounds **up** to the next power of two.
- Pre-size when the count is known, to skip the resize-copy cascade.
