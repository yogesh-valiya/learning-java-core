# Module 27 — I/O & Serialization

> Phase 7 — Rounding Out. Priority: **Medium**. Fills in practical corners before interview consolidation.

Companion demo: [`module-27-io-serialization/IOSerializationDemo.java`](./module-27-io-serialization/IOSerializationDemo.java)

---

## 1. Byte streams vs character streams

- **Byte streams** — `InputStream`/`OutputStream` (`FileInputStream`/`FileOutputStream`) — raw bytes, no interpretation.
- **Character streams** — `Reader`/`Writer` (`FileReader`/`FileWriter`) — text, requiring a **charset** to translate bytes correctly.

The split exists because treating raw bytes as text without a defined encoding is exactly how mojibake happens. `InputStreamReader`/`OutputStreamWriter` bridge the two worlds explicitly, letting you specify a `Charset` at the boundary.

## 2. The decorator pattern, wall to wall

`java.io` is essentially the Decorator pattern (already known from the PHP/OOP background) built out wall-to-wall:

```java
try (BufferedReader reader = new BufferedReader(
        new InputStreamReader(
                new FileInputStream("data.txt"), StandardCharsets.UTF_8))) {
    String line = reader.readLine();
}
```

`FileInputStream` reads raw bytes; `InputStreamReader` decodes them via a charset; `BufferedReader` adds buffering plus `readLine()`. Every layer wraps the one inside it, adding exactly one capability. Every stream/reader/writer is `AutoCloseable` — precisely what try-with-resources (Module 20) is for.

## 3. NIO.2 — the modern default

```java
Path path = Paths.get("data.txt");
Files.writeString(path, "hello");
String content = Files.readString(path);
```

Confirmed both styles reading the identical file:

```
1) NIO.2 read: hello via NIO.2
2) decorator-chain read: hello via NIO.2
```

**Real reason to prefer NIO.2:** `java.io.File`'s methods mostly signal failure via `false`/`0` — unhelpful for debugging. `Files` methods throw specific exceptions instead (`NoSuchFileException`, `AccessDeniedException`) — an actual answer instead of a guess. (Lower-level NIO — `Channel`/`ByteBuffer`, used for high-performance/non-blocking I/O — is a separate, deeper topic beyond "NIO basics.")

## 4. `Serializable` and its real pitfalls

Marker interface (Module 9's marker-interface pattern) — signals "convertible to/from a byte stream" via `ObjectOutputStream`/`ObjectInputStream`.

**`serialVersionUID`:** unspecified, the JVM computes one from the class's structure — a trivial unrelated change can silently change it, and deserializing an old byte stream against a class with a different computed UID throws `InvalidClassException`. **Always declare it explicitly.**

**`transient`:** skips a field during serialization; it gets its **default value** on deserialization, never the original. Confirmed:

```
3) original sessionToken: super-secret-token
4) restored sessionToken (transient): null
5) restored username (not transient): ravi
```

**Two genuinely dangerous pitfalls, both confirmed:**
1. A non-`Serializable` field left unmarked throws `NotSerializableException` — **at runtime**, not compile time (the compiler has no way to catch it, since `Serializable` enforces nothing structurally). Confirmed: a `Thread` field caused `NotSerializableException: java.lang.Thread` the moment serialization was actually attempted.
2. **Deserialization reconstructs an object without ever calling a constructor** — a class validating invariants (or defensively copying, Module 8) only in its constructor gets none of that protection against a crafted or corrupted byte stream. Modern practice increasingly avoids `Serializable` in application code for exactly this reason, preferring JSON (Jackson)/protobuf with their own validation.

## PHP contrast

PHP's `serialize()`/`unserialize()` shares almost the identical pitfall list — `__wakeup()` is PHP's equivalent escape hatch, existing precisely because unserializing untrusted data bypassing the constructor is a well-documented PHP security issue ("PHP object injection"). The caution transfers directly.

## Interview checklist

- Byte streams vs character streams — the split exists because text needs a charset, bytes don't.
- `java.io` is the Decorator pattern in practice.
- Prefer `java.nio.file.Path`/`Files` over legacy `java.io.File` — real exceptions instead of silent `false`/`0`.
- `Serializable` is a marker interface; always declare `serialVersionUID` explicitly.
- `transient` fields get their default value on deserialization — confirmed live.
- A non-`Serializable`, non-`transient` field throws `NotSerializableException` at runtime, not compile time — confirmed live.
- Deserialization bypasses constructors entirely — a real security/invariant concern.
