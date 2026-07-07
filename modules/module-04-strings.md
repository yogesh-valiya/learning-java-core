# Module 4 — Strings: Immutability, String Pool, StringBuilder/StringBuffer

> Phase 1 — Core language. Priority: **High**. Very commonly asked. Builds directly on the string pool from Module 3.

Companion demos:
- [`module-04-strings/ImmutabilityDemo.java`](./module-04-strings/ImmutabilityDemo.java)
- [`module-04-strings/BuilderDemo.java`](./module-04-strings/BuilderDemo.java)

---

## 1. String immutability

A Java `String` is **immutable**: once created, its contents can **never** change. Every method that appears to "modify" a string actually returns a **brand-new** `String` and leaves the original untouched.

```java
String s = "hello";
s.toUpperCase();          // returns "HELLO" — but the result is ignored!
System.out.println(s);    // still "hello"   ← original unchanged

s = s.toUpperCase();      // reassign the reference to the new object
System.out.println(s);    // now "HELLO"
```

The variable `s` can be re-pointed at a different object, but the object `"hello"` itself is frozen forever. This applies to `toUpperCase`, `concat`, `replace`, `substring`, `+`, and every other "modifying" method — each returns a new String.

### Contrast with PHP

PHP strings are **mutable** — you can edit a character in place:

```php
$s = "hello";
$s[0] = 'H';        // legal in PHP → "Hello"
```

Java has **no such operation**. `s.charAt(0) = 'H'` does not even compile. There is no way to mutate a String in place, full stop. This trips up developers coming from PHP, C, or Python.

---

## 2. Under the hood: why immutable?

Internally, `String` wraps a **`private final byte[]`** (it was `char[]` before Java 9; since Java 9, "compact strings" use `byte[]` to save memory for Latin-1 text). Two things enforce immutability:

