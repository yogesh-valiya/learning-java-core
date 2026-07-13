# Module 26 — Class Loading, Reflection & Annotations

> Phase 6 — JVM Under the Hood. Priority: **Medium**. Final module of Phase 6. Annotations here are a direct bridge to the Spring project this course leads into — Spring, JUnit, and Jackson all run on exactly the reflection/annotation mechanics in this module.

Companion demo: [`module-26-class-loading-reflection-annotations/ReflectionDemo.java`](./module-26-class-loading-reflection-annotations/ReflectionDemo.java)

---

## 1. Class loading

Classes load via a **delegation hierarchy**: Bootstrap (core JDK classes) → Platform → Application/System (your classpath). **Parent-first delegation** — a classloader asks its parent before trying itself, which is why user code can't accidentally shadow `java.lang.String`.

A class loads **lazily**, on first active use (first instantiation, first static member access) — not just because it's on the classpath. Phases: **Loading** (find bytecode, create the `Class` object) → **Linking** (Verification, Preparation — static fields get default values, not real ones yet, Resolution) → **Initialization** (static initializers run, static fields get their real values — Module 5's "static init runs once at class load").

**`ClassNotFoundException`** — the class file genuinely can't be found. **`NoClassDefFoundError`** — the class *was* available, but failed to initialize (commonly: its static initializer threw once). Confirmed live: a class whose static initializer divides by zero threw `ExceptionInInitializerError` (wrapping the real `ArithmeticException`) on the **first** reference, then `NoClassDefFoundError` on the **second** — the JVM marks the class erroneous after the first failed attempt and never retries initialization.

## 2. Reflection

```java
Derived.class.getFields();           // PUBLIC fields only, INCLUDING inherited ones
Derived.class.getDeclaredFields();    // ALL fields declared directly here, private included, NOT inherited
```

Confirmed with a `Derived` class (private field `secret`) extending `Base` (public field `basePublicField`):

```
1) getFields(): [basePublicField]
2) getDeclaredFields(): [secret]
```

Completely disjoint sets — `getFields()` mirrors the public API surface including inheritance; `getDeclaredFields()` mirrors what's declared directly in the class file, access modifier irrelevant, no inheritance.

```java
Field secretField = Derived.class.getDeclaredField("secret");
secretField.setAccessible(true);      // bypass the private access check
secretField.get(d); secretField.set(d, value);
```

Confirmed reading and overwriting a `private` field from outside the class entirely. **This is genuinely how dependency injection works:** Spring reflectively injects private `@Autowired` fields; Jackson reflectively (de)serializes private fields; JUnit reflectively invokes `@Test` methods. Two real caveats: reflective calls are measurably slower than direct calls, and JPMS increasingly restricts `setAccessible` against non-open JDK-internal packages.

## 3. Annotations

```java
@Retention(RetentionPolicy.RUNTIME)   // without this, reflection can't see it
@Target(ElementType.METHOD)
@interface MyTest {
    String value() default "";
}
```

**Meta-annotations:**
- **`@Retention`** — `SOURCE` (compiler-only), `CLASS` (in bytecode, **not** visible via reflection — the default if omitted), `RUNTIME` (reflectively visible — required for any framework to detect it).
- **`@Target`** — restricts legal placement (`METHOD`, `FIELD`, `TYPE`, ...).
- **`@Inherited`** — class-level annotations only, inherited by subclasses.
- **`@Documented`** — included in generated Javadoc.

Confirmed the retention distinction directly: a `@MyTest` method (`RUNTIME` retention) reported `isAnnotationPresent == true` and its `getAnnotation()` returned the real value (`"greet check"`); an `@NotVisible` method (default `CLASS` retention, no `@Retention` specified) reported `isAnnotationPresent == false` at runtime — **the exact same reflective check, the only difference is the retention policy.**

```java
if (method.isAnnotationPresent(Test.class)) {
    method.invoke(instance);   // mechanically what a test runner does
}
```

This scan-detect-invoke pattern **is** how Spring finds `@Component`/`@Autowired`, how JUnit finds `@Test`, how JPA finds `@Entity`/`@Column` — no additional magic, just this mechanism wired into a framework's startup.

## PHP contrast

PHP 8 attributes (`#[Attribute]`) are the direct structural equivalent — reflectively readable metadata used by frameworks (Symfony, Doctrine) exactly the way Spring/JPA use Java annotations.

## Interview checklist

- Classloader delegation is parent-first; classes load lazily on first active use.
- Linking phases: verify → prepare (defaults) → resolve; initialization then runs static blocks (Module 5 callback).
- `ClassNotFoundException` = genuinely missing; `NoClassDefFoundError` = available but failed to initialize — confirmed the first-`ExceptionInInitializerError`-then-`NoClassDefFoundError` sequence live.
- `getFields()`/`getMethods()` = public + inherited; `getDeclared*()` = everything declared directly, private included, not inherited — confirmed as fully disjoint sets.
- `setAccessible(true)` is the actual mechanism behind Spring/Jackson/JUnit — real performance cost, increasingly JPMS-restricted.
- `@Retention(RUNTIME)` is mandatory for reflective visibility — confirmed the same `isAnnotationPresent` check returning `true` vs `false` purely based on retention policy.
- Annotation processing (`isAnnotationPresent`/`getAnnotation`) is mechanically what DI/testing/ORM frameworks do at scale.
