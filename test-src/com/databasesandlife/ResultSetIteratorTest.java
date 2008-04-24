package com.databasesandlife;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.NoSuchElementException;
import junit.framework.TestCase;

/**
 * @author Adrian Smith &lt;adrian.m.smith@gmail.com&gt;
 */
public class ResultSetIteratorTest extends TestCase {
    
    Connection connection = null;
    
    public class I extends ResultSetIterator<Integer> {
        public I(PreparedStatement s) { super(s, true); }
        @Override protected Integer newObjectForRow(ResultSet r) throws SQLException {
            return r.getInt("intCol");
        }
    }
    
    public ResultSetIteratorTest(String testName) {
        super(testName);
    }            
    
    @Override
    public void setUp() throws Exception {
        new com.mysql.jdbc.Driver();
        String conUrl = "jdbc:mysql://localhost/tender_lucene_data?user=root"+
                "&password=piyrwqetuo&useUnicode=true&characterEncoding=UTF-8";
        connection = DriverManager.getConnection(conUrl);
    }
    
    @Override
    public void tearDown() throws Exception {
        if (connection != null)
            connection.prepareStatement("DROP TABLE IF EXISTS ResultSetIteratorTest").execute();
    }
    
    public void test() throws Exception {
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