- The array reference is `final` (can't be repointed) and `private` (nothing outside can touch it).
- `String` never exposes a method that writes into that array.

**Why did the designers make String immutable? (Four reasons — a common "why" question.)**

1. **The string pool is only safe because of immutability.** Pooled literals are shared across many variables. That sharing is safe *only* if nobody can mutate the shared instance — otherwise changing one variable's string would silently corrupt every other variable pointing at the same pooled object. **Immutability is the precondition that makes pooling possible.**
2. **Thread-safety for free.** Immutable objects can be shared across threads with zero synchronization, since nobody can change them. Valuable in a long-running JVM.
3. **Cached hashCode.** `String` computes its hashCode once and caches it (the value can never change). This makes `String` an extremely fast `HashMap` key — and strings are the most common map key in real code.
4. **Security.** Strings are used for filenames, DB URLs, class names, network hosts. If a string could mutate after a security check passed, you'd have a check-then-change exploit window. Immutability closes it.

---

## 3. The string pool and `intern()`

(Recap and extension of Module 3.)

- String **literals** (`"hi"`) are **interned** and shared → `"hi" == "hi"` is `true`.
- `new String("hi")` forces a fresh heap object, bypassing the pool → `new String("hi") == "hi"` is `false`.
- `.intern()` returns the **pooled** version of a string → `new String("hi").intern() == "hi"` is `true`.

### A JDK micro-optimization worth knowing

`String.concat` has a shortcut in its source:

```java
public String concat(String str) {
    if (str.isEmpty()) {
        return this;      // concatenating "" returns the SAME object
    }
    // ... otherwise build a new String
}
```

So `x.concat("")` returns the *same* object `x` (`x == x.concat("")` is `true`), while `x.concat("!")` builds a new object. Lesson beyond the trivia: **don't assume what a library method does — read the source.** The JDK is full of micro-optimizations like this.

---

## 4. The performance trap that motivates StringBuilder

Because every "modification" creates a new object, building a string with `+` in a loop is a serious performance trap:

```java
String result = "";
for (int i = 0; i < 10000; i++) {
    result += i;      // each += creates a NEW String and copies all prior chars
}
```

This creates ~10,000 throwaway `String` objects and re-copies the growing character array each time → **O(n²)** work and a flood of garbage.

---

## 5. StringBuilder vs StringBuffer

Both are **mutable** strings — a resizable character buffer you can append to **in place**, with **no new object per modification**. This solves the O(n²) loop trap.

```java
StringBuilder sb = new StringBuilder();
for (int i = 0; i < 10000; i++) {
    sb.append(i);            // mutates the SAME object — no garbage per iteration
}
String result = sb.toString();   // convert back to an immutable String at the end
```

`append()` returns `this`, so calls **chain**:

```java
String msg = new StringBuilder()
        .append("user=").append(userId)
        .append(", role=").append(role)
        .toString();
```

### The difference (a classic interview question)

`StringBuilder` and `StringBuffer` have the **identical API**. The only difference:

| | `StringBuilder` | `StringBuffer` |
|--|-----------------|----------------|
| Thread-safe? | No | Yes — every method is `synchronized` |
| Speed | Faster | Slower (lock overhead on every call) |
| Since | Java 5 | Java 1.0 (older) |
| When to use | **Default choice** (~99% of the time) | Only if multiple threads mutate the **same shared** builder instance (rare) |

**One-liner for interviews:** *"`StringBuffer` is synchronized/thread-safe but slower; `StringBuilder` is not synchronized but faster. Use `StringBuilder` by default — a builder is usually a local variable inside one method, so it's never shared across threads."* `StringBuffer` is effectively **legacy**; real thread-safety needs are almost always solved with proper concurrency tools (Phase 5), not by sharing a `StringBuffer`.

---

## 6. Under the hood: how StringBuilder is fast

- A `StringBuilder` wraps a **resizable `byte[]`** (like `String`, but **not** `final` — that's the point) plus a `length`. Default initial **capacity is 16**.
- When an `append` exceeds capacity, it **grows the array** (roughly `newCapacity = oldCapacity * 2 + 2`) and copies the old contents over.
- Because capacity **doubles**, appends are **amortized O(1)**, so building an n-char string is **O(n)** total — versus the immutable-`+` loop's **O(n²)**.
- Optimization: if you know the rough final size, pre-size it — `new StringBuilder(1000)` — to skip intermediate resizes/copies.

### Measured difference

Building a 100,000-character string:

```
String +=      : ~4300 ms      (O(n²))
StringBuilder  :    ~3 ms      (O(n))     → ~1400× faster
```

The `+=` version does ~100,000 array copies of ever-growing size; `StringBuilder` uses one buffer that doubles ~14 times. This is why "spot the `+=` in a loop" is a favorite interview snippet — it's a real order-of-magnitude bug behind innocent-looking code.

---

## 7. The `+` vs StringBuilder nuance

"Java compiles `a + b` into a `StringBuilder` anyway, so `+` is fine, right?" — Half true, and the other half is the trap.

- For a **single expression** — `"x=" + a + "y=" + b` — the compiler *does* optimize it into one efficient concat (a single `StringBuilder`, or in Java 9+ an `invokedynamic` string-concat). This is fine.
- But `+=` **inside a loop** is a *different expression each iteration*, so the compiler builds a **brand-new `StringBuilder` every pass**, appends, and calls `toString()` — right back to O(n²).

```java
// ❌ TRAP — a fresh StringBuilder created and discarded every iteration
String r = "";
for (...) r += x;

// ✅ FIX — one StringBuilder for the whole loop
StringBuilder sb = new StringBuilder();
for (...) sb.append(x);
String r = sb.toString();
```

**Rule:** `+` is fine for a fixed, small number of pieces on one line. The moment concatenation happens **in a loop**, use an explicit `StringBuilder`.

---

## 8. Interview checklist

- **Is String mutable?** — No; every "change" returns a new object.
- **Why is String immutable?** — pool safety, thread-safety, cached hashCode, security.
- **What backs a String?** — a `private final byte[]` (char[] pre-Java 9).
- **`==` vs `.equals()` on strings / `new String()` / `intern()`** — see Module 3 + §3 here.
- **StringBuilder vs StringBuffer** — synchronization is the only difference; StringBuilder is the default.
- **`+=` in a loop** — O(n²) bug; fix with StringBuilder. Know why (immutability + new object per concat).
- **StringBuilder internals** — resizable array, default capacity 16, doubling → amortized O(1) append.
