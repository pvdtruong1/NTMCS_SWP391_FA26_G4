package dal;

import java.sql.Connection;
import java.sql.DriverManager;

/**
 * Opens the connection to SQL Server.
 * Change URL / USER / PASSWORD to match YOUR machine.
 * All DAO classes extend this class and call getConnection().
 */
public class DBContext {

    private static final String URL
            = "jdbc:sqlserver://localhost:1433;databaseName=NTMCS;encrypt=false;trustServerCertificate=true";
    private static final String USER = "sa";
    private static final String PASSWORD = "123";

    public Connection getConnection() throws Exception {
        Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
