package com.databasesandlife.util.jdbc;

import java.util.Iterator;
import java.util.NoSuchElementException;

import junit.framework.TestCase;

import com.databasesandlife.util.jdbc.DbTransaction.DbQueryResultRow;
import com.databasesandlife.util.jdbc.testutil.DatabaseConnection;

public class DbTransactionTest extends TestCase {
    
    public void testQuery() {
        DbTransaction tx = new DbTransaction(DatabaseConnection.getJdbcUrl());
        Iterator<DbQueryResultRow> i;
        DbQueryResultRow row;
        try {
            // no rows hasNext
            i = tx.query("SELECT 1  WHERE 1=2").iterator();
            assertFalse(i.hasNext());
            assertFalse(i.hasNext());
            
            // no rows next
            i = tx.query("SELECT 1  WHERE 1=2").iterator();
            try { i.next(); fail(); }
            catch (NoSuchElementException e) { }
            
            // 1 row hasNext
            i = tx.query("SELECT 1 AS x").iterator();
            assertTrue(i.hasNext());
            assertTrue(i.hasNext());
            
            // 1 row next
            row = i.next();
            assertEquals(1, (int) row.getInt("x"));
            assertFalse(i.hasNext());
            assertFalse(i.hasNext());
            try { i.next(); fail(); }
            catch (NoSuchElementException e) { }
            
            // 1 row hasNext then next
            i = tx.query("SELECT 1 AS x").iterator();
            assertTrue(i.hasNext());
            assertTrue(i.hasNext());
            row = i.next();
            assertEquals(1, (int) row.getInt("x"));
            assertFalse(i.hasNext());
            assertFalse(i.hasNext());
            try { i.next(); fail(); }
            catch (NoSuchElementException e) { }
        }
        finally { tx.commit(); }
    }
    
    public void testInsert() {
        DbTransaction tx = new DbTransaction(DatabaseConnection.getJdbcUrl());
        tx.execute("DROP TABLE IF EXISTS joe");
        tx.execute("CREATE TABLE joe (id SERIAL, num INTEGER)");
        long id = tx.insert("INSERT INTO joe (num) VALUES (?)", 4);
        assertTrue(id == 1);
    }

}
