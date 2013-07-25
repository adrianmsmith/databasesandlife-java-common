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
        };
    }
}
