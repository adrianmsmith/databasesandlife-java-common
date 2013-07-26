package com.databasesandlife.util.jdbc.testutil;

import com.databasesandlife.util.jdbc.DbTransaction;

/**
 * @author This source is copyright <a href="http://www.databasesandlife.com">Adrian Smith</a> and licensed under the LGPL 3.
 * @version $Revision$
 */
public class DatabaseConnection {

    public static DbTransaction[] newDbTransactions() {
        return new DbTransaction[] {
            new DbTransaction("jdbc:mysql://localhost/databasesandlife_common?user=root&password=root"),
            new DbTransaction("jdbc:postgresql://localhost:5433/databasesandlife_common?user=postgres&password=piyrwqetuo"),
            new DbTransaction("jdbc:sqlserver://localhost;database=databasesandlife_common;user=adrianx;password=adrian"),
        };
    }
}
