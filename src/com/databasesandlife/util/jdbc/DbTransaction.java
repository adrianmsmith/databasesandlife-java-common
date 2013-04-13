package com.databasesandlife.util.jdbc;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.sql.Timestamp;
import java.sql.Types;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.databasesandlife.util.Timer;
import com.databasesandlife.util.YearMonthDay;

/**
 * Represents a transaction against a database.
 *     <p>
 * MySQL and PostgreSQL are supported.
 *     <p>
 * An object is created with the JDBC URL to the database.
 * There is no factory for this type of object, simply store the string JDBC URL as opposed to a DbConnectionFactory.
 *     <p>
 * The following facilities are provided 
 * (i.e. these are the reasons why objects of this class might be preferred over simply using JDBC directly):
 * <ul>
 *   <li>SQLExceptions are not thrown. 
 *       This allows checked Exceptions to be used in client code to represent things that might actually happen.
 *       If a SQLException occurs e.g. due to database connectivity, it is assumed there is nothing the program can do apart 
 *          from display an error to the user.
 *   <li>{@link #execute} method prepares statements and does parameter substitution in one line.
 *   <li>{@link #query} method acts like execute, but returns an {@link Iterable} of objects representing rows.
 *       This is more convenient for the java "for" statement than the JDBC ResultSet object.
 *   <li>Various extra data types are supported such as "points in time" stored as GMT date/times using {@link Date java.util.Date}, 
 *       {@link  YearMonthDay}, etc.
 *   <li>{@link #insert} and {@link #update} take Maps of columns as arguments (easier than maintaining SQL strings)
 *   <li>{@link #insertAndFetchNewId} performs an insert and returns the new "auto-increment ID".
 *   <li>{@link #insertIgnoringUniqueConstraintViolations} and {@link #updateIgnoringUniqueConstraintViolations}
 *       perform inserts and updates, but ignore any unique constraint violations.
 *       For example using the "insert then update" pattern, for "just-in-time" creating records, can use these methods.
 * </ul>
 *     <p>
 * Upon creating an object, a connection is made to the database, and a transaction is started.
 * Upon executing {@link #commit()} or {@link #rollback()} the connection is closed.
 * Although opening a connection each time is not as efficient as using a connection pool, this class is extremely simple,
 * which has advantages both in terms or reliability, maintainability and also speed. (For example, C3P0 has &gt; 50 KLOC).
 * Opening a connection to MySQL is fast.
 * </p> 
 * 
 * <p>Example usage:
 * <pre>
 *   DbTransaction db = new DbTransaction(
 *      "jdbc:mysql://hostname/dbName?user=x&password=x&useUnicode=true&characterEncoding=UTF-8");
 *   try {
 *      db.execute("DELETE FROM x WHERE id=?", 9);
 *      db.commit();
 *   }
 *   finally { db.rollbackIfConnectionStillOpen(); }
 * </pre>
 * 
 * @author This source is copyright <a href="http://www.databasesandlife.com">Adrian Smith</a> and licensed under the LGPL 3.
 * @version $Revision$
 */

@SuppressWarnings("serial")
public class DbTransaction {
    
    protected DbServerProduct product;
    protected Connection connection;    // null means already committed
    protected Map<String, PreparedStatement> preparedStatements = new HashMap<String, PreparedStatement>();
    protected Map<Class<? extends Enum<?>>, String> postgresTypeForEnum = new HashMap<Class<? extends Enum<?>>, String>();
    
    public enum DbServerProduct { mysql, postgres };
    
    public interface DbTransactionFactory {
        /** Caller must call {@link DbTransaction#commit()} or {@link DbTransaction#rollback()}. */
        public DbTransaction newDbTransaction();
    }
    
    public static class UniqueConstraintViolation extends Exception {
        UniqueConstraintViolation(Throwable t) { super(t); }
    }
    
    public static class DbQueryResultRow {
        ResultSet rs;
        DbQueryResultRow(ResultSet rs) { this.rs = rs; }
        
