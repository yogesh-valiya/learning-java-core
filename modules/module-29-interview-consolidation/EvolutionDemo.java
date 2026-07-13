import java.util.*;
import java.util.stream.*;

public class EvolutionDemo {

    // Pre-Java-8 style: plain class, imperative loop.
    static class PersonOld {
        String name; int age;
        PersonOld(String name, int age) { this.name = name; this.age = age; }
        String getName() { return name; }
        int getAge() { return age; }
    }

    // Modern style: record.
    record Person(String name, int age) {}

    static String preJava8Style(List<PersonOld> people) {
        List<String> names = new ArrayList<>();
        for (PersonOld p : people) {
            if (p.getAge() >= 18) {
                names.add(p.getName());
            }
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < names.size(); i++) {
            sb.append(names.get(i));
            if (i < names.size() - 1) sb.append(", ");
        }
        return sb.toString();
    }

    static String java8StreamsStyle(List<PersonOld> people) {
        return people.stream()
                .filter(p -> p.getAge() >= 18)
                .map(PersonOld::getName)
                .collect(Collectors.joining(", "));
    }

    static String modernStyle(List<Person> people) {
        var adultNames = people.stream()
                .filter(p -> p.age() >= 18)
                .map(Person::name)
                .toList(); // Java 16+ convenience terminal op
        return String.join(", ", adultNames);
    }

    public static void main(String[] args) {
        List<PersonOld> oldPeople = List.of(
                new PersonOld("Ravi", 25), new PersonOld("Anita", 15), new PersonOld("Vijay", 40));
        List<Person> people = List.of(
                new Person("Ravi", 25), new Person("Anita", 15), new Person("Vijay", 40));

        System.out.println("1) pre-Java-8 imperative:  " + preJava8Style(oldPeople));
        System.out.println("2) Java 8 streams:         " + java8StreamsStyle(oldPeople));
        System.out.println("3) modern (var + records): " + modernStyle(people));
    }
}
