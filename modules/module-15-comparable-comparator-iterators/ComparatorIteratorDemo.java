import java.util.*;

public class ComparatorIteratorDemo {

    record Person(String lastName, String firstName, int age) {}

    public static void main(String[] args) {
        List<Person> people = new ArrayList<>(List.of(
                new Person("Kumar", "Ravi", 30),
                new Person("Kumar", "Anita", 25),
                new Person("Sharma", "Vijay", 40)
        ));

        people.sort(Comparator.comparing(Person::lastName).thenComparing(Person::firstName));
        System.out.println("1) sorted by lastName, then firstName: " + people);

        // Fail-fast: removing directly from a List while for-each iterating over it.
        List<Integer> nums = new ArrayList<>(List.of(1, 2, 3, 4, 5));
        try {
            for (Integer n : nums) {
                if (n == 3) nums.remove(n); // structural modification mid-iteration
            }
        } catch (ConcurrentModificationException e) {
            System.out.println("2) direct removal during for-each threw: " + e.getClass().getSimpleName());
        }

        // The fix: Iterator.remove() (or List.removeIf in modern code).
        List<Integer> nums2 = new ArrayList<>(List.of(1, 2, 3, 4, 5));
        Iterator<Integer> it = nums2.iterator();
        while (it.hasNext()) {
            if (it.next() == 3) it.remove(); // safe -- updates the iterator's own expected state
        }
        System.out.println("3) after Iterator.remove(): " + nums2);

        // TreeSet uses compareTo EXCLUSIVELY for equality -- NOT equals()/hashCode().
        Set<Person> byAgeOnly = new TreeSet<>(Comparator.comparingInt(Person::age));
        byAgeOnly.add(new Person("A", "X", 30));
        byAgeOnly.add(new Person("B", "Y", 30)); // different person, but the SAME age
        System.out.println("4) TreeSet size with 2 different people sharing an age: " + byAgeOnly.size());
    }
}
