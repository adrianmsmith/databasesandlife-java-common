package com.databasesandlife.util.jdbc;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
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

import com.databasesandlife.util.Timer;
import com.databasesandlife.util.YearMonthDay;

/**
 * Represents a transaction against a database.
 *     <p>
 * MySQL is supported.
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
 *   <li>execute and query methods prepare statements and do parameter substitution in one line.
 *   <li>Various extra data types are supported such as GMT Dates ({@linkplain Date java.util.Date}), {@linkplain YearMonthDay}, etc.
 *   <li>Various convenience methods such as insertMap are provided.
 *   <li>Upon a unique constraint violation, the exception {@linkplain UniqueConstraintViolation} is thrown, which can be caught.  
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

public class DbTransaction {
    
    protected Connection connection;    // null means already committed
    protected Map<String, PreparedStatement> preparedStatements = new HashMap<String, PreparedStatement>();
    
    public static class UniqueConstraintViolation extends RuntimeException {
        UniqueConstraintViolation(String msg, Throwable t) { super(msg+": "+t.getMessage(), t); }
    }
    
    public static class DbQueryResultRow {
        ResultSet rs;
        DbQueryResultRow(ResultSet rs) { this.rs = rs; }
        
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
        
        public YearMonthDay getYearMonthDay(String col) {
            try {
                String str = rs.getString(col);
                if (str == null) return null;
                return YearMonthDay.newForYYYYMMDD(str);
            }
            catch (SQLException e) { throw new RuntimeException(e); }
        }
        
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
    
    protected PreparedStatement insertParamsToPreparedStatement(
        String sql, Object[] args
    ) {
        PreparedStatement ps = getPreparedStatement(sql);
        for (int i = 0; i < args.length; i++) {
            try {
                if (args[i] == null) 
                    ps.setString(i+1, null);
                else if (args[i] instanceof String) // setXx 1st param: first arg is 1 not 0
                    ps.setString(i+1, (String) args[i]);
                else if (args[i] instanceof Integer)
                    ps.setInt(i+1, ((Integer) args[i]).intValue());
                else if (args[i] instanceof Long)
                    ps.setLong(i+1, ((Long) args[i]).longValue());
                else if (args[i] instanceof Double)
                    ps.setDouble(i+1, ((Double) args[i]).doubleValue());
                else if (args[i] instanceof java.util.Date) {
                    // Can't set Timestamp object directly, see http://bugs.mysql.com/bug.php?id=15604 w.r.t GMT timezone
                    SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    f.setTimeZone(TimeZone.getTimeZone("UTC"));
                    ps.setString(i+1, f.format((java.util.Date) args[i]));   }
                else if (args[i] instanceof YearMonthDay)
                    ps.setString(i+1, ((YearMonthDay) args[i]).toYYYYMMDD());
                else if (args[i] instanceof byte[])
                    ps.setBytes(i+1, (byte[]) args[i]);
                else if (args[i] instanceof Enum<?>)
                    ps.setString(i+1, ((Enum<?>) args[i]).name());
                else 
                    throw new RuntimeException("sql='"+sql+
                        "': unexpected type for argument "+i+": "+args[i].getClass());
            } catch (SQLException e) {
                throw new RuntimeException("sql='"+sql+
                    "': unexpected error setting argument "+i+": "+e.getMessage(), e);
            }
        }
        return ps;
    }
    
    protected long getLastInsertId() {
        return query("select LAST_INSERT_ID() AS id").iterator().next().getLong("id");
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
            new com.mysql.jdbc.Driver();   // load the MySQL classes so that getConnection works
            connection = DriverManager.getConnection(jdbcUrl);
            connection.setAutoCommit(false);
        } catch (SQLException e) {
            throw new RuntimeException("cannot connect to database '"+jdbcUrl+"': JBDC driver is OK, "+
                "connection is NOT OK: "+e.getMessage(), e);
        }
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
    
    public Iterable<DbQueryResultRow> query(final String sql, final Object... args) {
        return new Iterable<DbQueryResultRow>() {
            public Iterator<DbQueryResultRow> iterator() {
                PreparedStatement ps = insertParamsToPreparedStatement(sql, args);
                Timer.start("SQL: " + getSqlForLog(sql, args));
                try {
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
        try {
            try {
                System.out.println("SQL: " + getSqlForLog(sql, args));
                
                PreparedStatement ps = insertParamsToPreparedStatement(sql, args);
                ps.executeUpdate();  // returns int = row count processed; we ignore
            } catch (SQLIntegrityConstraintViolationException e) {
                if (e.getMessage().contains("Duplicate entry")) throw new UniqueConstraintViolation(getSqlForLog(sql, args), e);
                throw e;
            }
        } catch (SQLException e) {
            throw new RuntimeException("database error ("+
                getSqlForLog(sql, args)+"): " + e.getMessage(), e);
        }
    }

    public long insert(String sql, Object... args) {
        execute(sql, args);
        return getLastInsertId();
    }
    
    public long insertMap(String table, Map<String, ?> cols) {
        StringBuilder keys = new StringBuilder();
        StringBuilder questionMarks = new StringBuilder();
        List<Object> values = new ArrayList<Object>();
        for (Entry<String, ?> c : cols.entrySet()) {
            if (keys.length() > 0) { keys.append(", "); questionMarks.append(", "); }
            keys.append(c.getKey());
            questionMarks.append("?");
            values.add(c.getValue());
        }
        
        return insert("INSERT INTO "+table+" ("+keys+") VALUES ("+questionMarks+")", values.toArray());
    }
    
    public void updateMap(String table, Map<String, ?> cols, String where, Object... whereParams) {
        StringBuilder sql = new StringBuilder();
        List<Object> params = new ArrayList<Object>();
        
        sql.append("UPDATE ");
        sql.append(table);
        sql.append(" SET ");
        for (Entry<String, ?> c : cols.entrySet()) {
            if (params.size() > 0) sql.append(", ");
            sql.append(c.getKey());
            sql.append(" = ?");
            params.add(c.getValue());
        }
        sql.append(" WHERE ");
        sql.append(where);
        params.addAll(Arrays.asList(whereParams));
        
        execute(sql.toString(), params.toArray());
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
