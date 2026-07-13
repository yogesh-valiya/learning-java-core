public class HashBucketDemo {

    // Mirrors HashMap's real internal spread function: static final int hash(Object key)
    static int spread(int h) {
        return h ^ (h >>> 16);
    }

    static int bucketIndex(int hash, int capacity) {
        return (capacity - 1) & hash; // works because capacity is always a power of two
    }

    public static void main(String[] args) {
        int capacity = 16;
        int[] rawHashes = { 0x00010000, 0x00020000, 0x12345678, 1, 17 };

        for (int h : rawHashes) {
            int naiveIndex = bucketIndex(h, capacity);          // if we skipped spreading
            int realIndex  = bucketIndex(spread(h), capacity);  // what HashMap actually does
            System.out.printf("hash=0x%08X  naive-bucket=%2d  spread-bucket=%2d%n", h, naiveIndex, realIndex);
        }
    }
}