        public boolean hasColumn(String columnName) {
            try {
                ResultSetMetaData rsmd = rs.getMetaData();
                int columns = rsmd.getColumnCount();
                for (int x = 1; x <= columns; x++)
                    if (columnName.equals(rsmd.getColumnName(x))) return true;
                return false;
            }
            catch (SQLException e) { throw new RuntimeException(e); }
        }

        public Boolean getBoolean(String col){
            try { return rs.getBoolean(col); }
            catch (SQLException e) { throw new RuntimeException(e); }
        }
        
        public String getString(String col) {
            try { return rs.getString(col); }
            catch (SQLException e) { throw new RuntimeException(e); }
        }
        
        public Integer getInt(String col) {
            try { int result = rs.getInt(col); if (rs.wasNull()) return null; else return result; }
            catch (SQLException e) { throw new RuntimeException(e); }
        }
        
        public Long getLong(String col) {
            try { long result = rs.getLong(col); if (rs.wasNull()) return null; else return result; }
            catch (SQLException e) { throw new RuntimeException(e); }
        }
        
        public Double getDouble(String col) {
            try { double result = rs.getDouble(col); if (rs.wasNull()) return null; else return result; }
            catch (SQLException e) { throw new RuntimeException(e); }
        }
        
        public Date getDate(String col) {
            try { 
                String str = rs.getString(col);
                if (str == null) return null;
                SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                f.setTimeZone(TimeZone.getTimeZone("UTC"));
                return f.parse(str);
            }
            catch (SQLException e) { throw new RuntimeException(e); }
            catch (ParseException e) { throw new RuntimeException(e); }
        }
        
        public String[] getStringArray(String col) {
            try {
                Object[] a = (Object[]) rs.getArray(col).getArray();
                return Arrays.copyOf(a, a.length, String[].class);
            }
            catch (SQLException e) { throw new RuntimeException(e); }
        }
        
        public Integer[] getIntegerArray(String col) {
            try {
                Object[] a = (Object[]) rs.getArray(col).getArray();
                return Arrays.copyOf(a, a.length, Integer[].class);
            }
            catch (SQLException e) { throw new RuntimeException(e); }
        }
        
        public YearMonthDay getYearMonthDay(String col) {
            try {
                String str = rs.getString(col);
                if (str == null) return null;
                return YearMonthDay.newForYYYYMMDD(str);
            }
            catch (SQLException e) { throw new RuntimeException(e); }
        }

        @SuppressWarnings("unchecked")    
        public <T extends Enum<T>> T getEnum(String col, Class<T> clazz) {
            try {
                String str = rs.getString(col);
                if (str == null) return null;
                Method valueOfMethod = clazz.getMethod("valueOf", String.class);
                return (T) valueOfMethod.invoke(null, str);
            }
            catch (SQLException e) { throw new RuntimeException(e); }
            catch (NoSuchMethodException e) { throw new RuntimeException(e); }
            catch (InvocationTargetException e) { throw new RuntimeException(e); }
            catch (IllegalAccessException e) { throw new RuntimeException(e); }
        }

        /**
         * The SELECT must supply a VARCHAR[] as PostgreSQL JDBC driver does not implement ENUM[].
         * For example <code>SELECT my_enum_array::VARCHAR[] ....</code>
         */
        @SuppressWarnings("unchecked")
        public <T extends Enum<T>> T[] getEnumArray(String col, Class<? extends T> componentClass){
            try {
                Method valueOfMethod = componentClass.getMethod("valueOf", String.class);
                Object[] stringArrayFromDb = (Object[]) rs.getArray(col).getArray();
                T[] result = (T[]) Array.newInstance(componentClass, stringArrayFromDb.length);
                for (int i = 0; i < stringArrayFromDb.length; i++)
                    result[i] = (T) valueOfMethod.invoke(null, stringArrayFromDb[i]);
                return result;
            }
            catch (SQLException e) { throw new RuntimeException(e); }
            catch (NoSuchMethodException e) { throw new RuntimeException(e); }
            catch (InvocationTargetException e) { throw new RuntimeException(e); }
            catch (IllegalAccessException e) { throw new RuntimeException(e); }
        }
    }
    
