# Module 3 — Primitives, Wrappers, Autoboxing; `==` vs `.equals()`

> Phase 1 — Core language. Priority: **High**. A constant source of gotchas and one of the most frequently asked topics in Java interviews.

Companion demo: [`module-03-primitives-wrappers/EqualsDemo.java`](./module-03-primitives-wrappers/EqualsDemo.java)

---

## 1. Two fundamentally different kinds of values

Java has a hard split that PHP does not:

- **Primitives** hold the actual value directly.
- **Objects / references** — the variable holds a **reference** (a handle/pointer) to an object living on the heap, *not* the value itself.

```java
int x = 42;              // x literally contains 42
String s = "hello";      // s contains a REFERENCE to a String object on the heap
```

Hold onto this: **primitive = holds the value; object variable = holds a reference to the value.** It is the foundation of the entire `==` vs `.equals()` story.

### The 8 primitive types

| Type | Size | Example | Notes |
|------|------|---------|-------|
| `byte` | 8-bit | `127` | |
| `short` | 16-bit | | rarely used |
| `int` | 32-bit | `42` | **default whole number** |
| `long` | 64-bit | `42L` | note the `L` suffix |
| `float` | 32-bit | `3.14f` | note the `f` suffix |
| `double` | 64-bit | `3.14` | **default decimal** |
| `char` | 16-bit | `'A'` | single quotes, one character |
| `boolean` | — | `true` | |

Everything else — `String`, `ArrayList`, your own classes — is an **object/reference type**.

---

## 2. Wrapper classes

Every primitive has a matching **wrapper class** — an object that boxes the same value:

| Primitive | Wrapper |
|-----------|---------|
| `int` | `Integer` |
| `long` | `Long` |
| `double` | `Double` |
| `boolean` | `Boolean` |
| `char` | `Character` |
| … | (one per primitive) |

```java
int prim = 42;         // primitive
Integer obj = 42;      // wrapper object holding 42
```

### Why wrappers exist

1. **Collections store only objects, never primitives.** `List<int>` is a **compile error**; you must write `List<Integer>`. Any number that goes into a collection must become a wrapper object. This is the #1 day-to-day reason wrappers matter.
2. **They can be `null`.** A primitive `int` can never be null; an `Integer` can.
   - `Integer count = null;` is **legal** (a reference can be null).
   - `int count = null;` is a **compile error** (a primitive is a raw value slot; there is no "absence" it can hold).
3. **Utility methods & constants:** `Integer.parseInt("42")`, `Integer.MAX_VALUE`, etc.

---

## 3. `==` vs `.equals()` — the core rule

> **`==` compares what the variable holds. `.equals()` compares logical value.**
>
> - For **primitives**, `==` compares the actual values (works exactly as expected).
> - For **objects**, `==` compares **references** — "are these two variables pointing at the literal same object in memory?" — **not** whether their contents are equal.
>
> `.equals()` is a method a class defines to mean "are we logically/value equal?" `String.equals()` compares character by character.

```java
// PRIMITIVES — == is fine
int a = 5, b = 5;
a == b;            // true — compares values

// OBJECTS — == is the trap
String x = new String("hi");
String y = new String("hi");
x == y;            // false! two different objects in memory
x.equals(y);       // true  — same characters
```

**PHP context switch:** in PHP, `$x == $y` on two strings compares the text. In Java, `==` on two `String` objects compares *identity*, not text.

**Rule of thumb:** never use `==` to compare Strings (or any objects) for value — always `.equals()`. `==` is only reliable on primitives.

---

## 4. Hidden mechanism #1 — the String Pool

When you write a **String literal** (`"hi"` directly in code), Java does not create a fresh object each time. It keeps a special table called the **String Pool** (a.k.a. intern pool). The first time it sees `"hi"`, it creates one String object there; every other literal `"hi"` in your code points at that **same pooled object**.

