# Module 7 — equals() & hashCode() Contract + Object Methods

> Phase 1 — Core language. Priority: **High**. Extremely frequent. Foundation for HashMap internals (Phase 2).

Companion demos:
- [`module-07-equals-hashcode/HashContractDemo.java`](./module-07-equals-hashcode/HashContractDemo.java)
- [`module-07-equals-hashcode/EqualsImplDemo.java`](./module-07-equals-hashcode/EqualsImplDemo.java)

---

## 1. Every class extends `Object`

Even without writing `extends`, every class implicitly extends `java.lang.Object` and inherits:

| Method | Default behavior (from `Object`) |
|--------|----------------------------------|
| `equals(Object o)` | **Reference** equality — literally `this == o` |
| `hashCode()` | An int derived from the object's **identity** (~memory address) |
| `toString()` | `ClassName@hexHashCode` (e.g. `Point@1b6d3586`) |
| `getClass()` | The runtime `Class` object |

Default `equals()` is just `==`, so two different objects with identical contents are **not** equal. For value-like objects (`Point`, `Money`, `UserId`) you override `equals()` to compare fields.

---

## 2. The `equals()` contract

A correct `equals()` must be:

- **Reflexive:** `x.equals(x)` is true.
- **Symmetric:** `x.equals(y)` ⟺ `y.equals(x)`.
- **Transitive:** `x.equals(y)` and `y.equals(z)` ⟹ `x.equals(z)`.
- **Consistent:** repeated calls give the same result (if the object is unchanged).
- **Non-null:** `x.equals(null)` is always false.

---

## 3. The `hashCode()` contract & the golden rule

- If two objects are **equal** by `equals()`, they **must** return the **same** `hashCode()`.
- Unequal objects *may* share a hashCode (a "collision" — allowed, just less efficient).
- Consistent: same object → same hashCode across calls.

> **GOLDEN RULE: if you override `equals()`, you MUST override `hashCode()`.** Break it and you break every hash-based collection (`HashMap`, `HashSet`, …).

### Why it breaks (the mechanism)

A `HashMap`/`HashSet` finds an object in two steps:
1. Use `hashCode()` to pick a **bucket**.
2. Use `equals()` to find the item **within** that bucket.

If you override `equals()` but keep the default identity `hashCode()`, two "equal" objects produce **different** hashcodes → land in **different buckets** → the collection looks in the wrong bucket and **never even calls `equals()`**. Consequences:

- `map.get(equalKey)` returns `null` (lookup lost).
- `set.add(x); set.add(equalX);` → **both stored** → the Set contains duplicates (`size() == 2`).

It fails **silently** — no exception, just wrong behavior. Demonstrated in `HashContractDemo`: the `PointBad` set ends up with size 2; the `PointGood` set dedups to 1.

> **Interview line:** *"`hashCode()` decides the bucket, `equals()` decides within the bucket. Override `equals()` without `hashCode()` and equal objects scatter across buckets — the collection can't find or dedup them."*

---

## 4. The canonical implementation

### equals()

```java
@Override
public boolean equals(Object o) {           // param MUST be Object
    if (this == o) return true;              // 1. same reference → fast true
    if (o == null || getClass() != o.getClass()) return false;  // 2. null + type check
    Point p = (Point) o;                     // 3. safe cast
    return x == p.x && y == p.y;             // 4. compare significant fields
}
```

### hashCode()

```java
@Override
public int hashCode() {
    return Objects.hash(x, y);   // combine the SAME fields used in equals()
}
```

- `Objects.hash(...)` — idiomatic, null-safe (slight boxing overhead; hand-roll `31*result + field` only on hot paths).
- Use the **same fields** in `equals()` and `hashCode()`.
- `Objects.equals(a, b)` — **null-safe** field comparison inside `equals()` for object fields.

### The #1 silent gotcha: wrong parameter type

```java
public boolean equals(Point p) { ... }   // OVERLOADS, does NOT override!
```