    protected static class DbQueryResultSet implements Iterator<DbQueryResultRow> {
        enum State { readingData, /** rs is actually one row forward of iterator */ peeked, finished };
        
        ResultSet rs;
        State state = State.readingData;
        
        DbQueryResultSet(ResultSet rs) { this.rs = rs; }
        
        @Override public boolean hasNext() {
            try {
                if (state == State.readingData) { if (rs.next()) state = State.peeked; else state = State.finished; }
                if (state == State.peeked) return true;
                if (state == State.finished) return false;
                throw new RuntimeException();
            }
            catch (SQLException e) { throw new RuntimeException(e); }
        }
        
        @Override public DbQueryResultRow next() {
            hasNext(); // make sure we are peeking or finished
            if (state == State.peeked) { state = State.readingData; return new DbQueryResultRow(rs); }
            if (state == State.finished) throw new NoSuchElementException();
            throw new RuntimeException();
        }
        
        @Override public void remove() {
            throw new UnsupportedOperationException();
        }
    }
    
    // ---------------------------------------------------------------------------------------------------------------
    // Internal methods
    // ---------------------------------------------------------------------------------------------------------------
    
    protected Connection getConnection() {
        if (connection == null) throw new IllegalStateException("connection already committed or rolledback");
        return connection;
    }
    
    protected PreparedStatement getPreparedStatement(String sql) {
        Connection c = getConnection(); // throws if already committed/rolledback
        
        PreparedStatement ps = (PreparedStatement) preparedStatements.get(sql);
        if (ps != null) return ps;
        
        try { ps = c.prepareStatement(sql); }
        catch (SQLException e) { throw new RuntimeException(e); }
        
        preparedStatements.put(sql, ps);
        return ps;
    }
    
    protected PreparedStatement insertParamsToPreparedStatement(String sql, Object... args) {
        PreparedStatement ps = getPreparedStatement(sql);
        for (int i = 0; i < args.length; i++) {
            try {
                if (args[i] == null) 
                    ps.setNull(i+1,Types.NULL);
                else if (args[i] instanceof Boolean)
                    ps.setBoolean(i+1, (Boolean) args[i]);
                else if (args[i] instanceof String) // setXx 1st param: first arg is 1 not 0
                    ps.setString(i+1, (String) args[i]);
                else if (args[i] instanceof Integer)
                    ps.setInt(i+1, ((Integer) args[i]).intValue());
                else if (args[i] instanceof Long)
                    ps.setLong(i+1, ((Long) args[i]).longValue());
                else if (args[i] instanceof Double)
                    ps.setDouble(i+1, ((Double) args[i]).doubleValue());
                else if (args[i] instanceof java.util.Date)
                    switch (product) {
                        case mysql:
                            // Can't set Timestamp object directly, see http://bugs.mysql.com/bug.php?id=15604 w.r.t GMT timezone
                            SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                            f.setTimeZone(TimeZone.getTimeZone("UTC"));
                            ps.setString(i+1, f.format((java.util.Date) args[i]));
                            break;
                        default:
                            Timestamp ts = new java.sql.Timestamp(((java.util.Date) args[i]).getTime());
                            Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
                            ps.setTimestamp(i+1, ts, cal);
                    }
                else if (args[i] instanceof YearMonthDay)
                    ps.setString(i+1, ((YearMonthDay) args[i]).toYYYYMMDD());
                else if (args[i] instanceof byte[])
                    ps.setBytes(i+1, (byte[]) args[i]);
                else if (args[i] instanceof Enum<?>)
                    ps.setString(i+1, ((Enum<?>) args[i]).name());
                else if (args[i] instanceof String[])
                    ps.setArray(i+1, connection.createArrayOf("varchar", (String[]) args[i]));
                else if (args[i] instanceof Integer[])
                    ps.setArray(i+1, connection.createArrayOf("int", (Integer[]) args[i]));
                else if (args[i] instanceof Enum<?>[])
                    switch (product) {
                        case postgres:
                            ps.setArray(i+1, connection.createArrayOf(postgresTypeForEnum.get(args[i].getClass().getComponentType()), (Enum<?>[]) args[i]));
                            break;
                        default:
                            throw new RuntimeException("Enum Arrays are not supported for: " + "product");
                    }
                else 
                    throw new RuntimeException("sql='"+sql+
                        "': unexpected type for argument "+i+": "+args[i].getClass());
            }
            catch (SQLException e) {
                throw new RuntimeException("sql='"+sql+
                    "': unexpected error setting argument "+i+": "+e.getMessage(), e);
            }
        }
        return ps;
    }
    
