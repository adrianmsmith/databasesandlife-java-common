package com.databasesandlife.util.jdbc;

import com.databasesandlife.util.jdbc.ResultSetIterator;
import com.databasesandlife.util.jdbc.testutil.DatabaseConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.NoSuchElementException;
import junit.framework.TestCase;

/**
 * @author This source is copyright <a href="http://www.databasesandlife.com">Adrian Smith</a> and licensed under the LGPL 3.
 * @version $Revision$
 */
public class ResultSetIteratorTest extends TestCase {
    
    public class I extends ResultSetIterator<Integer> {
        public I(PreparedStatement s) { super("", s, CloseStrategy.CLOSE_STATEMENT); }
        @Override protected Integer newObjectForRow(ResultSet r) throws SQLException {
            return r.getInt("intCol");
        }
    }
    
    public ResultSetIteratorTest(String testName) {
        super(testName);
    }
    
    public void test() throws Exception {
        for (DbTransaction tx : DatabaseConnection.newDbTransactions()) {
            Connection connection = tx.getConnection();
            
            connection.prepareStatement("DROP TABLE IF EXISTS ResultSetIteratorTest").execute();
            connection.prepareStatement("CREATE TABLE ResultSetIteratorTest(intCol INTEGER)").execute();
            
            // no results
            PreparedStatement pNoResults = connection.prepareStatement("SELECT * FROM ResultSetIteratorTest");
            assertFalse(pNoResults.isClosed());
            I iNoResults = new I(pNoResults);
            assertFalse(iNoResults.hasNext());
            boolean exceptionThrown = false;
            try { iNoResults.next(); }
            catch (NoSuchElementException e) { exceptionThrown = true; }
            assertTrue(exceptionThrown);
            assertTrue(pNoResults.isClosed());
            
            // 1 result
            connection.prepareStatement("INSERT INTO ResultSetIteratorTest VALUES(9)").execute();
            PreparedStatement pResults = connection.prepareStatement("SELECT * FROM ResultSetIteratorTest");
            I iResults = new I(pResults);
            assertFalse(pResults.isClosed());
            assertTrue(iResults.hasNext());
            assertEquals(9, (int) iResults.next());
            assertFalse(iResults.hasNext());
            exceptionThrown = false;
            try { iResults.next(); }
            catch (NoSuchElementException e) { exceptionThrown = true; }
            assertTrue(exceptionThrown);
            assertTrue(pResults.isClosed());
        }
    }
}
