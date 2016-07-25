package com.databasesandlife.util.jdbc.testutil;

import com.databasesandlife.util.jdbc.DbTransaction;

/**
 * @author This source is copyright <a href="http://www.databasesandlife.com">Adrian Smith</a> and licensed under the LGPL 3.
 */
public class DatabaseConnection {

    public static final String mysql = "jdbc:mysql://localhost/databasesandlife_common?user=root&password=root";
    public static final String postgresql = "jdbc:postgresql://localhost/databasesandlife_common?user=postgres&password=postgres";
//    public static final String sqlserver = "jdbc:sqlserver://localhost;database=databasesandlife_common;user=adrianx;password=adrian";

    public static DbTransaction[] newDbTransactions() {
        return new DbTransaction[] {
            new DbTransaction(mysql),
            new DbTransaction(postgresql),
//            new DbTransaction(sqlserver),
        };
    }
}
