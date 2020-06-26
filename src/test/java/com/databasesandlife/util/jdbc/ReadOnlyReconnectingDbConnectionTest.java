package com.databasesandlife.util.jdbc;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import com.databasesandlife.util.jdbc.DbTransaction.DbTransactionFactory;
import com.databasesandlife.util.jdbc.DbTransaction.SqlException;
import com.databasesandlife.util.jdbc.testutil.DatabaseConnection;

/**
 * @author This source is copyright <a href="http://www.databasesandlife.com">Adrian Smith</a> and licensed under the LGPL 3.
 * @see <a href="https://github.com/adrianmsmith/databasesandlife-java-common">Project on GitHub</a>
 */
public class ReadOnlyReconnectingDbConnectionTest extends TestCase {
    
    protected void testConcurrency(DbTransactionFactory fac) throws Exception {
        final ReadOnlyReconnectingDbConnection c = new ReadOnlyReconnectingDbConnection(fac);

        class Runner extends Thread {
            public Exception ex = null;
            public void run() {
                try { for (int i = 0; i < 100; i++) c.query("SELECT 1 AS one").iterator().next().getInt("one"); }
                catch (Exception e) { ex = e; } 
            }
        }
        
        List<Runner> threads = new ArrayList<>();
        for (int i = 0; i < Runtime.getRuntime().availableProcessors(); i++) threads.add(new Runner());
        for (Runner t : threads) t.start();
        for (Runner t : threads) try { t.join(); } catch (InterruptedException e) { throw new RuntimeException(e); }
        for (Runner t : threads) if (t.ex != null) throw t.ex;
    }
    
    public void testReconnect() throws Exception {
        DbTransactionFactory fac = new DbTransactionFactory() {
            @Override public DbTransaction newDbTransaction() {
                return new DbTransaction(DatabaseConnection.postgresql) {
                    int count = 0;
                    @Override protected PreparedStatement insertParamsToPreparedStatement( String sql, Object... args) {
                        try {
                            if (++count % 2 == 0) throw new SqlException("test fail");
                            else return super.insertParamsToPreparedStatement(sql, args);
                        } 
                        catch (SQLException e) { throw new RuntimeException(e); }
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
        
        // Test that threading behaviour works (with a db factory that fails)
        testConcurrency(fac);
        
        // Test that threading behaviour works (with a db factory that doesn't fail)
        testConcurrency(new DbTransactionFactory() {
            @Override public DbTransaction newDbTransaction() {
                return new DbTransaction(DatabaseConnection.postgresql);
            }
        });
    }
}
