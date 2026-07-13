import java.util.ArrayList;
import java.util.List;

// Run with a deliberately small heap to trigger this quickly and deterministically, e.g.:
//   java -Xmx32m HeapOOMDemo
public class HeapOOMDemo {

    public static void main(String[] args) {
        List<byte[]> holder = new ArrayList<>(); // keeps every chunk reachable -- nothing is ever collectible
        long chunksAllocated = 0;
        try {
            while (true) {
                holder.add(new byte[1_000_000]); // ~1 MB per chunk
                chunksAllocated++;
            }
        } catch (OutOfMemoryError e) {
            long allocatedBeforeFailure = chunksAllocated;
            holder.clear();  // release the held memory FIRST -- otherwise even this catch block's own
            holder = null;   // String allocations for the log message below can throw a SECOND OOM
            System.out.println("1) OutOfMemoryError caught after allocating approximately "
                    + allocatedBeforeFailure + " MB");
            System.out.println("2) error message: " + e.getMessage());
        }
    }
}
