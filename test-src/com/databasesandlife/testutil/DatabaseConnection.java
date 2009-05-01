package com.databasesandlife.testutil;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * @author Adrian Smith
 */
public class DatabaseConnection {
    
    public static String getHostname() { return "localhost"; }
    public static String getDatabaseName() { return "databasesandlife_common"; }
    public static String getUsername() { return "root"; }
    public static String getPassword() { return "root"; }

    public static String getJdbcUrl() {
        return "jdbc:mysql://" + getHostname() + "/" + getDatabaseName() + "?user=" + getUsername() +
                "&password=" + getPassword() + "&useUnicode=true&characterEncoding=UTF-8";
    }

    public static Connection getConnection() {
        try {
            new com.mysql.jdbc.Driver();
            return DriverManager.getConnection(getJdbcUrl());
        }
        catch (SQLException e) { throw new RuntimeException(e); }
    }

}
