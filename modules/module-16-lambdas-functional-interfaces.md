# Module 16 — Lambdas & Functional Interfaces

> Phase 3 — Modern Functional Java. Priority: **High**. Opens Phase 3.

Companion demo: [`module-16-lambdas-functional-interfaces/LambdaDemo.java`](./module-16-lambdas-functional-interfaces/LambdaDemo.java)

**PHP bridge:** closures (`function() use ($x) {}`, arrow functions `fn($x) => ...`) mean the *concept* isn't new. Java's specific rules around typing, capture, and `this` are what need real attention.

---

## 1. Functional interfaces — the SAM rule

A **functional interface** is any interface with exactly **one abstract method** (default/static methods don't count — Module 6). That method is the shape a lambda fills in. `@FunctionalInterface` is optional but makes the compiler enforce the one-abstract-method constraint — a safety net, not a requirement.

```java
Runnable r = () -> System.out.println("running");
Comparator<String> byLength = (a, b) -> a.length() - b.length();
```

## 2. Lambdas vs anonymous classes — not just shorter syntax

Two real differences from Module 10's anonymous classes:

**Compilation:** an anonymous class gets its own compiled `.class` file (`Outer$1.class`) at compile time. A lambda compiles to an `invokedynamic` call; the implementation class is generated at runtime by `LambdaMetafactory`, only on first use — no separate `.class` file per lambda.

**`this` binding.** An anonymous class gets its *own* `this` (a real, separate object); a lambda does **not** — `this` inside a lambda is the **enclosing instance**, resolved lexically, exactly as if the lambda body were ordinary code in the surrounding method. Confirmed in `LambdaDemo.java`:

```
anonymous class 'this' class name:                     <- empty! (anonymous classes have no simple name)
reaching outer field needs LambdaDemo.this.value: 100   <- had to qualify with Outer.this
lambda 'this' class name: LambdaDemo                    <- this IS the enclosing instance
lambda sees enclosing field directly via this.value: 100
```

The anonymous class needed `LambdaDemo.this.value` to reach the outer field (its own `this` is the `Runnable`). The lambda's `this` *is* `LambdaDemo` directly. **Say it exactly like this:** *"A lambda doesn't introduce a new `this` — it captures the enclosing one lexically. An anonymous class always gets its own."*

**Variable capture uses the identical rule already learned in Module 10:** captured locals must be effectively final (frozen copy at capture time); enclosing instance fields are read live through the same enclosing reference.

## 3. The four core functional interfaces (`java.util.function`)

| Interface | Method | Shape |
|---|---|---|
| `Function<T,R>` | `R apply(T t)` | transform |
| `Predicate<T>` | `boolean test(T t)` | filter condition |
| `Consumer<T>` | `void accept(T t)` | side-effect, no return |
| `Supplier<T>` | `T get()` | no input, produces a value |

Each has default methods for chaining:

```java
doubleIt.andThen(addTen).apply(5)   // = addTen(doubleIt(5)) = 20 -- doubleIt runs FIRST
doubleIt.compose(addTen).apply(5)   // = doubleIt(addTen(5)) = 30 -- addTen runs FIRST
isLong.and(startsWithA).test("Alpha")  // true
isLong.negate().test("Al")             // true
```

`andThen` runs the receiver first, then the argument; `compose` runs the argument first, then the receiver. Primitive specializations (`IntPredicate`, `ToIntFunction<T>`, `IntUnaryOperator`, ...) exist for the same reason Module 3's wrapper classes cost you: `Predicate<Integer>` boxes every value; `IntPredicate` doesn't.

## 4. Method references — four kinds

```java
Consumer<String> printer = System.out::println;      // bound instance:   receiver already fixed
Supplier<List<String>> listFactory = ArrayList::new;  // constructor reference
Function<String, Integer> parse = Integer::parseInt;  // static method
Function<String, String> upper = String::toUpperCase; // unbound instance: receiver becomes the argument
```

The one that trips people up: `String::toUpperCase` looks static but isn't — it's an instance method with no arguments, so the method reference's single parameter *becomes the receiver* (`s -> s.toUpperCase()`). Contrast with `System.out::println`, where the receiver is already fixed and the lambda's parameter becomes the method's *argument*. Same-looking syntax, different binding — check whether a receiver is already named before the `::`.

## PHP contrast

PHP closures capture by value via explicit `use ($x)`; Java lambdas capture implicitly, but only effectively-final locals. PHP has no equivalent of the lambda/anonymous-class `this`-binding distinction, since PHP closures don't have the "two different ways to define an inline behavior object" split Java has.

## Interview checklist

- SAM rule; `@FunctionalInterface` is enforcement, not a requirement.
- Lambdas compile via `invokedynamic`/`LambdaMetafactory` at runtime, not a `.class` file per lambda like anonymous classes.
- `this` in a lambda = enclosing instance (lexical); `this` in an anonymous class = its own instance.
- Effectively-final capture rule is identical to Module 10's.
- `Function`/`Predicate`/`Consumer`/`Supplier` — know the shape of each cold.
- `andThen` (receiver first) vs `compose` (argument first).
- Primitive specializations (`IntPredicate`, etc.) exist to avoid autoboxing — callback to Module 3.
- Four method-reference kinds: static, bound-instance, unbound-instance (receiver becomes the parameter), constructor.
