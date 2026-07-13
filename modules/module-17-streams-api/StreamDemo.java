import java.util.*;
import java.util.stream.*;

public class StreamDemo {

    record Employee(String name, String dept, int salary) {}

    public static void main(String[] args) {
        // 1) Laziness + short-circuiting: a pipeline processes ONE element at a time,
        // vertically through every stage, and stops the instant a terminal op is
        // satisfied -- NOT "filter over everything, then map over everything."
        List<Integer> nums = List.of(1, 2, 3, 4, 5, 6, 7, 8);
        Optional<Integer> firstResult = nums.stream()
                .peek(n -> System.out.println("   peek saw: " + n))
                .filter(n -> n % 2 == 0)
                .map(n -> n * 10)
                .findFirst();
        System.out.println("1) findFirst result: " + firstResult.get());

        // 2) flatMap: Stream<List<Integer>> -> Stream<Integer> (1-to-many, flattened)
        List<List<Integer>> nested = List.of(List.of(1, 2), List.of(3, 4), List.of(5));
        List<Integer> flat = nested.stream().flatMap(List::stream).collect(Collectors.toList());
        System.out.println("2) flatMap flattened: " + flat);

        // 3) Collectors: groupingBy + downstream, partitioningBy, joining
        List<Employee> employees = List.of(
                new Employee("Ravi", "Eng", 90000),
                new Employee("Anita", "Eng", 95000),
                new Employee("Vijay", "Sales", 60000)
        );

        Map<String, List<String>> byDept = employees.stream()
                .collect(Collectors.groupingBy(Employee::dept,
                        Collectors.mapping(Employee::name, Collectors.toList())));
        System.out.println("3) grouped by dept: " + byDept);

        Map<String, Long> countByDept = employees.stream()
                .collect(Collectors.groupingBy(Employee::dept, Collectors.counting()));
        System.out.println("4) count by dept: " + countByDept);

        Map<Boolean, List<String>> highEarners = employees.stream()
                .collect(Collectors.partitioningBy(e -> e.salary() > 80000,
                        Collectors.mapping(Employee::name, Collectors.toList())));
        System.out.println("5) partitioned by salary>80000: " + highEarners);

        String names = employees.stream().map(Employee::name).collect(Collectors.joining(", "));
        System.out.println("6) joined names: " + names);

        // 4) reduce: identity + accumulator
        int totalSalary = employees.stream().reduce(0, (sum, e) -> sum + e.salary(), Integer::sum);
        System.out.println("7) total salary via reduce: " + totalSalary);

        // 5) A stream can only be consumed ONCE.
        Stream<Integer> once = Stream.of(1, 2, 3);
        once.forEach(n -> {});
        try {
            once.count(); // reusing an already-consumed stream
        } catch (IllegalStateException e) {
            System.out.println("8) reusing a consumed stream threw: " + e.getClass().getSimpleName());
        }
    }
}
