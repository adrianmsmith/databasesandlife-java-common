package com.databasesandlife.util.jdbc;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;

import junit.framework.TestCase;

import com.databasesandlife.util.YearMonthDay;
import com.databasesandlife.util.jdbc.DbTransaction.DbQueryResultRow;
import com.databasesandlife.util.jdbc.DbTransaction.DbServerProduct;
import com.databasesandlife.util.jdbc.testutil.DatabaseConnection;

/**
 * @author This source is copyright <a href="http://www.databasesandlife.com">Adrian Smith</a> and licensed under the LGPL 3.
 * @see <a href="https://github.com/adrianmsmith/databasesandlife-java-common">Project on GitHub</a>
 */
@SuppressWarnings("deprecation")
public class DbTransactionTest extends TestCase {
    
    public void testQuery() {
        for (DbTransaction tx : DatabaseConnection.newDbTransactions()) {
            try {
                Iterator<DbQueryResultRow> i;
                DbQueryResultRow row;
                
                String fromDual = tx.getFromDual();
                
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
        for (DbTransaction tx : DatabaseConnection.newDbTransactions()) {
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
    
    protected void assertDateCorrect(DbTransaction tx) {
        assertEquals(""+tx.product, new YearMonthDay(2015, 1, 30), tx.query("SELECT d FROM x").iterator().next().getYearMonthDay("d"));
        assertEquals(""+tx.product, new org.joda.time.LocalDate(2015, 1, 30), tx.query("SELECT d FROM x").iterator().next().getJodatimeLocalDate("d"));
        assertEquals(""+tx.product, java.time.LocalDate.of(2015, 1, 30), tx.query("SELECT d FROM x").iterator().next().getLocalDate("d"));
    }
    
    protected void assertTimeCorrect(DbTransaction tx) {
        assertEquals(""+tx.product, new org.joda.time.LocalTime(18, 19), tx.query("SELECT t FROM x").iterator().next().getJodatimeLocalTime("t"));
        assertEquals(""+tx.product, java.time.LocalTime.of(18, 19), tx.query("SELECT t FROM x").iterator().next().getLocalTime("t"));
    }
    
    public void testDateAndTime() {
        for (DbTransaction tx : DatabaseConnection.newDbTransactions()) {
            try {
                tx.execute("DROP TABLE IF EXISTS x");
                tx.execute("CREATE TABLE x (d DATE, t TIME)");
                
                tx.execute("DELETE FROM x");
                tx.execute("INSERT INTO x VALUES ('2015-01-30', '18:19')");
                assertDateCorrect(tx);
                assertTimeCorrect(tx);
                
                tx.execute("DELETE FROM x");
                tx.execute("INSERT INTO x VALUES (?, NULL)", new YearMonthDay(2015, 1, 30));
                assertDateCorrect(tx);
                
                tx.execute("DELETE FROM x");
                tx.execute("INSERT INTO x VALUES (?, ?)", new org.joda.time.LocalDate(2015, 1, 30), new org.joda.time.LocalTime(18, 19));
                assertDateCorrect(tx);
                assertTimeCorrect(tx);
                
                tx.execute("DELETE FROM x");
                tx.execute("INSERT INTO x VALUES (?, ?)", java.time.LocalDate.of(2015, 1, 30), java.time.LocalTime.of(18, 19));
                assertDateCorrect(tx);
                assertTimeCorrect(tx);
            }
            finally { tx.rollback(); }
        }
    }
}
