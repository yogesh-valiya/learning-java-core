import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

public class ConcurrentToolsDemo {

    static void runConcurrent(int threads, int perThread, Runnable task) throws InterruptedException {
        Thread[] ts = new Thread[threads];
        for (int i = 0; i < threads; i++) {
            ts[i] = new Thread(() -> { for (int j = 0; j < perThread; j++) task.run(); });
        }
        for (Thread t : ts) t.start();
        for (Thread t : ts) t.join();
    }

    public static void main(String[] args) throws Exception {
        int threads = 10, perThread = 10_000;
        int expected = threads * perThread;

        // 1) Manual check-then-act (get + put) on a ConcurrentHashMap -- STILL racy.
        ConcurrentHashMap<String, Integer> racyMap = new ConcurrentHashMap<>();
        racyMap.put("counter", 0);
        runConcurrent(threads, perThread, () -> {
            int cur = racyMap.get("counter");
            racyMap.put("counter", cur + 1);
        });
        int racyResult = racyMap.get("counter");
        System.out.println("1) manual get+put on ConcurrentHashMap: " + racyResult
                + " (expected " + expected + ")" + (racyResult == expected ? "" : "  <- LOST UPDATES"));

        // 2) The atomic fix: merge() performs the read-modify-write as ONE operation.
        ConcurrentHashMap<String, Integer> atomicMap = new ConcurrentHashMap<>();
        atomicMap.put("counter", 0);
        runConcurrent(threads, perThread, () -> atomicMap.merge("counter", 1, Integer::sum));
        int mergeResult = atomicMap.get("counter");
        System.out.println("2) map.merge() on ConcurrentHashMap: " + mergeResult
                + (mergeResult == expected ? "  <- exactly correct" : "  <- UNEXPECTED"));

        // 3) AtomicInteger -- lock-free, correctly handles the Module 22 counter race.
        AtomicInteger atomicCounter = new AtomicInteger(0);
        runConcurrent(threads, perThread, atomicCounter::incrementAndGet);
        System.out.println("3) AtomicInteger result: " + atomicCounter.get()
                + (atomicCounter.get() == expected ? "  <- exactly correct, no locks used" : "  <- UNEXPECTED"));

        // 4) BlockingQueue -- Module 22's wait/notify producer-consumer, built in.
        BlockingQueue<Integer> queue = new ArrayBlockingQueue<>(3);
        Thread producer = new Thread(() -> {
            try { for (int i = 1; i <= 5; i++) { queue.put(i); System.out.println("   put " + i); } }
            catch (InterruptedException ignored) {}
        });
        Thread consumer = new Thread(() -> {
            try { for (int i = 1; i <= 5; i++) System.out.println("   took " + queue.take()); }
            catch (InterruptedException ignored) {}
        });
        System.out.println("4) BlockingQueue producer-consumer:");
        producer.start();
        consumer.start();
        producer.join();
        consumer.join();

        // 5) ReentrantLock: tryLock() fails while held by another thread, succeeds after release.
        ReentrantLock lock = new ReentrantLock();
        CountDownLatch locked = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        Thread holder = new Thread(() -> {
            lock.lock();
            try {
                locked.countDown();
                release.await();
            } catch (InterruptedException ignored) {
            } finally {
                lock.unlock(); // the non-negotiable manual unlock
            }
        });
        holder.start();
        locked.await(); // holder definitely owns the lock now

        boolean acquiredWhileHeld = lock.tryLock(100, TimeUnit.MILLISECONDS);
        System.out.println("5) tryLock() while held by another thread: " + acquiredWhileHeld);
        if (acquiredWhileHeld) lock.unlock();

        release.countDown();
        holder.join(); // holder has definitely unlocked by now

        boolean acquiredAfterRelease = lock.tryLock(1, TimeUnit.SECONDS);
        System.out.println("6) tryLock() after release: " + acquiredAfterRelease);
        if (acquiredAfterRelease) lock.unlock();
    }
}
