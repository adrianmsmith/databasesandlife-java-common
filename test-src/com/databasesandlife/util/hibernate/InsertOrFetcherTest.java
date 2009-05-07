package com.databasesandlife.util.hibernate;

import com.databasesandlife.testutil.DatabaseConnection;
import com.databasesandlife.util.hibernate.testutil.HibernateSessionFactory;
import com.databasesandlife.util.hibernate.testutil.PersistentObject;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import junit.framework.TestCase;
import org.hibernate.Session;

public class InsertOrFetcherTest extends TestCase {
    
    public InsertOrFetcherTest(String testName) {
        super(testName);
    }

    protected int count(Connection connection) throws SQLException {
        ResultSet s = connection.prepareStatement("SELECT COUNT(*) AS c FROM persistent_object").executeQuery();
        assertTrue(s.next());
        return s.getInt("c");
    }

    public void testLoad() throws Exception {
        Connection connection = DatabaseConnection.getConnection();
        connection.prepareStatement("DROP TABLE IF EXISTS persistent_object").execute();
        connection.prepareStatement("CREATE TABLE persistent_object(" +
                "id INT PRIMARY KEY AUTO_INCREMENT, key1 VARCHAR(20), key2 VARCHAR(20), data VARCHAR(20)," +
                "CONSTRAINT u UNIQUE (key1, key2))").execute();
        assertEquals(0, count(connection));

        Session s = HibernateSessionFactory.getSessionFactory().openSession();

        PersistentObject prototype = new PersistentObject("foo", "bar");
        Collection<String> key = Arrays.asList("key1", "key2");

        PersistentObject obj = InsertOrFetcher.load(PersistentObject.class, s, prototype, key);
        assertNotNull(obj);
        assertEquals(1, count(connection)); // really exists in database

        PersistentObject objSame = InsertOrFetcher.load(PersistentObject.class, s, prototype, key);
        assertNotNull(objSame);
        assertEquals(1, count(connection)); // didn't do another INSERT
        assertSame(obj, objSame);   // returned same object instance

        prototype.setKey2("different");
        PersistentObject objDifferent = InsertOrFetcher.load(PersistentObject.class, s, prototype, key);
        assertNotNull(objDifferent);
        assertEquals(2, count(connection)); // did do another INSERT
        assertNotSame(obj, objDifferent);  // return different object
        assertNotSame(obj.getId(), objDifferent.getId());  // return different object
    }
}