```java
String p = "hi";
String q = "hi";
p == q;            // true  — both reference the same pooled object

String a = new String("hi");
a == "hi";         // false — new String() forces a fresh heap object, bypassing the pool
```

- `"hi"` (literal) → pooled, shared → `==` can be `true`.
- `new String("hi")` → new object every time → `==` is `false`.

The takeaway is **not** "== sometimes works for strings." It's that `==` on strings is **unpredictable** — it depends on whether the string was pooled, which you often can't see at a glance. That is precisely **why the rule is "always use `.equals()` for strings."**

`new String("hi")` is also a mild **code smell**: it allocates a redundant object when an identical pooled one already exists, for no benefit. Prefer the literal `"hi"`.

### `intern()`

`String.intern()` returns the **pooled** version of a string:

```java
String a = new String("hi");   // not pooled
String b = a.intern();         // returns the pooled "hi"
b == "hi";                     // true
```

---

## 5. Hidden mechanism #2 — the Integer Cache

When you autobox an `int` into an `Integer`, Java **caches** wrapper objects for the range **−128 to 127**. Values in that range return a **shared cached object**; values outside it get a **fresh object** each time.

```java
Integer m = 100, n = 100;
m == n;            // true  — 100 is in −128..127, both share the cached object

Integer big1 = 1000, big2 = 1000;
big1 == big2;      // false — 1000 is outside the cache, two separate objects
big1.equals(big2); // true  — value comparison is reliable
```

- The range is `−128..127` because that's the `byte` range; small integers (loop counters, etc.) are used constantly, so caching them saves millions of tiny allocations.
- It is implemented in `Integer.valueOf()`, which is exactly what autoboxing calls under the hood.

**Classic production bug:** using `==` to compare two `Integer` IDs from a database passes every test (small IDs ≤ 127 fall in the cache and are shared), then silently breaks in production the moment an ID exceeds 127. **Fix:** use `.equals()`, or unbox to `int` before comparing.

---

## 6. Autoboxing / unboxing & the NPE trap

**Autoboxing / unboxing** is Java automatically converting between primitive and wrapper:

```java
Integer boxed = 5;      // autoboxing:  int 5 → Integer   (calls Integer.valueOf(5))
int prim = boxed;       // unboxing:    Integer → int     (calls boxed.intValue())
```

Convenient, but it hides a trap. Since an `Integer` can be `null` but an `int` cannot, unboxing a `null` wrapper throws:

```java
Integer count = null;   // legal — object reference
int x = count;          // NullPointerException at runtime
```

Java tries to call `count.intValue()` to unbox — but `count` is `null` → **NPE**. It's sneaky because **there is no visible method call in your code**; the unboxing is injected by the compiler. This commonly bites with values from a database or a map:

```java
Map<String, Integer> scores = ...;
int s = scores.get("missing");   // .get() returns null → auto-unbox → NPE
```

**Rule:** any time a wrapper *might* be null and you use it in a primitive context, you risk an NPE. Null-check first, or keep the value as the wrapper type.

---

## 7. The one rule that ties the module together

> **Primitives hold values; wrappers are objects that hold references.** `==` compares references for objects, and identity is polluted by hidden optimizations you can't see (the string pool, the Integer cache) — so for **any** object value-comparison (Strings, wrappers, your own classes) use `.equals()`. `==` is only safe on primitives. And beware that autoboxing/unboxing silently converts between the two, which can NPE on a null wrapper.

---

## 8. Interview checklist

- **`==` vs `.equals()`?** — reference vs value; always `.equals()` for objects.
- **Can an `int` be null?** — No. Can an `Integer`? — Yes.
- **Why can't you write `List<int>`?** — collections store objects; use `List<Integer>`.
- **String pool:** literals are interned/shared; `new String()` forces a new object.
- **Integer cache −128..127:** the `Integer ==` production bug and its fix.
- **Unboxing a null wrapper → NPE**, with no visible method call.
