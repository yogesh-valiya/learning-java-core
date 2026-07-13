import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;

public class IOSerializationDemo {

    static class Session implements Serializable {
        private static final long serialVersionUID = 1L;
        String username;
        transient String sessionToken; // must NOT survive serialization

        Session(String username, String sessionToken) {
            this.username = username;
            this.sessionToken = sessionToken;
        }
    }

    static class BrokenSession implements Serializable {
        private static final long serialVersionUID = 1L;
        String username;
        Thread workerThread; // NOT Serializable, NOT marked transient -- a ticking bomb

        BrokenSession(String username) {
            this.username = username;
            this.workerThread = new Thread(() -> {});
        }
    }

    public static void main(String[] args) throws Exception {
        Path path = Files.createTempFile("module27demo", ".txt");
        try {
            // 1) NIO.2 -- the modern one-liner.
            Files.writeString(path, "hello via NIO.2");
            System.out.println("1) NIO.2 read: " + Files.readString(path));

            // 2) Classic decorator chain -- same file, old-school API.
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(new FileInputStream(path.toFile()), StandardCharsets.UTF_8))) {
                System.out.println("2) decorator-chain read: " + reader.readLine());
            }

            // 3) transient field defaults after deserialization.
            Session original = new Session("ravi", "super-secret-token");
            byte[] bytes;
            try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
                 ObjectOutputStream oos = new ObjectOutputStream(bos)) {
                oos.writeObject(original);
                bytes = bos.toByteArray();
            }
            Session restored;
            try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bytes))) {
                restored = (Session) ois.readObject();
            }
            System.out.println("3) original sessionToken: " + original.sessionToken);
            System.out.println("4) restored sessionToken (transient): " + restored.sessionToken);
            System.out.println("5) restored username (not transient): " + restored.username);

            // 4) NotSerializableException at RUNTIME, not compile time.
            BrokenSession broken = new BrokenSession("anita");
            try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
                 ObjectOutputStream oos = new ObjectOutputStream(bos)) {
                oos.writeObject(broken); // compiles fine -- fails only now, at runtime
            } catch (NotSerializableException e) {
                System.out.println("6) serializing a non-Serializable field threw: "
                        + e.getClass().getSimpleName() + ": " + e.getMessage());
            }
        } finally {
            Files.deleteIfExists(path);
        }
    }
}
