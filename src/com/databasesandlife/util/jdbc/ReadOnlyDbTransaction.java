package com.databasesandlife.util.jdbc;

import javax.mail.MethodNotSupportedException;
import java.sql.SQLException;
import java.util.Map;

/**
 * {@inheritDoc}
 *
 * It is assumed that this DbTransaction
 *
 * NOTE: This class has no writing capabilities i.e. update, insert and delete are not allowed.
 * <br>
 * Doing so will crash your application.
 * @see com.databasesandlife.util.jdbc.DbTransaction
 */
public class ReadOnlyDbTransaction extends DbTransaction {

    public ReadOnlyDbTransaction(String jdbcUrl) {
        super(jdbcUrl);
        try {
            connection.setReadOnly(true);
        } catch (SQLException e) {
            throw new RuntimeException("cannot connect to database '"+jdbcUrl+"': JBDC driver is OK, "+
                        "connection is NOT OK: "+e.getMessage(), e);
        }
    }

    @Override public void commit() { }
    @Override public void rollback() { }
    @Override public void rollbackIfConnectionStillOpen() { }
}