    protected long fetchNewPkValue() {
        try {
            String sql;
            switch (product) {
                case mysql: sql = "SELECT LAST_INSERT_ID() AS id"; break;
                case postgres: sql = "SELECT lastval() AS id"; break;
                default: throw new RuntimeException();
            }
            PreparedStatement ps = insertParamsToPreparedStatement(sql);
            ResultSet rs = ps.executeQuery();
            if ( ! rs.next()) throw new RuntimeException("SQL to request just-inserted PK value returned no results");
            long result = rs.getLong("id");
            rs.close();
            return result;
        }
        catch (SQLException e) { throw new RuntimeException(e); }
    }
    
    protected String getQuestionMarkForValue(Object value) {
        if (product == DbServerProduct.postgres) {
            if (value instanceof Enum<?>) {
                String type = postgresTypeForEnum.get(value.getClass());
                if (type == null) throw new RuntimeException("Cannot convert Java Enum '" + value.getClass() + "' to " +
                    "Postgres ENUM type: use addPostgresTypeForEnum method after DbTransaction constructor");
                return "?::" + type;
            }
            if (value instanceof Enum<?>[]) {
                String type = postgresTypeForEnum.get(value.getClass().getComponentType());
                if (type == null) throw new RuntimeException("Cannot convert Java Enum '" + value.getClass() + "' to " +
                    "Postgres ENUM type: use addPostgresTypeForEnum method after DbTransaction constructor");
                return "?::" + type + "[]";
            }
        }
        return "?";
    }
    
    /**
     * If "exception" represents a violation exception it is thrown and the connection is rolled back to "initialState",
     * otherwise the original exception is re-thrown.
     */
    protected void rollbackToSavepointAndThrowUniqueConstraintViolation(Savepoint initialState, RuntimeException exception) 
    throws UniqueConstraintViolation {
        try {
            boolean isConstraintViolation = false;
            if (exception.getMessage().contains("Duplicate entry")) isConstraintViolation = true;               // MySQL
            if (exception.getMessage().contains("violates unique constraint")) isConstraintViolation = true;    // PostgreSQL
            
            if (isConstraintViolation) {
                if (initialState != null) {
                    connection.rollback(initialState);
                    connection.releaseSavepoint(initialState);
                }
                throw new UniqueConstraintViolation(exception);
            }
            else {
                throw exception;
            }
        }
        catch (SQLException e) { throw new RuntimeException(e); }
    }
    
    protected void closeConnection() {
        try {
            for (PreparedStatement p : preparedStatements.values()) p.close();
            connection.close();
            connection = null;
        }
        catch (SQLException e) {  }  // ignore errors on closing
    }
    
    // ---------------------------------------------------------------------------------------------------------------
    // Public API
    // ---------------------------------------------------------------------------------------------------------------
    
