import java.util.concurrent.*;

public class ExecutorDemo {

    public static void main(String[] args) throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(2);

        // 1) submit() + Future.get() -- basic result retrieval.
        Future<Integer> f1 = pool.submit(() -> 6 * 7);
        System.out.println("1) future result: " + f1.get());

        // 2) A failing task, submitted but Future.get() NEVER called -- the exception is invisible.
        Future<?> f2 = pool.submit(() -> { throw new RuntimeException("silent failure"); });
        Thread.sleep(200); // give it time to actually run and fail
        System.out.println("2) submitted a failing task, did NOT call get() -- no exception surfaced here");

        // 3) The SAME kind of failure, but this time we call get() -- it surfaces, wrapped.
        Future<?> f3 = pool.submit(() -> { throw new RuntimeException("surfaced failure"); });
        try {
            f3.get();
        } catch (ExecutionException e) {
            System.out.println("3) calling get() surfaced: " + e.getClass().getSimpleName()
                    + ", cause: " + e.getCause());
        }

        // 4) Shutdown lifecycle: submitting after shutdown() is rejected.
        pool.shutdown();
        try {
            pool.submit(() -> 1);
        } catch (RejectedExecutionException e) {
            System.out.println("4) submitting after shutdown() threw: " + e.getClass().getSimpleName());
        }

        // 5) CompletableFuture chaining.
        CompletableFuture<Integer> chained = CompletableFuture.supplyAsync(() -> 10)
                .thenApply(x -> x + 5)
                .thenApply(x -> x * 2);
        System.out.println("5) CompletableFuture chained result: " + chained.get());

        // 6) thenCombine -- merge two independent async computations.
        CompletableFuture<Integer> a = CompletableFuture.supplyAsync(() -> 3);
        CompletableFuture<Integer> b = CompletableFuture.supplyAsync(() -> 4);
        CompletableFuture<Integer> combined = a.thenCombine(b, Integer::sum);
        System.out.println("6) thenCombine result: " + combined.get());

        // 7) exceptionally -- recover from a failed stage with a fallback value.
        CompletableFuture<Integer> recovered = CompletableFuture
                .<Integer>supplyAsync(() -> { throw new RuntimeException("cf failure"); })
                .exceptionally(ex -> -1);
        System.out.println("7) exceptionally recovered: " + recovered.get());
    }
}
