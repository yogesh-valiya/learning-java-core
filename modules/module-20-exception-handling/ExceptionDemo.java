public class ExceptionDemo {

    static int returnInFinally() {
        try {
            throw new RuntimeException("boom");
        } finally {
            return 2; // swallows the exception above entirely -- never do this
        }
    }

    static class NoisyResource implements AutoCloseable {
        final String name;
        final boolean throwOnClose;
        NoisyResource(String name, boolean throwOnClose) {
            this.name = name;
            this.throwOnClose = throwOnClose;
        }
        @Override public void close() {
            System.out.println("   closing " + name);
            if (throwOnClose) throw new IllegalStateException("close failed for " + name);
        }
    }

    static class OrderProcessingException extends RuntimeException {
        OrderProcessingException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public static void main(String[] args) {
        // 1) A return inside finally silently swallows an in-flight exception.
        System.out.println("1) returnInFinally() = " + returnInFinally());

        // 2) try-with-resources closes in REVERSE declaration order.
        try (NoisyResource r1 = new NoisyResource("r1", false);
             NoisyResource r2 = new NoisyResource("r2", false)) {
            System.out.println("2) inside try block");
        }

        // 3) If the try block throws AND close() also throws, the ORIGINAL
        // exception wins and propagates; close()'s exception is suppressed, not swapped in.
        try {
            try (NoisyResource r = new NoisyResource("r3", true)) {
                throw new RuntimeException("original failure in try block");
            }
        } catch (RuntimeException e) {
            System.out.println("3) caught: " + e.getMessage());
            for (Throwable suppressed : e.getSuppressed()) {
                System.out.println("   suppressed: " + suppressed.getClass().getSimpleName()
                        + ": " + suppressed.getMessage());
            }
        }

        // 4) Custom exception chaining the original cause.
        try {
            try {
                throw new IllegalArgumentException("bad payment token");
            } catch (IllegalArgumentException e) {
                throw new OrderProcessingException("order #123 failed", e);
            }
        } catch (OrderProcessingException e) {
            System.out.println("4) caught: " + e.getMessage() + ", cause: " + e.getCause());
        }
    }
}