    public DbTransaction(String jdbcUrl) {
        try {
            Logger.getLogger(DbTransaction.class.getName() + "." + "newTransaction").info("Starting new transaction...");
            
            if (jdbcUrl.contains(":mysql")) product = DbServerProduct.mysql;
            else if (jdbcUrl.contains(":postgres")) product = DbServerProduct.postgres;
            else throw new RuntimeException("Unrecognized server product (mysql or postgres?): " + jdbcUrl);
            
            switch (product) {   // load the classes so that getConnection recognizes the :mysql: etc part of JDBC url
                case mysql: new com.mysql.jdbc.Driver(); break;
                case postgres: new org.postgresql.Driver(); break;
            }
            
            connection = DriverManager.getConnection(jdbcUrl);
            connection.setAutoCommit(false);
        }
        catch (SQLException e) {
            throw new RuntimeException("cannot connect to database '"+jdbcUrl+"': JBDC driver is OK, "+
                "connection is NOT OK: "+e.getMessage(), e);
        }
    }
    
    public void addPostgresTypeForEnum(Class<? extends Enum<?>> enumClass, String postgresType) {
        postgresTypeForEnum.put(enumClass, postgresType);
    }
    
    public static String getSqlForLog(String sql, Object[] args) {
        StringBuffer result = new StringBuffer();
        Matcher m = Pattern.compile(Pattern.quote("?")).matcher(sql);
        int argIdx = 0;
        while (m.find()) {
            Object arg = args[argIdx++];
            String argUnencoded = "" + arg;
            if (arg instanceof java.util.Date) {
                SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                f.setTimeZone(TimeZone.getTimeZone("UTC"));
                argUnencoded = f.format((java.util.Date) arg);
            }
            String argEncoded = argUnencoded.replace("'", "\\'");
            m.appendReplacement(result, Matcher.quoteReplacement("'" + argEncoded + "'"));
        }
        m.appendTail(result);
        return result.toString();
    }
    
    /** @return Never retuns null (but may return an empty iterable) */
    public Iterable<DbQueryResultRow> query(final String sql, final Object... args) {
        return new Iterable<DbQueryResultRow>() {
            public Iterator<DbQueryResultRow> iterator() {
                Timer.start("SQL: " + getSqlForLog(sql, args));
                try {
                    PreparedStatement ps = insertParamsToPreparedStatement(sql, args);
                    ResultSet rs = ps.executeQuery();
                    Iterator<DbQueryResultRow> result = new DbQueryResultSet(rs);
                    result.hasNext(); // this forces the statement to really be executed; important for timing
                    return result;
                }
                catch (SQLException e) { throw new RuntimeException(getSqlForLog(sql, args) + ": " + e.getMessage(), e); }
                finally { Timer.end("SQL: " + getSqlForLog(sql, args)); }
            }
        };
    }
    
    public void execute(String sql, Object... args) {
        try { insertParamsToPreparedStatement(sql, args).executeUpdate(); } // returns int = row count processed; we ignore
        catch (SQLException e) { throw new RuntimeException("database error ("+ getSqlForLog(sql, args)+"): " + e.getMessage(), e); }
    }
    
    public void insert(String table, Map<String, ?> cols) {
        if (cols.isEmpty() && product == DbServerProduct.postgres) {
            // if no columns:
            //     MySQL:      INSERT INTO mytable () VALUES ();
            //     PostgreSQL: INSERT INTO mytable DEFAULT VALUES;
            execute("INSERT INTO "+table+" DEFAULT VALUES");
        } else {
            StringBuilder keys = new StringBuilder();
            StringBuilder questionMarks = new StringBuilder();
            List<Object> values = new ArrayList<Object>();
            for (Entry<String, ?> c : cols.entrySet()) {
                if (keys.length() > 0) { keys.append(", "); questionMarks.append(", "); }
                keys.append(c.getKey());
                questionMarks.append(getQuestionMarkForValue(c.getValue()));
                values.add(c.getValue());
            }
            
            execute("INSERT INTO "+table+" ("+keys+") VALUES ("+questionMarks+")", values.toArray());
        }
    }
    
