import java.lang.annotation.*;
import java.lang.reflect.*;
import java.util.Arrays;

public class ReflectionDemo {

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @interface MyTest {
        String value() default "";
    }

    @interface NotVisible {
        // no @Retention specified -- defaults to CLASS, NOT visible via reflection at runtime
    }

    static class Base {
        public String basePublicField = "base";
    }

    static class Derived extends Base {
        private String secret = "hidden";

        @MyTest("greet check")
        public void testGreet() {
            System.out.println("   testGreet() invoked reflectively, secret=" + secret);
        }

        @NotVisible
        public void otherMethod() {}
    }

    static class Bomb {
        static int x = 1 / 0; // throws ArithmeticException during static initialization
    }

    public static void main(String[] args) throws Exception {
        Derived d = new Derived();

        // 1) getFields() (public, INCLUDING inherited) vs getDeclaredFields() (declared HERE, any modifier)
        System.out.println("1) getFields(): " + Arrays.toString(
                Arrays.stream(Derived.class.getFields()).map(Field::getName).toArray()));
        System.out.println("2) getDeclaredFields(): " + Arrays.toString(
                Arrays.stream(Derived.class.getDeclaredFields()).map(Field::getName).toArray()));

        // 2) setAccessible(true) to read/write a private field from outside the class.
        Field secretField = Derived.class.getDeclaredField("secret");
        secretField.setAccessible(true);
        System.out.println("3) private field value via reflection: " + secretField.get(d));
        secretField.set(d, "modified via reflection");
        System.out.println("4) after reflective set: " + secretField.get(d));

        // 3) A RUNTIME-retention annotation IS visible and readable via reflection.
        Method testMethod = Derived.class.getDeclaredMethod("testGreet");
        System.out.println("5) testGreet has @MyTest: " + testMethod.isAnnotationPresent(MyTest.class));
        MyTest annotation = testMethod.getAnnotation(MyTest.class);
        System.out.println("6) @MyTest value: " + annotation.value());
        testMethod.invoke(d); // mechanically, exactly what a test runner does

        // 4) A default-retention (CLASS) annotation is INVISIBLE to reflection at runtime.
        Method otherMethod = Derived.class.getDeclaredMethod("otherMethod");
        System.out.println("7) otherMethod has @NotVisible (at runtime): "
                + otherMethod.isAnnotationPresent(NotVisible.class));

        // 5) ExceptionInInitializerError (first failure) vs NoClassDefFoundError (every reference after).
        try {
            new Bomb();
        } catch (ExceptionInInitializerError e) {
            System.out.println("8) first reference threw: " + e.getClass().getSimpleName()
                    + ", caused by " + e.getCause());
        }
        try {
            new Bomb();
        } catch (NoClassDefFoundError e) {
            System.out.println("9) second reference threw: " + e.getClass().getSimpleName() + ": " + e.getMessage());
        }
    }
}
