# Module 17 — Streams API

> Phase 3 — Modern Functional Java. Priority: **High**. Builds directly on Module 16 — every stream operation is powered by the functional interfaces just covered (`map` takes a `Function`, `filter` takes a `Predicate`, `forEach` takes a `Consumer`).

Companion demo: [`module-17-streams-api/StreamDemo.java`](./module-17-streams-api/StreamDemo.java)

---

## 1. A Stream is a pipeline, not a data structure

A `Stream` holds no elements of its own — it's a **lazy pipeline** describing a computation over a source (a `Collection`, `Arrays.stream(arr)`, `Stream.of(...)`, `IntStream.range(...)`). Two consequences:

- **Nothing runs until a terminal operation is invoked.** `list.stream().filter(...).map(...)` alone does nothing — it builds up a description of work.
- **A stream is single-use.** Once a terminal operation runs, the stream is consumed — calling another terminal operation on the same reference throws `IllegalStateException`.

## 2. Intermediate vs terminal operations

**Intermediate** (`map`, `filter`, `flatMap`, `sorted`, `distinct`, `limit`, `skip`, `peek`) return a new `Stream` and are lazy. **Terminal** (`collect`, `reduce`, `forEach`, `count`, `anyMatch`/`allMatch`/`noneMatch`, `findFirst`/`findAny`, `toArray`) trigger execution and produce a real result. A pipeline with no terminal op is inert.

## 3. The model that actually matters: vertical, element-at-a-time, short-circuiting

The naive mental model — "run `filter` over everything, then `map` over what's left" — is wrong. A pipeline pulls **one element at a time** through every stage before pulling the next — vertical, not horizontal — and **stops the instant the terminal operation is satisfied**. `findFirst`/`anyMatch`/`limit` are **short-circuiting**: they can end the whole pipeline early.

Verified in `StreamDemo.java` — `List.of(1..8).stream().peek(...).filter(even).map(*10).findFirst()`:

```
   peek saw: 1
   peek saw: 2
1) findFirst result: 20
```

Elements 3 through 8 are **never touched** — not by `peek`, not by `filter`, not by `map` — because `findFirst` was satisfied at element 2 and the pipeline stopped immediately.

## 4. `map` vs `flatMap`

`map` is 1-to-1. `flatMap` is 1-to-many and **flattens**: when each input element maps to its own stream/collection, `flatMap` merges all of them into one flat output. `List<List<Integer>>` → `Stream<Integer>` via `nested.stream().flatMap(List::stream)` — confirmed: `[[1,2],[3,4],[5]]` → `[1, 2, 3, 4, 5]`.

## 5. `reduce` — folding to a single value

Three overloads: `reduce(BinaryOperator<T>)` → `Optional<T>`; `reduce(identity, BinaryOperator<T>)` → `T`; `reduce(identity, accumulator, combiner)` — the 3-arg form exists for **parallel** streams (each thread folds its own chunk with the accumulator, then the combiner merges partial results).

## 6. Collectors — the practical workhorse

```java
employees.stream().collect(Collectors.toList());
employees.stream().map(Employee::name).collect(Collectors.joining(", "));
employees.stream().collect(Collectors.groupingBy(Employee::dept));                                     // Map<String,List<Employee>>
employees.stream().collect(Collectors.groupingBy(Employee::dept, Collectors.counting()));              // Map<String,Long>
employees.stream().collect(Collectors.groupingBy(Employee::dept,
        Collectors.mapping(Employee::name, Collectors.toList())));                                      // Map<String,List<String>>
employees.stream().collect(Collectors.partitioningBy(e -> e.salary() > 80000));                          // Map<Boolean,List<Employee>>
```

`groupingBy` alone bundles each group into a `List`; pairing it with a **downstream collector** (`counting()`, `summingInt()`, `mapping(...)`, nested `groupingBy`) reshapes what ends up in each bucket — the single most practically useful thing in this module.

## 7. Primitive streams — the Module 3 callback, again

`IntStream`/`LongStream`/`DoubleStream` exist so numeric pipelines avoid boxing every element — the same autoboxing cost from Module 3 and Module 16's primitive functional interfaces. `mapToInt`/`mapToObj`/`.boxed()` convert at the boundary.

## 8. Parallel streams — a caution, not a deep dive

`.parallelStream()` splits work across `ForkJoinPool.commonPool()` threads. Not a free win: coordination overhead can make it *slower* for small collections or cheap per-element work, and any shared mutable state touched inside the lambda is a race condition waiting to happen. Full mechanics are Phase 5; for now: default to sequential, reach for parallel only with large data and genuinely expensive, stateless per-element work, and measure.

## PHP contrast

PHP's `array_filter`/`array_map`/`array_reduce` chain runs eagerly, producing a full intermediate array at each step (horizontal, not lazy), with no single-use restriction. Java's laziness and short-circuiting have no real PHP-array parallel.

## Interview checklist

- A stream is a lazy pipeline, not a data structure; does nothing until a terminal op runs; consumable exactly once.
- Intermediate ops are lazy; terminal ops trigger execution.
- Processing is vertical (one element through the whole pipeline) and short-circuits on `findFirst`/`anyMatch`/`limit`.
- `map` = 1-to-1; `flatMap` = 1-to-many, flattening.
- `reduce`'s 3-arg overload exists for parallel streams (per-thread accumulate, then combine).
- `groupingBy` + a downstream collector is the practical power move.
- Primitive streams avoid autoboxing — callback to Module 3.
- Parallel streams aren't automatically faster and require stateless, side-effect-free lambdas.
