
package util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    private static final String URL =
            "jdbc:sqlserver://localhost:1433;"
            + "databaseName=NTMCS;"
            + "integratedSecurity=true;"
            + "encrypt=true;"
            + "trustServerCertificate=true;";

    public static Connection getConnection()
            throws SQLException {

        try {
            Class.forName(
                    "com.microsoft.sqlserver.jdbc.SQLServerDriver");
        } catch (ClassNotFoundException e) {
            throw new SQLException(
                    "SQL Server JDBC driver is not available at runtime.", e);
        }

        return DriverManager.getConnection(URL);
    }
}