`Object.equals` takes an **`Object`** parameter. Typing your own class (`Point`) creates a *different* method — an overload — and collections still call the default `Object.equals`. Your method silently never runs. **`@Override` prevents this** — the compiler rejects a method that doesn't actually override. Always annotate.

---

## 5. `getClass()` vs `instanceof` — the symmetry debate

Step 2 of `equals()` can be written two ways:

| Approach | Behavior | Trade-off |
|----------|----------|-----------|
| `getClass() != o.getClass()` | Strict — only the **exact same class** can be equal | Preserves symmetry & transitivity; but a **subclass instance can never equal a superclass instance** (violates Liskov) |
| `o instanceof Point` | Lenient — subclasses can be equal to the base | Can **break symmetry** if a subclass adds fields to its own `equals()` |

**The `instanceof` symmetry break** (shown in `EqualsImplDemo`): base `P` uses `instanceof P` and checks only `x,y`; subclass `ColorP` adds `color`. Then:

- `p.equals(cp)` → `true` (cp is a P; base checks only x,y).
- `cp.equals(p)` → `false` (p is not a ColorP).
- → asymmetric → contract violated.

**Effective Java guidance:** for value classes, use `getClass()`, or make the class `final`, or favor composition over inheritance. Practical rule: **value types should be `final`.**

---

## 6. `toString()` and `Objects` helpers

- Default `toString()` is the useless `ClassName@hex`. Override it for readable logs:
  ```java
  @Override public String toString() { return "Point(" + x + ", " + y + ")"; }
  ```
- `Objects.equals(a, b)` — null-safe equality.
- `Objects.hash(...)` — combine fields for hashCode.

---

## 7. The modern shortcut: records (Java 16+)

For a pure value/data class, a **record** auto-generates `equals()`, `hashCode()`, `toString()`, accessors, and the canonical constructor — all contract-correct:

```java
record Point(int x, int y) {}
// free: equals, hashCode, toString, x(), y(), constructor
```

- Record `toString()` format: `Point[x=1, y=2]` (square brackets, all components).
- This is the idiomatic modern way to write value/DTO classes (deep-dive in Module 28).
- Pre-16 equivalents: Lombok `@EqualsAndHashCode` / IDE-generated methods.

---

## 8. The mutable-key trap

If you use an object as a `HashMap` key and then **mutate a field used in its `hashCode()`**, the entry becomes **unreachable**:

```java
map.put(key, "hello");   // stored in bucket for hashCode() at insert time
key.someField = ...;     // hashCode changes
map.get(key);            // returns NULL — new hashCode points to a different bucket
```

The entry is **stranded** in the old bucket: unretrievable even with the same reference, and it still occupies space (a subtle leak).

> **Rule: hash keys must be immutable.** This is why `String` and the wrapper types (all immutable) are ideal keys, and why value classes should be `final` with `final` fields. Ties directly to Module 4 (immutability).

---

## 9. PHP contrast

- PHP object comparison: `==` compares class + property values; `===` compares identity. PHP has **no `equals()`/`hashCode()` contract** because PHP arrays/maps use string/int keys, not object hashing.
- PHP `__toString()` ≈ Java `toString()`.
- So this whole contract is Java-specific and important — there's no PHP muscle memory to lean on.

---

## 10. Interview checklist

- **Why override `hashCode()` with `equals()`?** bucket-then-equals mechanism; equal objects must share a bucket.
- **`equals(Point)` vs `equals(Object)`** — the overload trap; `@Override` catches it.
- **The 5 `equals` contract properties** (reflexive/symmetric/transitive/consistent/non-null).
- **`getClass()` vs `instanceof`** — symmetry vs Liskov; prefer `getClass()` / `final` value types.
- **Mutable-key trap** — hash keys must be immutable.
- **Records** auto-generate all of this (Java 16+).
- **Default `toString()`** is unhelpful; override for logging.