    public void insertOrThrowUniqueConstraintViolation(String table, Map<String, ?> cols)
    throws UniqueConstraintViolation {
        try {
            Savepoint initialState = null;
            if (product == DbServerProduct.postgres) initialState = connection.setSavepoint();
            try { 
                insert(table, cols); 
                if (initialState != null) connection.releaseSavepoint(initialState);
            }
            catch (RuntimeException e) { 
                rollbackToSavepointAndThrowUniqueConstraintViolation(initialState, e); 
            }
        }
        catch (SQLException e) { throw new RuntimeException(e); }
    }
    
    public void insertIgnoringUniqueConstraintViolations(String table, Map<String, ?> cols) {
        try { insertOrThrowUniqueConstraintViolation(table, cols); }
        catch (UniqueConstraintViolation e) { } // ignore
    }
    
    public long insertAndFetchNewId(String table, Map<String, ?> cols) {
        insert(table, cols);
        return fetchNewPkValue();
    }
    
    public long insertAndFetchNewIdOrThrowUniqueConstraintViolation(String table, Map<String, ?> cols)
            throws UniqueConstraintViolation {
        insertOrThrowUniqueConstraintViolation(table, cols);
        return fetchNewPkValue();
    }
    
    /**
     * @deprecated
     * Use individual {@link #insert(String, Map)} statements.
     * The idea was that inserting in bulk would reduce latency between the app and the database,
     * but in fact it turned out that having a statement with lots of ? takes longer than the saved latency.
     */
    public void insertBulk(String table, List<Map<String, ?>> rows) {
        // MySQL driver cannot handle long statements; 15s for this vs 36s for multiple VALUES INSERT
        switch (product) {
            default: for (Map<String, ?> cols : rows) insert(table, cols); break;
        }
    }
    
    public void update(String table, Map<String, ?> cols, String where, Object... whereParams) {
        StringBuilder sql = new StringBuilder();
        List<Object> params = new ArrayList<Object>();
        
        sql.append("UPDATE ");
        sql.append(table);
        sql.append(" SET ");
        for (Entry<String, ?> c : cols.entrySet()) {
            if (params.size() > 0) sql.append(", ");
            sql.append(c.getKey());
            sql.append(" = ");
            sql.append(getQuestionMarkForValue(c.getValue()));
            params.add(c.getValue());
        }
        sql.append(" WHERE ");
        sql.append(where);
        params.addAll(Arrays.asList(whereParams));
        
        execute(sql.toString(), params.toArray());
    }
    
    public void updateOrThrowUniqueConstraintViolation(String table, Map<String, ?> cols, String where, Object... whereParams)
    throws UniqueConstraintViolation {
        try {
            Savepoint initialState = null;
            if (product == DbServerProduct.postgres) initialState = connection.setSavepoint();
            try { 
                update(table, cols, where, whereParams); 
                if (initialState != null) connection.releaseSavepoint(initialState);
            }
            catch (RuntimeException e) { 
                rollbackToSavepointAndThrowUniqueConstraintViolation(initialState, e); 
            }
        }
        catch (SQLException e) { throw new RuntimeException(e); }
    }
    
    public void updateIgnoringUniqueConstraintViolations(String table, Map<String, ?> cols, String where, Object... whereParams) {
        try { updateOrThrowUniqueConstraintViolation(table, cols, where, whereParams); }
        catch (UniqueConstraintViolation e) { } // ignore
    }
    
    public void rollback() {
        try {
            getConnection().rollback();
            closeConnection();
        }
        catch (SQLException e) { throw new RuntimeException("Can't rollback: " + e.getMessage(), e); }
    }
    
    public void commit() {
        try {
            getConnection().commit();
            closeConnection();
        }
        catch (SQLException e) { throw new RuntimeException("Can't commit: " + e.getMessage(), e); }
    }
    
    public void rollbackIfConnectionStillOpen() {
        if (connection != null) rollback();
    }
}
