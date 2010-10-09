package com.databasesandlife.util.jdbc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Example usage:
 * <pre>
 *   try { new com.mysql.jdbc.Driver(); } catch (SQLException e) { throw new RuntimeException(); }
 *   DbClient db = new DbClient("jdbc:mysql://hostname/dbName?user=x&password=x&useUnicode=true&characterEncoding=UTF-8");
 *   db.doSqlAction("DELETE FROM x WHERE id=?", new Object[] { 9 }");
 *   // doSqlQuery, doSqlInsert
 *   db.commit();
 * </pre>
 */

public class DbClient {
    
    protected Connection connection;    // null means already committed
    protected Map<String, PreparedStatement> preparedStatements = new HashMap<String, PreparedStatement>();
    
    public static class UniqueConstraintViolation extends RuntimeException {
        UniqueConstraintViolation(String msg, Throwable t) { super(msg, t); }
    }
    
    public DbClient(String jdbcUrl) {
        try {
            connection = DriverManager.getConnection(jdbcUrl);
            connection.setAutoCommit(false);
        } catch (SQLException e) {
            throw new RuntimeException("cannot connect to database '"+jdbcUrl+"': JBDC driver is OK, connection is NOT OK", e);
        }
    }
    
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
                else if (args[i] instanceof java.util.Date)
                    ps.setTimestamp(i+1,
                        new java.sql.Timestamp(((java.util.Date) args[i]).getTime()));
                else if (args[i] instanceof byte[]) {
                    ps.setBytes(i+1, (byte[]) args[i]);
                } else 
                    throw new RuntimeException("DBClient: sql='"+sql+
                    "': unexpected type for argument "+i+": "+args[i].getClass());
            } catch (SQLException e) {
                throw new RuntimeException("DBClient: sql='"+sql+
                "': unexpected error setting argument "+i, e);
            }
        }
        return ps;
    }
    
    public static String getSqlForLog(String sql, Object[] args) {
        String result = "sql=(" + sql + "), args=(";
        for (int i=0; i<args.length; i++) {
            String comma = (i == 0) ? "" : ",";
            result += comma + args[i];
        }
        return result + ")";
    }
    
    /** @param args an arr of String or Integer objs for ? values
      * @return to use do<br>
      * <pre> while (result.next()) {
      *         System.out.println("3 is " + result.getInt(1));
      *       }
      *       result.close(); </pre> */
    public ResultSet doSqlQuery(String sql, Object... args) {
        try {
            PreparedStatement ps = insertParamsToPreparedStatement(sql, args);
            return ps.executeQuery();
        } catch (SQLException e) {
            throw new RuntimeException("database error ("+
                getSqlForLog(sql, args)+")", e);
        }
    }
    
    /** @param args an arr of String or Integer objs for ? values */
    public void doSqlAction(String sql, Object... args) {
        try {
            PreparedStatement ps = insertParamsToPreparedStatement(sql, args);
            ps.executeUpdate();  // returns int = row count processed; we ignore
        } catch (SQLIntegrityConstraintViolationException e) {
            throw new UniqueConstraintViolation(getSqlForLog(sql, args), e);
        } catch (SQLException e) {
            throw new RuntimeException("database error ("+
                getSqlForLog(sql, args)+")", e);
        }
    }

    protected long getLastInsertId() {
        ResultSet autoIncrRS = doSqlQuery("select LAST_INSERT_ID() AS id");
        try {
            try {
                if ( ! autoIncrRS.next()) throw new RuntimeException("unreachable");
                return autoIncrRS.getLong("id");
            }
            finally { autoIncrRS.close(); }
        }
        catch (SQLException e) { throw new RuntimeException(e); }
    }
    
    public long doSqlInsert(String sql, Object... args) {
        doSqlAction(sql, args);
        return getLastInsertId();
    }
    
    public long doSqlInsertMap(String table, Map<String, ?> cols) {
        StringBuilder keys = new StringBuilder();
        StringBuilder questionMarks = new StringBuilder();
        List<Object> values = new ArrayList<Object>();
        for (Entry<String, ?> c : cols.entrySet()) {
            if (keys.length() > 0) { keys.append(", "); questionMarks.append(", "); }
            keys.append(c.getKey());
            questionMarks.append("?");
            values.add(c.getValue());
        }
        
        return doSqlInsert("INSERT INTO "+table+" ("+keys+") VALUES ("+questionMarks+")", values.toArray());
    }
    
    protected void closeConnection() {
        try {
            for (PreparedStatement p : preparedStatements.values()) p.close();
            connection.close();
            connection = null;
        }
        catch (SQLException e) {  }  // ignore errors on closing
    }
    
    public void rollback() {
        try {
            getConnection().rollback();
            closeConnection();
        }
        catch (SQLException e) { throw new RuntimeException("Can't rollback", e); }
   }
    
    public void commit() {
        try {
            getConnection().commit();
            closeConnection();
        }
        catch (SQLException e) { throw new RuntimeException("Can't commit", e); }
    }
    
    public void rollbackIfConnectionStillOpen() {
        if (connection != null) rollback();
    }
}
