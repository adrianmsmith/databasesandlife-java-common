package com.databasesandlife.util.jdbc;

import java.util.Iterator;
import java.util.List;

import com.databasesandlife.util.jdbc.DbTransaction.DbQueryResultRow;
import com.databasesandlife.util.jdbc.DbTransaction.DbQueryResultSet;
import com.databasesandlife.util.jdbc.DbTransaction.DbTransactionFactory;
import com.databasesandlife.util.jdbc.DbTransaction.SqlException;

/**
 * Read-only access to a database, which re-connects to the database if the connection is lost.
 * 
 * <p>Is thread-safe, you can use this from multiple threads</p>
 * 
 * <p>It is assumed that the results of one query are read before the next one begins.
 * Starting a new query can close the old database connection when reconnecting, meaning reads from previous queries
 * might not work.</p>
 * 
 * @see com.databasesandlife.util.jdbc.DbTransaction
 * @author This source is copyright <a href="http://www.databasesandlife.com">Adrian Smith</a> and licensed under the LGPL 3.
 * @see <a href="https://github.com/adrianmsmith/databasesandlife-java-common">Project on GitHub</a>
 */
public class ReadOnlyReconnectingDbConnection implements DbQueryable {
    
    protected DbTransactionFactory fac;
    protected ThreadLocal<DbTransaction> tx = new ThreadLocal<>();

    public ReadOnlyReconnectingDbConnection(DbTransactionFactory fac) {
        this.fac = fac;
    }
    
    protected abstract class ReconnectingDbQueryResultSet extends DbQueryResultSet {
        protected abstract DbQueryResultSet query();
        
        @Override public Iterator<DbQueryResultRow> iterator() {
            if (tx.get() != null) {
                try { return query().iterator(); }
                catch (SqlException e) { 
                    try { tx.get().rollbackIfConnectionStillOpen(); }
                    catch (Exception e2) { }
                }
            }
            
            tx.set(fac.newDbTransaction());
            return query().iterator(); 
        }
    }

    public DbQueryResultSet query(final String sql, final Object... args) {
        return new ReconnectingDbQueryResultSet() {
            @Override protected DbQueryResultSet query() {
                return tx.get().query(sql, args);
            }
        };
    }

    public DbQueryResultSet query(CharSequence sql, List<?> args) {
        return query(sql.toString(), args.toArray());
    }
}
