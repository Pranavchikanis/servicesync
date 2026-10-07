package com.servicesync.kiosk.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseUtil {

    private static final String URL_ENV = "DB_URL";
    private static final String USER_ENV = "DB_USERNAME_KSK";
    private static final String PASS_ENV = "DB_PASSWORD_KSK";

    private static final String DEFAULT_URL = "jdbc:mysql://localhost:3306/servicesync";
    private static final String DEFAULT_USER = "servicesync_kiosk";

    static {
        try {
            // Load the MySQL JDBC Driver
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("MySQL JDBC Driver not found", e);
        }
    }

    public static Connection getConnection() throws SQLException {
        String url = System.getenv(URL_ENV) != null ? System.getenv(URL_ENV) : DEFAULT_URL;
        String user = System.getenv(USER_ENV) != null ? System.getenv(USER_ENV) : DEFAULT_USER;
        String pass = System.getenv(PASS_ENV);
        
        if (pass == null) {
            throw new IllegalStateException("Missing required environment variable: " + PASS_ENV);
        }
        
        return DriverManager.getConnection(url, user, pass);
    }
}
