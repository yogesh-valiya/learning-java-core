import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.LinkedList;
import java.util.concurrent.CountDownLatch;

public class DeadlockAndWaitNotifyDemo {

    static final Object lockA = new Object();
    static final Object lockB = new Object();

    static void triggerAndDetectDeadlock() throws InterruptedException {
        CountDownLatch bothStarted = new CountDownLatch(2);

        Thread t1 = new Thread(() -> {
            synchronized (lockA) {
                bothStarted.countDown();
                await(bothStarted);
                sleep(50);
                synchronized (lockB) {
                    System.out.println("   t1 got both locks (should never print)");
                }
            }
        }, "t1-locksA-then-B");
        t1.setDaemon(true); // daemon so the JVM can still exit even though these two never finish

        Thread t2 = new Thread(() -> {
            synchronized (lockB) {
                bothStarted.countDown();
                await(bothStarted);
                sleep(50);
                synchronized (lockA) {
                    System.out.println("   t2 got both locks (should never print)");
                }
            }
        }, "t2-locksB-then-A");
        t2.setDaemon(true);

        t1.start();
        t2.start();

        Thread.sleep(1000); // give the circular wait time to fully form

        ThreadMXBean bean = ManagementFactory.getThreadMXBean();
        long[] deadlockedIds = bean.findDeadlockedThreads();
        if (deadlockedIds != null) {
            System.out.println("1) ThreadMXBean detected " + deadlockedIds.length + " deadlocked threads:");
            for (long id : deadlockedIds) {
                ThreadInfo info = bean.getThreadInfo(id);
                System.out.println("   " + info.getThreadName() + " is blocked on " + info.getLockName()
                        + ", owned by " + info.getLockOwnerName());
            }
        } else {
            System.out.println("1) no deadlock detected this run (timing didn't line up)");
        }
    }

    static void await(CountDownLatch latch) {
        try { latch.await(); } catch (InterruptedException ignored) {}
    }
    static void sleep(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException ignored) {}
    }

    // wait/notify producer-consumer over a bounded queue.
    static final Object queueLock = new Object();
    static final LinkedList<Integer> queue = new LinkedList<>();
    static final int CAPACITY = 3;

    static void produce(int value) throws InterruptedException {
        synchronized (queueLock) {
            while (queue.size() == CAPACITY) {
                queueLock.wait(); // MUST be a while loop, not if -- spurious wakeups are real
            }
            queue.add(value);
            System.out.println("   produced " + value + ", queue=" + queue);
            queueLock.notifyAll();
        }
    }

    static int consume() throws InterruptedException {
        synchronized (queueLock) {
            while (queue.isEmpty()) {
                queueLock.wait();
            }
            int value = queue.removeFirst();
            System.out.println("   consumed " + value + ", queue=" + queue);
            queueLock.notifyAll();
            return value;
        }
    }

    public static void main(String[] args) throws Exception {
        triggerAndDetectDeadlock();

        System.out.println("2) wait/notify producer-consumer:");
        Thread producer = new Thread(() -> {
            try { for (int i = 1; i <= 5; i++) produce(i); } catch (InterruptedException ignored) {}
        });
        Thread consumer = new Thread(() -> {
            try { for (int i = 1; i <= 5; i++) consume(); } catch (InterruptedException ignored) {}
        });
        producer.start();
        consumer.start();
        producer.join();
        consumer.join();
        System.out.println("3) producer-consumer finished cleanly, final queue: " + queue);
    }
}
