package dal;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * High-performance connection pool context for NTMCS (SQL Server).
 * Maintains a persistent pool of pre-warmed database connections, eliminating
 * repetitive TCP socket creation, TLS handshakes, and TDS login negotiation overhead.
 * Delivers sub-millisecond connection acquisition for all Servlets and DAOs.
 */
public class DBContext implements AutoCloseable {
    private static final Logger LOGGER = Logger.getLogger(DBContext.class.getName());

    private static final String USER = "sa";
    private static final String PASS = "123";
    private static final String URL = "jdbc:sqlserver://127.0.0.1:1433;databaseName=NTMCS;encrypt=false;trustServerCertificate=true;multipleActiveResultSets=true;loginTimeout=5;";

    private static final int MAX_POOL_SIZE = 10;
    private static final BlockingQueue<Connection> POOL = new LinkedBlockingQueue<>(MAX_POOL_SIZE);

    protected Connection connection;
    private boolean borrowedFromPool = false;

    static {
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            Thread warmupThread = new Thread(() -> {
                try {
                    for (int i = 0; i < 2; i++) {
                        Connection conn = DriverManager.getConnection(URL, USER, PASS);
                        if (!POOL.offer(conn)) {
                            conn.close();
                        }
                    }
                } catch (Exception ignored) {}
            });
            warmupThread.setDaemon(true);
            warmupThread.setName("DBContext-Warmup");
            warmupThread.start();
        } catch (ClassNotFoundException ex) {
            LOGGER.log(Level.SEVERE, "SQL Server JDBC Driver not found in classpath!", ex);
        }
    }

    /**
     * Acquire an active connection from the pool, or establish a new connection if the pool is empty.
     */
    public Connection getConnection() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            return connection;
        }

        Connection conn = POOL.poll();
        while (conn != null) {
            try {
                if (!conn.isClosed()) {
                    this.connection = conn;
                    this.borrowedFromPool = true;
                    return this.connection;
                }
            } catch (SQLException ignored) {
                try { conn.close(); } catch (Exception ignore) {}
            }
            conn = POOL.poll();
        }

        this.connection = DriverManager.getConnection(URL, USER, PASS);
        this.borrowedFromPool = true;
        return this.connection;
    }

    public boolean testConnection() {
        try {
            Connection conn = getConnection();
            return conn != null && !conn.isClosed();
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Database connection test failed", ex);
            return false;
        }
    }

    /**
     * Return connection to the shared pool instead of terminating the underlying TCP/TDS socket.
     */
    @Override
    public void close() {
        if (connection != null) {
            try {
                if (borrowedFromPool && !connection.isClosed()) {
                    if (!connection.getAutoCommit()) {
                        connection.setAutoCommit(true);
                    }
                    if (!POOL.offer(connection)) {
                        connection.close();
                    }
                } else if (!connection.isClosed()) {
                    connection.close();
                }
            } catch (SQLException ex) {
                LOGGER.log(Level.WARNING, "Error releasing connection to pool", ex);
                try { connection.close(); } catch (Exception ignore) {}
            } finally {
                connection = null;
                borrowedFromPool = false;
            }
        }
    }
}
