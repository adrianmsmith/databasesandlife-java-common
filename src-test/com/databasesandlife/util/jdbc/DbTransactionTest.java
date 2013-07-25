package com.databasesandlife.util.jdbc;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;

import junit.framework.TestCase;

import com.databasesandlife.util.jdbc.DbTransaction.DbQueryResultRow;
import com.databasesandlife.util.jdbc.DbTransaction.DbServerProduct;
import com.databasesandlife.util.jdbc.testutil.DatabaseConnection;

public class DbTransactionTest extends TestCase {
    
    public void testQuery() {
        for (DbTransaction tx : DatabaseConnection.newDbTransactions()) {
            try {
                Iterator<DbQueryResultRow> i;
                DbQueryResultRow row;
                
                String fromDual = tx.product == DbServerProduct.postgres ? "" : " FROM dual ";
                
                // no rows hasNext
                i = tx.query("SELECT 1 "+fromDual+" WHERE 1=2").iterator();
                assertFalse(i.hasNext());
                assertFalse(i.hasNext());
                
                // no rows next
                i = tx.query("SELECT 1 "+fromDual+" WHERE 1=2").iterator();
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
    }
    
    enum Choice { a,b };
    public void testEnumArray() {
        for (DbTransaction tx : DatabaseConnection.newDbTransactions())
            try {
                if (tx.product != DbServerProduct.postgres) continue;
                
                tx.addPostgresTypeForEnum(Choice.class, "choice");
                
                tx.execute("DROP TYPE IF EXISTS choice");
                tx.execute("CREATE TYPE choice AS ENUM ('a', 'b')");
    
                tx.execute("DROP TABLE IF EXISTS choice_table");
                tx.execute("CREATE TABLE choice_table (choice_list choice[] NOT NULL)");
                
                Map<String, Object> values = new HashMap<String, Object>();
                values.put("choice_list", new Choice[] { Choice.a });
                tx.insert("choice_table", values);
                
                Iterator<DbQueryResultRow> rows = tx.query("SELECT choice_list::varchar[] FROM choice_table").iterator();
                assertTrue(rows.hasNext());
                DbQueryResultRow r = rows.next();
                Choice[] choices = r.getEnumArray("choice_list", Choice.class);
                assertEquals(1, choices.length);
                assertEquals(Choice.a, choices[0]);
            }
            finally { tx.rollback(); }
    }
}
