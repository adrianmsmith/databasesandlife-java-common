package com.databasesandlife.util.jdbc;

import java.util.Iterator;
import java.util.List;

import com.databasesandlife.util.jdbc.DbTransaction.DbQueryResultRow;
import com.databasesandlife.util.jdbc.DbTransaction.DbQueryResultSet;
import com.databasesandlife.util.jdbc.DbTransaction.DbTransactionFactory;
import com.databasesandlife.util.jdbc.DbTransaction.SqlException;

/**
 * @see com.databasesandlife.util.jdbc.DbTransaction
 */
public class ReadOnlyReconnectingDbConnection {
    
    protected DbTransactionFactory fac;
    protected DbTransaction tx;

    public ReadOnlyReconnectingDbConnection(DbTransactionFactory fac) {
        this.fac = fac;
        this.tx = fac.newDbTransaction();
    }
    
    protected abstract class ReconnectingDbQueryResultSet extends DbQueryResultSet {
        protected abstract DbQueryResultSet query();
        
        @Override public Iterator<DbQueryResultRow> iterator() {
            try { return query().iterator(); }
            catch (SqlException e) {
                try { tx.rollbackIfConnectionStillOpen(); }
                catch (Exception e2) { }
                
                tx = fac.newDbTransaction();
                return query().iterator(); 
            }
        }
    }

    public DbQueryResultSet query(final String sql, final Object... args) {
        return new ReconnectingDbQueryResultSet() {
            @Override protected DbQueryResultSet query() {
                return tx.query(sql, args);
            }
        };
    }

    public DbQueryResultSet query(CharSequence sql, List<Object> args) {
        return query(sql.toString(), args.toArray());
    }
}
