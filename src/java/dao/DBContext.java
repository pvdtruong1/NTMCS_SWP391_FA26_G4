
package dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import model.Topic;

/**
 * Manages connections to the NTMCS SQL Server database.
 */
public class DBContext {

    private static final Logger LOGGER =
            Logger.getLogger(DBContext.class.getName());

    private static final String SERVER = "localhost";
    private static final String PORT = "1433";
    private static final String DATABASE_NAME = "NTMCS";
    private static final String USER = "sa";
    private static final String PASS = "sa";

    private static final String URL =
            "jdbc:sqlserver://" + SERVER + ":" + PORT
            + ";databaseName=" + DATABASE_NAME
            + ";encrypt=false;trustServerCertificate=true;";

    static {
        try {
            Class.forName(
                    "com.microsoft.sqlserver.jdbc.SQLServerDriver");
        } catch (ClassNotFoundException e) {
            LOGGER.log(
                    Level.SEVERE,
                    "Microsoft SQL Server JDBC Driver not found.",
                    e
            );
        }
    }

    /**
     * Creates a connection to the NTMCS database.
     *
     * @return an active database connection
     * @throws SQLException if the connection cannot be established
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASS);
    }

    /**
     * Checks whether the database connection is available.
     *
     * @return true if the connection is available, otherwise false
     */
    public static boolean isAvailable() {
        try (Connection connection = getConnection()) {
            return connection != null && !connection.isClosed();
        } catch (SQLException e) {
            LOGGER.log(
                    Level.WARNING,
                    "Cannot connect to SQL Server database NTMCS.",
                    e
            );
            return false;
        }
    }

    /**
     * Tests the database connection and retrieves reporter topics.
     *
     * @param args command-line arguments
     */
    public static void main(String[] args) {
        System.out.println("Testing connection to SQL Server...");

        try (Connection connection = getConnection()) {
            System.out.println("CONNECTED SUCCESSFULLY!");
            System.out.println("Catalog: " + connection.getCatalog());

            System.out.println(
                    "Product: "
                    + connection.getMetaData().getDatabaseProductName()
                    + " "
                    + connection.getMetaData()
                            .getDatabaseProductVersion()
            );

            TopicRepository repository = TopicRepository.getInstance();

            List<Topic> topics = repository.findByReporter(
                    "hung.reporter@newsroom.vn"
            );

            System.out.println(
                    "Total topics fetched from database: "
                    + topics.size()
            );

            for (Topic topic : topics) {
                System.out.println(
                        " - [" + topic.getStatus() + "] "
                        + topic.getTitle()
                        + " (Category: " + topic.getCategory()
                        + ", Desk: " + topic.getDesk() + ")"
                );
            }

        } catch (SQLException e) {
            LOGGER.log(
                    Level.SEVERE,
                    "Database connection or SQL operation failed.",
                    e
            );
        } catch (Exception e) {
            LOGGER.log(
                    Level.SEVERE,
                    "An unexpected error occurred while testing the database.",
                    e
            );
        }
    }
}
