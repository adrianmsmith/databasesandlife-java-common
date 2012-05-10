package com.databasesandlife.util.hibernate;

import com.databasesandlife.util.jdbc.DbTransaction;
import com.databasesandlife.util.jdbc.testutil.DatabaseConnection;
import com.databasesandlife.util.hibernate.testutil.HibernateSessionFactory;
import com.databasesandlife.util.hibernate.testutil.PersistentObject;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import junit.framework.TestCase;
import org.hibernate.Session;

/**
 * @author This source is copyright <a href="http://www.databasesandlife.com">Adrian Smith</a> and licensed under the LGPL 3.
 * @version $Revision$
 */
public class InsertOrFetcherTest extends TestCase {
    
    public InsertOrFetcherTest(String testName) {
        super(testName);
    }

    public void testLoad() throws Exception {
        DbTransaction tx = DatabaseConnection.newDbTransaction();
        tx.execute("DROP TABLE IF EXISTS persistent_object");
        tx.execute("CREATE TABLE persistent_object(" +
                "id INT PRIMARY KEY AUTO_INCREMENT, key1 VARCHAR(100), key2 VARCHAR(100), data VARCHAR(20)," +
                "CONSTRAINT u UNIQUE (key1, key2)) ENGINE=InnoDB");
        assertEquals(0, (int) tx.query("SELECT COUNT(*) AS c FROM persistent_object").iterator().next().getInt("c"));

        Session s = HibernateSessionFactory.getSessionFactory().openSession();

        PersistentObject prototype = new PersistentObject("jkdfjkfgjkfgfggd", "dfjjjjfgjgjfg");
        Collection<String> key = Arrays.asList("key1", "key2");

        PersistentObject obj = InsertOrFetcher.load(PersistentObject.class, s, prototype, key);
        assertNotNull(obj);
        assertEquals(1, (int) tx.query("SELECT COUNT(*) AS c FROM persistent_object").iterator().next().getInt("c")); // really exists in database

        PersistentObject objSame = InsertOrFetcher.load(PersistentObject.class, s, prototype, key);
        assertNotNull(objSame);
        assertEquals(1, (int) tx.query("SELECT COUNT(*) AS c FROM persistent_object").iterator().next().getInt("c")); // didn't do another INSERT
        assertSame(obj, objSame);   // returned same object instance

        prototype.setKey2("different");
        PersistentObject objDifferent = InsertOrFetcher.load(PersistentObject.class, s, prototype, key);
        assertNotNull(objDifferent);
        assertEquals(2, (int) tx.query("SELECT COUNT(*) AS c FROM persistent_object").iterator().next().getInt("c")); // did do another INSERT
        assertNotSame(obj, objDifferent);  // return different object
        assertNotSame(obj.getId(), objDifferent.getId());  // return different object
    }
}
