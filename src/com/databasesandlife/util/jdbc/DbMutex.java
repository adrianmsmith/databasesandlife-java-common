package com.databasesandlife.util.jdbc;

import java.util.HashMap;

/** 
 * Maintains a table full of mutexes as described
 * <a href="http://www.databasesandlife.com/mysql-lock-tables-does-an-implicit-commit/">here</a>
 *    <p>
 * To use this, create this table:
 * <pre>
 *   CREATE TABLE mutex (name VARCHAR(100)) ENGINE=InnoDB;
 * </pre> 
 *
 * @author The Java source is copyright <a href="http://www.databasesandlife.com">Adrian Smith</a> and licensed under the LGPL 3.
 * @version $Revision$
 */
public class DbMutex {
    
    public String name;
    
    public DbMutex(String name) { this.name = name; }
    
    public void acquire(DbTransaction tx) {
        tx.insertIgnoringUniqueConstraintViolations("mutex", new HashMap<String, String>() {{ put("name", name); }});
        tx.query("SELECT * FROM mutex WHERE name=? FOR UPDATE", name);
    }

}
