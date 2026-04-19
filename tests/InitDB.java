import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.sql.SQLException;

public class InitDB {
    public static void main(String[] args) {
        String url = "jdbc:mysql://localhost:3306/?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
        String user = "root";
        String pass = "1507";
        
        System.out.println("Attempting to connect to MySQL...");
        try (Connection conn = DriverManager.getConnection(url, user, pass)) {
            System.out.println("Connected successfully as user: '" + user + "' with password.");
            executeSchema(conn);
        } catch (SQLException e1) {
            System.out.println("Connection failed with password. Trying without password...");
            try (Connection conn = DriverManager.getConnection(url, user, "")) {
                System.out.println("Connected successfully as user: '" + user + "' without password.");
                executeSchema(conn);
            } catch (SQLException e2) {
                System.err.println("Fatal: Could not connect to MySQL server. Is it running on port 3306?");
                e2.printStackTrace();
            }
        }
    }

    private static void executeSchema(Connection conn) {
        try {
            System.out.println("Reading schema.sql...");
            String schema = new String(Files.readAllBytes(Paths.get("db/schema.sql")));
            String[] commands = schema.split(";");
            
            try (Statement stmt = conn.createStatement()) {
                for (String command : commands) {
                    if (command.trim().isEmpty()) continue;
                    stmt.execute(command);
                }
                System.out.println("Database and schema created successfully!");
            }
        } catch (IOException e) {
            System.err.println("Could not read db/schema.sql");
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("Error executing schema commands");
            e.printStackTrace();
        }
    }
}