package test;

import java.sql.Connection;
import java.sql.DriverManager;

public class BenchmarkDB {
    public static void main(String[] args) throws Exception {
        Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        
        // Test with 127.0.0.1 (pure IPv4, bypasses IPv6 DNS resolution)
        String urlIpv4 = "jdbc:sqlserver://127.0.0.1:1433;databaseName=NTMCS;encrypt=true;trustServerCertificate=true;";

        long start = System.currentTimeMillis();
        try (Connection c = DriverManager.getConnection(urlIpv4, "sa", "123")) {
            c.getMetaData();
        }
        long t1 = System.currentTimeMillis() - start;
        System.out.println("Connection with 127.0.0.1 (IPv4): " + t1 + " ms");

        // Test with localhost
        String urlLocalhost = "jdbc:sqlserver://localhost:1433;databaseName=NTMCS;encrypt=true;trustServerCertificate=true;";
        start = System.currentTimeMillis();
        try (Connection c = DriverManager.getConnection(urlLocalhost, "sa", "123")) {
            c.getMetaData();
        }
        long t2 = System.currentTimeMillis() - start;
        System.out.println("Connection with localhost: " + t2 + " ms");
    }
}
