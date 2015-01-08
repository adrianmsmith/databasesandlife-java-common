package com.databasesandlife.util.jdbc;

import java.sql.PreparedStatement;

import junit.framework.TestCase;

import com.databasesandlife.util.jdbc.DbTransaction.DbTransactionFactory;
import com.databasesandlife.util.jdbc.DbTransaction.SqlException;
import com.databasesandlife.util.jdbc.testutil.DatabaseConnection;

public class ReadOnlyReconnectingDbConnectionTest extends TestCase {
    
    public void testReconnect() {
        DbTransactionFactory fac = new DbTransactionFactory() {
            @Override public DbTransaction newDbTransaction() {
                return new DbTransaction(DatabaseConnection.mysql) {
                    int count = 0;
                    @Override protected PreparedStatement insertParamsToPreparedStatement( String sql, Object... args) {
                        if (++count % 2 == 0) throw new SqlException("test fail");
                        else return super.insertParamsToPreparedStatement(sql, args);
                    }
                };
            }
        };

        // Test that failing tx actually fails
        boolean exceptionThrown = false;
        DbTransaction failingTx = fac.newDbTransaction();
        for (int i = 0; i < 10; i++) 
            try { failingTx.query("SELECT 1 AS one").iterator(); }
            catch (SqlException e) { exceptionThrown = true; }
        assertTrue(exceptionThrown);
        
        // Test that reconnecting tx handles it
        ReadOnlyReconnectingDbConnection c = new ReadOnlyReconnectingDbConnection(fac);
        for (int i = 0; i < 10; i++) 
            assertEquals(1, (int)c.query("SELECT 1 AS one").iterator().next().getInt("one")); // won't throw
    }
}
