# Module 20 — Exception Handling

> Phase 4 — Exceptions. Priority: **High**. Single-module phase, heavily drilled in interviews.

Companion demo: [`module-20-exception-handling/ExceptionDemo.java`](./module-20-exception-handling/ExceptionDemo.java)

**PHP bridge:** PHP has **no checked exceptions at all** — every PHP exception behaves like Java's *unchecked* category. The checked/unchecked split and its compiler enforcement is genuinely new mechanics here.

---

## 1. The `Throwable` hierarchy

```
Throwable
 ├── Error              -- serious JVM-level failures (OutOfMemoryError, StackOverflowError),
 │                          not meant to be caught/handled by application code
 └── Exception
      ├── RuntimeException  -- UNCHECKED: NullPointerException, IllegalArgumentException,
      │                        IndexOutOfBoundsException... programming errors
      └── (everything else) -- CHECKED: IOException, SQLException...
                               recoverable conditions the caller should plan for
```

**Checked exceptions** are compiler-enforced — catch or declare `throws`, or it won't compile. **Unchecked exceptions** carry no such enforcement — they can propagate silently to the top.

Checked exceptions are genuinely controversial in the Java community: they interact badly with lambdas/streams (can't throw one from most standard functional interfaces without wrapping it unchecked), and plenty of modern Java code deliberately favors unchecked exceptions even for recoverable conditions to avoid that friction.

## 2. try-with-resources

Any `AutoCloseable` (single method `close()`) can be declared in a `try`'s parentheses:

```java
try (NoisyResource r1 = new NoisyResource("r1", false);
     NoisyResource r2 = new NoisyResource("r2", false)) {
    // use r1, r2
}   // both closed automatically, even on exception
```

Confirmed in `ExceptionDemo.java`: resources close in **reverse** declaration order (`r2` closes before `r1`). And if the try block throws **and** `close()` also throws, the **original exception wins** and propagates; the `close()`-time exception is attached as a **suppressed exception** rather than replacing it:

```
3) caught: original failure in try block
   suppressed: IllegalStateException: close failed for r3
```

This directly fixes a real bug in the old manual-`finally`-close pattern, where a throwing `close()` could silently **replace** (mask) the original exception — the actual root cause could vanish from the stack trace entirely.

## 3. Custom exceptions

Extend `Exception` for checked, `RuntimeException` for unchecked. **Always chain the cause** when wrapping:

```java
class OrderProcessingException extends RuntimeException {
    OrderProcessingException(String message, Throwable cause) {
        super(message, cause);   // preserves the original via getCause()
    }
}
```

Confirmed: catching a wrapped `OrderProcessingException` still exposes the original `IllegalArgumentException` via `getCause()`. Never swallow the original exception when translating between types.

## 4. `finally` semantics — and its most dangerous gotcha

`finally` runs **always** (try completes normally, throws, or hits `return`/`break`/`continue`) — barring a JVM crash, `System.exit()`, or an infinite loop.

**The gotcha:** a `return` (or `throw`) inside `finally` silently overrides the try/catch outcome — including swallowing an in-flight exception with **zero trace**:

```java
static int broken() {
    try {
        throw new RuntimeException("boom");
    } finally {
        return 2;   // the RuntimeException is GONE
    }
}
```

Confirmed: `returnInFinally()` returns `2` — no exception ever reaches the caller. **Never put a `return` inside a `finally` block.**

## 5. Best practices

- Catch the most specific exception you can actually recover from — not a blanket `catch (Exception e)` outside a genuine top-level boundary.
- Never leave a catch block empty — at minimum, log it.
- Don't use exceptions for ordinary control flow (stack trace capture isn't free; it obscures normal logic).
- Always chain the cause when wrapping/rethrowing as a different exception type.
- Prefer try-with-resources over manual `finally`-based cleanup.

## Interview checklist

- `Error` = JVM-level, don't catch. `RuntimeException` = unchecked. Everything else under `Exception` = checked, compiler-enforced.
- PHP has no checked-exception equivalent — this enforcement mechanism is Java-specific.
- try-with-resources: reverse-order close; a `close()`-time exception is suppressed (`addSuppressed`/`getSuppressed`), not swapped in for the original.
- Custom exceptions must chain the cause (`super(message, cause)`).
- `finally` always runs; a `return`/`throw` inside it silently discards any in-flight exception or return value — never do this.
- Catch specific, log don't swallow, don't use exceptions for control flow, chain causes, prefer try-with-resources.
