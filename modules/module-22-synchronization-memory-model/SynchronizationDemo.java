import java.util.concurrent.CountDownLatch;

public class SynchronizationDemo {

    static int plainCount = 0;
    static volatile int volatileCount = 0;
    static int syncCount = 0;
    static final Object lock = new Object();

    static void runConcurrentIncrements(int threads, int perThread, Runnable increment) throws InterruptedException {
        Thread[] ts = new Thread[threads];
        for (int i = 0; i < threads; i++) {
            ts[i] = new Thread(() -> {
                for (int j = 0; j < perThread; j++) increment.run();
            });
        }
        for (Thread t : ts) t.start();
        for (Thread t : ts) t.join();
    }

    static void runRaceDemo() throws InterruptedException {
        int threads = 10;
        int perThread = 100_000;

        plainCount = 0;
        runConcurrentIncrements(threads, perThread, () -> plainCount++);

        volatileCount = 0;
        runConcurrentIncrements(threads, perThread, () -> volatileCount++);

        syncCount = 0;
        runConcurrentIncrements(threads, perThread, () -> { synchronized (lock) { syncCount++; } });

        int expected = threads * perThread;
        System.out.println("1) expected total: " + expected);
        System.out.println("2) plain int (unsynchronized): " + plainCount
                + (plainCount == expected ? "" : "  <- LOST UPDATES"));
        System.out.println("3) volatile int (visibility only): " + volatileCount
                + (volatileCount == expected ? "" : "  <- LOST UPDATES (volatile != atomic)"));
        System.out.println("4) synchronized int: " + syncCount
                + (syncCount == expected ? "  <- exactly correct" : "  <- UNEXPECTED"));
    }

    // Reentrancy: an instance-synchronized method calling another on the same object/thread.
    synchronized void reentrantOuter() {
        System.out.println("5) reentrantOuter() holds the instance lock, calling reentrantInner()...");
        reentrantInner();
    }
    synchronized void reentrantInner() {
        System.out.println("6) reentrantInner() re-entered the SAME lock, same thread -- no self-deadlock");
    }

    // Static-synchronized and instance-synchronized use DIFFERENT locks.
    synchronized void instanceMethod(CountDownLatch releaseSignal, CountDownLatch startedSignal) throws InterruptedException {
        System.out.println("7) instanceMethod() acquired the INSTANCE lock and is holding it...");
        startedSignal.countDown();
        releaseSignal.await();
        System.out.println("7b) instanceMethod() releasing the instance lock");
    }
    static synchronized void staticMethod() {
        System.out.println("8) staticMethod() acquired the STATIC (class) lock");
    }

    public static void main(String[] args) throws Exception {
        runRaceDemo();

        SynchronizationDemo instance = new SynchronizationDemo();
        instance.reentrantOuter();

        CountDownLatch releaseSignal = new CountDownLatch(1);
        CountDownLatch startedSignal = new CountDownLatch(1);
        Thread holder = new Thread(() -> {
            try {
                instance.instanceMethod(releaseSignal, startedSignal);
            } catch (InterruptedException ignored) {}
        });
        holder.start();
        startedSignal.await(); // instanceMethod has definitely acquired the instance lock by now

        long start = System.nanoTime();
        staticMethod(); // must NOT block on the still-held instance lock if the locks are truly different
        long elapsedMs = (System.nanoTime() - start) / 1_000_000;
        System.out.println("9) staticMethod() returned after " + elapsedMs
                + " ms while the instance lock was still held -- proves separate locks");

        releaseSignal.countDown();
        holder.join();
    }
}
