import java.util.concurrent.*;

public class ThreadDemo {

    public static void main(String[] args) throws Exception {
        // 1) run() vs start() -- run() is just a normal method call, no new thread.
        Thread t1 = new Thread(() -> System.out.println("   run() executes on: " + Thread.currentThread().getName()));
        System.out.println("1) calling t1.run() directly:");
        t1.run();

        Thread t2 = new Thread(() -> System.out.println("   start() executes on: " + Thread.currentThread().getName()));
        System.out.println("2) calling t2.start():");
        t2.start();
        t2.join();

        // 2) A terminated thread cannot be restarted.
        Thread t3 = new Thread(() -> {});
        t3.start();
        t3.join();
        try {
            t3.start(); // already TERMINATED
        } catch (IllegalThreadStateException e) {
            System.out.println("3) restarting a terminated thread threw: " + e.getClass().getSimpleName());
        }

        // 3) Lifecycle states: NEW -> TIMED_WAITING (while sleeping) -> TERMINATED.
        Thread t4 = new Thread(() -> {
            try { Thread.sleep(300); } catch (InterruptedException ignored) {}
        });
        System.out.println("4) state before start(): " + t4.getState());
        t4.start();
        Thread.sleep(100); // give t4 time to actually enter sleep()
        System.out.println("5) state while sleeping: " + t4.getState());
        t4.join();
        System.out.println("6) state after join(): " + t4.getState());

        // 4) Callable + FutureTask -- the way to get a return value out of a thread.
        Callable<Integer> callable = () -> {
            Thread.sleep(50);
            return 6 * 7;
        };
        FutureTask<Integer> futureTask = new FutureTask<>(callable);
        Thread t5 = new Thread(futureTask);
        t5.start();
        System.out.println("7) Callable result via FutureTask.get(): " + futureTask.get());

        // 5) interrupt() wakes a sleeping thread with InterruptedException -- it's cooperative.
        Thread t6 = new Thread(() -> {
            try {
                Thread.sleep(10_000);
                System.out.println("   (should never print)");
            } catch (InterruptedException e) {
                System.out.println("   sleeping thread woke via InterruptedException");
            }
        });
        t6.start();
        Thread.sleep(100); // let t6 actually get into sleep()
        t6.interrupt();
        t6.join();
        System.out.println("8) after interrupt+join, t6 state: " + t6.getState());
    }
}
