package com.databasesandlife.util.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Takes a {@link PreparedStatement} and creates a {@link ResultSet} and
 * implements the {@link Iterator} interface that returns the results.
 * <p>
 * Subclasses may be created which convert a particular row into a particular type of object
 * to be returned from the iterator.
 * <p>
 * It is intended for those ResultSets which can only iterate forward (i.e. streaming results).
 * On these ResultSets the <code>aResultSet.hasNext</code>
 * method cannot be called.
 * Therefore the ResultSet only has a <code>next</code> method,
 * but the Iterator must provide a <code>hasNext</code>
 * method which can be called an arbitrary number of times to see if there would be a next row.
 * The algorithm employed is to always fetch one row ahead store it in the object, so <code>hasNext</code>
 * can return if there is a next row or not. The <code>next</code> method then returns that previously fetched one, and
 * fetches the next one.
 * <p>
 * Any {@link SQLException} which are thrown from the ResultSet are assumed to be "should never happen"
 * type, and thus are transformed into {@link RuntimeException}s.
 * <p>
 * The ResultSet is opened on the first next or hasNext method, and closed at the end of the iteration.
 * This means that an object of this type can be created then used hours later; the ResultSet will not have timed out, as the 
 * statement is executed only briefly before the results are needed.
 * <p>
 * The PreparedStatement, and the associated Connection, can also be closed at the end of the iteration if desired
 * (if this is the only code which will be using that statement) or can be left open (if other code will use it).
 * <p>
 * Usage:
 * <pre>
 * public class MyDocResultSetIterator extends ResultSetIterator&lt;MyDoc&gt; {
 *     protected MyDoc objectForRow(ResultSet r) throws SQLException {
 *         MyDoc result = new MyDoc();
 *         result.setVal(r.getString("val"));
 *         return result;
 *     }
 * }
 * public Iterator&lt;MyDoc&gt; foo() {
 *     PreparedStatement stat = aConnection.prepareStatement(
 *         "SELECT val FROM big_table",
 *         ResultSet.TYPE_FORWARD_ONLY,
 *         ResultSet.CONCUR_READ_ONLY);
 *     stat.setFetchSize(Integer.MIN_VALUE);
 *     boolean shouldClosePreparedStatement = truel
 *     return new MyDocResultSetIterator("...", stat, shouldClosePreparedStatement);
 * }
 * </pre>
 * <p>
 * See <a href="http://www.databasesandlife.com/reading-row-by-row-into-java-from-mysql/">Reading row-by-row into Java from MySQL</a>.
 * <p>
 * This code has beem tested using Java 6 with MySQL 5.0 and the JDBC driver "MySQL Connector" 5.1.15.
 *
 * @author This source is copyright <a href="http://www.databasesandlife.com">Adrian Smith</a> and licensed under the LGPL 3.
 */
public abstract class ResultSetIterator<T> implements Iterator<T> {
    
    protected enum State {
                                                BEFORE_EXECUTE,
                      /** nextObject != null */ RETURN_RESULTS,
                        /** resultSet closed */ FINISHED
    };
    
    public enum CloseStrategy {
                                                CLOSE_NOTHING,
                                                CLOSE_STATEMENT,
         /** Closes statement and connection */ CLOSE_CONNECTION
    }
    
    protected State state = State.BEFORE_EXECUTE;
    protected final String sqlForLog;
    protected final PreparedStatement statement;
    protected final CloseStrategy closeStrategy;
    protected ResultSet resultSet;
    protected T nextObject;

    /**
     * @param sqlForLog What SQL was executed? This is only used to populate the text of Exceptions
     */
    public ResultSetIterator(String sqlForLog, PreparedStatement statement, CloseStrategy closeStrategy) {
        this.sqlForLog = sqlForLog;
        this.statement = statement;
        this.closeStrategy = closeStrategy;
    }
    
    protected synchronized void stateTransitionToReturnResults() {
        try {
            if (state != State.BEFORE_EXECUTE) return;
            resultSet = statement.executeQuery();
            state = State.RETURN_RESULTS;
            afterQueryExecuted();
            next(); // load "nextObject" attribute; can stateTransitionToFinished 
        }
        catch (SQLException e) { throw new RuntimeException(sqlForLog + ": " + e.getMessage(), e); }
    }
    
    /**
     * Can be overridden to do things just after the ResultSet has been executed but before any rows have been read.
     * For example process metadata.
     */
    protected void afterQueryExecuted() throws SQLException { }
    
    protected synchronized void stateTransitionToFinished() {
        try {
            if (state != State.RETURN_RESULTS) return;
            resultSet.close();
            Connection connection = statement.getConnection();
            switch (closeStrategy) {
                case CLOSE_NOTHING: break;
                case CLOSE_STATEMENT: statement.close(); break;
                case CLOSE_CONNECTION: statement.close(); connection.close(); break;
            }
            state = State.FINISHED;
        }
        catch (SQLException e) { throw new RuntimeException(sqlForLog + ": " + e.getMessage(), e); }
    }
    
    /**
     * Looks in the current data in the resultSet and
     * returns a new object to be returned from the Iterator.
     * @param r cursor is already at the row containing the data.
     * @return new object representing the data in the ResultSet.
     * @throws SQLException if there is some problem reading the ResultSet.
     *         (This is converted into a RuntimeException by {@link #next}, so there is
     *          no point implementing the same code in the subclass' implementation of this method.)
     */
    protected abstract T newObjectForRow(ResultSet r) throws SQLException;

    public boolean hasNext() {
        if (state == State.BEFORE_EXECUTE) stateTransitionToReturnResults();
        if (state == State.FINISHED) return false;
        return true;
    }

    public T next() {
        try {
            if (state == State.BEFORE_EXECUTE) stateTransitionToReturnResults();
            if (state == State.FINISHED) throw new NoSuchElementException();
            T result = nextObject;
            if (resultSet.next()) nextObject = newObjectForRow(resultSet);
            else stateTransitionToFinished();
            return result;
        }
        catch (SQLException e) { throw new RuntimeException(sqlForLog + ": " + e.getMessage(), e); }
    }

    public void remove() {
        throw new UnsupportedOperationException();
    }
}
