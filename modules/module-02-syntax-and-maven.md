# Module 2 — Syntax Map for PHP Devs + Maven Basics

> Phase 0 — Orientation. Priority: **Low** (fast-tracked cheat-sheet). Assumes you already know loops, conditionals, operators, and OOP concepts — this only maps Java's *spelling* of things you know and flags where behavior differs.

---

## Part 1 — Core syntax

### Types: Java is statically typed

Every variable's type is fixed at compile time and declared explicitly. There is no `$` sigil.

```java
int age = 30;
double price = 19.99;
boolean active = true;
String name = "Yogesh";   // capital S — String is a class, not a primitive
```

- In PHP, `$age = 30;` needs no declaration and the type can change at runtime.
- In Java, `age = "thirty";` is a **compile error**, not a runtime surprise. The compiler catches a whole class of type bugs before the program ever runs.

### Operators: ~95% identical to PHP, with one classic trap

Arithmetic, comparison, logical (`&&`, `||`, `!`), and ternary (`? :`) all behave like PHP. The trap is **integer division**:

```java
System.out.println(7 / 2);     // 3    — int / int = int, decimal truncated
System.out.println(7 / 2.0);   // 3.5  — one operand is double → floating-point math
System.out.println(9 / 4);     // 2    — 2.25 truncated to 2
```

PHP's `7 / 2` always gives `3.5`. Java's `int / int` **truncates** the decimal unless at least one operand is a floating type. This is the classic *"why did my average come out as 0?"* bug — e.g. `4 / 9` is `0` because `0.44…` truncates.

### Control flow: same shapes, different `foreach`

`if/else`, `while`, `do-while`, and `switch` map directly from PHP. The enhanced-for is spelled differently:

```java
for (String s : names) {      // Java "enhanced for" — like PHP foreach
    System.out.println(s);
}
```

```php
foreach ($names as $s) {      // PHP
    echo $s;
}
```

### Arrays: fixed-size, typed

```java
int[] nums = {1, 2, 3};
String[] names = new String[5];   // fixed length, pre-sized
int n = nums.length;              // .length is a FIELD — no parentheses
```

- PHP arrays are dynamic ordered maps; they do **not** map 1:1 to Java arrays. A Java array is a **fixed block of typed slots**.
- For growable / map-like behavior (what PHP arrays give you), Java uses the **Collections framework** (`ArrayList`, `HashMap`, …) — covered in Phase 2. In real Java code, collections are your everyday tool; raw arrays are comparatively rare.

**Gotcha to bank:** `array.length` is a **field** (no parens), but `list.size()` on a Collection is a **method** (parens). The inconsistency is historical — arrays predate the Collections framework (added in Java 1.2).

---

## Part 2 — Packages & the `main` method

### Packages = namespace + mandatory folder structure

```java
package com.example.myapp;

import java.util.List;
```

The package declaration **must match the physical directory path**: this file must live at `com/example/myapp/…`. This is **enforced by `javac`**, unlike PHP's PSR-4 which is a convention interpreted by Composer's autoloader. Wrong folder → it won't compile.

### The entry-point signature is exact

```java
public class App {
    public static void main(String[] args) {
        // program starts here
    }
}
```

- **`public`** — the JVM calls it from outside the class.
- **`static`** — the JVM calls it before any object of the class exists, so it can't be an instance method.
- **`void`** — returns nothing to the OS.
- **`String[] args`** — command-line arguments (like PHP CLI's `$argv`).

If a class has **no `main`**, it still **compiles**, but running it fails **at launch**: `Error: Main method not found in class X`. That's a runtime launch error, not a compile error.

### Filename must match the public class name

`Hello.java` must contain `public class Hello`. A file may contain multiple classes but **at most one `public`** class, and that one dictates the filename. Compiler-enforced, not convention. Renaming `Hello.java` → `Greeting.java` (while it still holds `public class Hello`) fails to compile: *"class Hello is public, should be declared in a file named Hello.java."*

---

## Part 3 — Maven basics

Maven is Java's build tool **and** dependency manager — think **Composer + a build lifecycle** combined.

### Translation table (Composer → Maven)

| Concept | PHP / Composer | Java / Maven |
|---------|----------------|--------------|
| Manifest file | `composer.json` | `pom.xml` (XML) |
| Dependency source | Packagist | Maven Central |
| Downloaded deps location | `vendor/` (per project) | `~/.m2/repository` (**shared, machine-wide** cache) |
| Lockfile | `composer.lock` | (no true default equivalent — versions pinned directly in `pom.xml`) |
| Package identifier | `vendor/package` | `groupId:artifactId:version` ("GAV" coordinates) |

The `~/.m2` difference matters: Maven downloads each dependency **once per machine** and all projects share it, rather than copying into a per-project `vendor/`.

### A minimal `pom.xml`

```xml
<project>
    <modelVersion>4.0.0</modelVersion>

    <!-- Your project's own coordinates -->
    <groupId>com.example</groupId>
    <artifactId>myapp</artifactId>
    <version>1.0.0</version>

    <dependencies>
        <dependency>
            <groupId>com.google.guava</groupId>
            <artifactId>guava</artifactId>
            <version>33.0.0-jre</version>
        </dependency>
    </dependencies>
</project>
```

A `<dependency>` block is the equivalent of one line in Composer's `"require": { … }`.

- In GAV, **`groupId` ≈ Composer's vendor name**, **`artifactId` ≈ the package name**.

### Standard directory layout (convention over configuration)

```
myapp/
├── pom.xml
└── src/
    ├── main/
    │   └── java/          ← application source (packages start here)
    │       └── com/example/App.java
    └── test/
        └── java/          ← test source
```

Put sources in `src/main/java` and tests in `src/test/java` and Maven works with zero config — the same philosophy as Composer expecting a PSR-4 layout, but stricter.

### The lifecycle (the part interviewers probe)

Maven runs in **phases**. Phases are **ordered and cumulative** — running a phase runs every phase before it.

| Command | What it does |
|---------|--------------|
| `mvn compile` | source → `.class` files in `target/` |
| `mvn test` | compiles, then runs unit tests |
| `mvn package` | compiles + tests + **bundles into a `.jar`** in `target/` |
| `mvn install` | above + copies the `.jar` into `~/.m2` so **other local projects** can depend on it |
| `mvn clean` | deletes the `target/` build output |

Key points:

- `mvn package` **automatically** runs `compile` and `test` first — you don't invoke them separately in a real build.
- **A failing test blocks the build** (`package`/`install` won't complete) — a deliberate quality gate. In PHP, tests are usually a separate opt-in step.
- **`package` vs `install`:** `package` produces the jar in `target/`; `install` *additionally* puts it in your local `~/.m2` for other projects to consume. Common "do you actually use Maven?" check.
- `mvn clean install` (clean, then full build + install) is the command you'll type most often.

### JARs

- A **JAR** ("Java ARchive") is a zip of `.class` files + metadata.
- A **fat / uber JAR** additionally bundles your dependencies inside, so it runs standalone (`java -jar myapp.jar`). Producing one needs a plugin like the Maven Shade plugin. Spring Boot produces fat JARs, which is why `java -jar` "just works" for a Boot app.

> **Gradle** is the other major JVM build tool (uses Groovy/Kotlin DSL instead of XML). Maven dominates enterprise Java, so this course focuses on Maven. For interviews, understanding the **lifecycle, GAV coordinates, and `~/.m2`** matters far more than memorizing `pom.xml` XML.
