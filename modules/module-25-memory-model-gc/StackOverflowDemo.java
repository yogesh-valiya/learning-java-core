public class StackOverflowDemo {

    static long depth = 0;

    static void recurse() {
        depth++;
        recurse(); // no base case -- exhausts the stack, not the heap
    }

    public static void main(String[] args) {
        try {
            recurse();
        } catch (StackOverflowError e) {
            System.out.println("1) StackOverflowError caught after approximately " + depth + " recursive calls");
        }
    }
}
