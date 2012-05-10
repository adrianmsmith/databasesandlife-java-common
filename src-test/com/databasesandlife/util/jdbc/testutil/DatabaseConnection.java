package com.databasesandlife.util.jdbc.testutil;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import com.databasesandlife.util.jdbc.DbTransaction;

/**
 * @author This source is copyright <a href="http://www.databasesandlife.com">Adrian Smith</a> and licensed under the LGPL 3.
 * @version $Revision$
 */
public class DatabaseConnection {

    public static String getHostname()      { return System.getProperty("db.hostname", "localhost"); }
    public static String getDatabaseName()  { return System.getProperty("db.name", "databasesandlife_common"); }
    public static String getUsername()      { return System.getProperty("db.username", "root"); }
    public static String getPassword()      { return System.getProperty("db.password", ""); }

    public static String getJdbcUrl() {
        return "jdbc:postgresql://" + getHostname() + "/" + getDatabaseName() + 
            "?user=" + getUsername() + "&password=" + getPassword();
    }

    public static DbTransaction newDbTransaction() {
        return new DbTransaction(getJdbcUrl());
    }
}
