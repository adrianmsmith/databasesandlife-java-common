package com.databasesandlife.util.jdbc;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.apache.log4j.Logger;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.TableRecord;
import org.jooq.impl.DSL;

import com.databasesandlife.util.Timer;
import com.databasesandlife.util.YearMonthDay;
import com.microsoft.sqlserver.jdbc.SQLServerDriver;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import static com.databasesandlife.util.gwtsafe.ConfigurationException.prefixExceptionMessage;

/**
 * Represents a transaction against a database.
 *     <p>
 * MySQL, PostgreSQL and SQL Server are supported.
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
 *       {@link LocalDate}, etc.
 *   <li>{@link #insert} and {@link #update} take Maps of columns as arguments (easier than maintaining SQL strings)
 *   <li>{@link #insertAndFetchNewId} performs an insert and returns the new "auto-increment ID".
 *   <li>{@link #insertIgnoringUniqueConstraintViolations} and {@link #updateIgnoringUniqueConstraintViolations}
 *       perform inserts and updates, but ignore any unique constraint violations.
 *       For example using the "insert then update" pattern, for "just-in-time" creating records, can use these methods.
 *   <li>{@link #attempt(Runnable)} establishes a savepoint before the runnable and rolls back to it on failure,
 *       necessary for any operation that may fail when using PostgreSQL.</li>
 *   <li>The transaction isolation level is set to REPEATABLE READ. (This is the default in MySQL but not other databases.)
 *   <li>You can register {@link RollbackListener} objects with {@link #addRollbackListener(RollbackListener)}.
 *       When the transaction rolls back, this listener will get called.
 *       This is so that any primary keys which have been assigned and stored in Java objects,
 *       which are now no longer valid due to the rollback, may be removed from the Java objects.
 * </ul>
 *     <p>
 * Upon creating an object, a connection is made to the database, and a transaction is started.
 * Upon executing {@link #commit()} or {@link #rollback()} the connection is closed.
 * Although opening a connection each time is not as efficient as using a connection pool, this class is extremely simple,
 * which has advantages both in terms or reliability, maintainability and also speed. (For example, C3P0 has &gt; 50 KLOC).
 * Opening a connection to MySQL is fast.
 * </p>
 * 
 * <p>DbTransaction objects are not thread safe; do not use them from multiple threads simultaneously.</p>
 * 
 * <p>Example usage:
 * <pre>
 *   String jdbc = "jdbc:mysql://hostname/dbName?user=x&amp;password=x&amp;useUnicode=true&amp;characterEncoding=UTF-8";
 *   try (DbTransaction db = new DbTransaction(jdbc)) {
 *      db.execute("DELETE FROM x WHERE id=?", 9);
 *      db.commit();
 *   }
 * </pre>
 * 
 * @author This source is copyright <a href="http://www.databasesandlife.com">Adrian Smith</a> and licensed under the LGPL 3.
 * @see <a href="https://github.com/adrianmsmith/databasesandlife-java-common">Project on GitHub</a>
 */

@SuppressWarnings({ "serial", "deprecation" })
public class DbTransaction implements DbQueryable, AutoCloseable {
    
    public final DbServerProduct product;
    protected Connection connection;    // null means already committed
    protected final List<RollbackListener> rollbackListeners = new ArrayList<>();
    protected final Map<String, PreparedStatement> preparedStatements = new HashMap<>();
    protected final Map<Class<? extends Enum<?>>, String> postgresTypeForEnum = new HashMap<>();

    @Override
    public void close() {
        rollbackIfConnectionStillOpen();
    }

    public enum DbServerProduct { mysql, postgres, sqlserver };
    
    @FunctionalInterface
    public interface DbTransactionFactory {
        /** Caller must call {@link DbTransaction#commit()} or {@link DbTransaction#rollback()}. */
        public DbTransaction newDbTransaction();
    }
    
    public static class UniqueConstraintViolation extends Exception {
        public final String constraintName;
        public UniqueConstraintViolation(String c) { super(); constraintName=c; }
        public UniqueConstraintViolation(String c, Throwable t) { super(t); constraintName=c; }
    }
    
    public static class ForeignKeyConstraintViolation extends Exception {
        public ForeignKeyConstraintViolation() { super(); }
        public ForeignKeyConstraintViolation(Throwable t) { super(t); }
    }
    
    public static class SqlException extends RuntimeException {
        public SqlException(Throwable x) { super(x); }
        public SqlException(String x) { super(x); }
        public SqlException(String prefix, Throwable t) { super(prefixExceptionMessage(prefix, t), t); }
    }
    
    public static class CannotConnectToDatabaseException extends RuntimeException {
        public CannotConnectToDatabaseException(String x) { super(x); }
        public CannotConnectToDatabaseException(String x, Throwable t) { super(x, t); }
    }
    
    @FunctionalInterface public interface RollbackListener {
        public void transactionHasRolledback();
    }
    
    public static class DbQueryResultRow {
        ResultSet rs;
        DbQueryResultRow(ResultSet rs) { this.rs = rs; }
        
        public boolean hasColumn(String columnName) {
            try {
                ResultSetMetaData rsmd = rs.getMetaData();
                int columnCount = rsmd.getColumnCount();
                for (int x = 1; x <= columnCount; x++)
                    if (columnName.equals(rsmd.getColumnName(x))) return true;
                return false;
            }
            catch (SQLException e) { throw new RuntimeException(e); }
        }
        
        public List<String> getColumnNames() {
            try {
                ResultSetMetaData rsmd = rs.getMetaData();
                int columnCount = rsmd.getColumnCount();
                List<String> result = new ArrayList<>(columnCount);
                for (int i = 1; i <= columnCount; i++) result.add(rsmd.getColumnName(i));
                return result;
            }
            catch (SQLException e) { throw new RuntimeException(e); }
        }

        @SuppressFBWarnings("NP_BOOLEAN_RETURN_NULL") // We want to return null here, this is by design 
        public Boolean getBoolean(String col){
            try { boolean result = rs.getBoolean(col); if (rs.wasNull()) return null; else return result; }
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
        
        /**
         * for the bytea/blob data type - returns the stream, does not convert to byte[]
         * @param col column name
         * @return InputStream
         */
        public InputStream getBinaryStream(String col){
            try { InputStream result = rs.getBinaryStream(col); if (rs.wasNull()) return null; else return result; }
            catch (SQLException e) { throw new RuntimeException(e); }
        }
        
        /**
         * for the bytea/blob data type - returns byte[]
         * @param col column name
         * @return byte[];
         */
        public byte[] getByteArray(String col){
            try {
                byte[] result = rs.getBytes(col);
                if (rs.wasNull())
                    return null;
                else 
                    return result;
            } catch (SQLException e) { 
                throw new RuntimeException(e); 
            }
        }

        public Date getDate(String col) {
            try { 
                String str = rs.getString(col);
                if (str == null) return null;
                SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                f.setTimeZone(TimeZone.getTimeZone("UTC"));
                return f.parse(str);
            }
            catch (SQLException | ParseException e) { throw new RuntimeException(e); }
        }
        
        public String[] getStringArray(String col) {
            try {
                java.sql.Array x = rs.getArray(col);
                if (x == null) return null;
                Object[] a = (Object[]) x.getArray();
                return Arrays.copyOf(a, a.length, String[].class);
            }
            catch (SQLException e) { throw new RuntimeException(e); }
        }
        
        public Integer[] getIntegerArray(String col) {
            try {
                java.sql.Array x = rs.getArray(col);
                if (x == null) return null;
                Object[] a = (Object[]) x.getArray();
                return Arrays.copyOf(a, a.length, Integer[].class);
            }
            catch (SQLException e) { throw new RuntimeException(e); }
        }
        
        /** Reads column as string and expects "YYYY-MM-DD" format */
        public YearMonthDay getYearMonthDay(String col) {
            try {
                String str = rs.getString(col);
                if (str == null) return null;
                if (str.length() > "YYYY-MM-DD".length()) str = str.substring(0, "YYYY-MM-DD".length()); // e.g. if col is datetime
                return YearMonthDay.newForYYYYMMDD(str);
            }
            catch (SQLException e) { throw new RuntimeException(e); }
        }
        
        public LocalDate getLocalDate(String col) {
            try {                
                String str = rs.getString(col);
                if (str == null) return null;
                
                return LocalDate.parse(str);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }

        public LocalTime getLocalTime(String col) {
            try {                
                String str = rs.getString(col);
                if (str == null) return null;
                
                return LocalTime.parse(str);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }

        @SuppressWarnings("unchecked")    
        public <T extends Enum<T>> T getEnum(String col, Class<T> clazz) {
            try {
                String str = rs.getString(col);
                if (str == null) return null;
                Method valueOfMethod = clazz.getMethod("valueOf", String.class);
                return (T) valueOfMethod.invoke(null, str);
            }
            catch (SQLException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }

        /**
         * The SELECT must supply a VARCHAR[] as PostgreSQL JDBC driver does not implement ENUM[].
         * For example <code>SELECT my_enum_array::VARCHAR[] ....</code>
         */
        @SuppressWarnings("unchecked")
        public <T extends Enum<T>> T[] getEnumArray(String col, Class<? extends T> componentClass){
            try {
                java.sql.Array x = rs.getArray(col);
                if (x == null) return null;
                Object[] stringArrayFromDb = (Object[]) x.getArray();
                T[] result = (T[]) Array.newInstance(componentClass, stringArrayFromDb.length);
                Method valueOfMethod = componentClass.getMethod("valueOf", String.class);
                for (int i = 0; i < stringArrayFromDb.length; i++)
                    result[i] = (T) valueOfMethod.invoke(null, stringArrayFromDb[i]);
                return result;
            }
            catch (SQLException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }
    }
    
    public static class DbQueryResultRowIterator implements Iterator<DbQueryResultRow> {
        enum State { readingData, /** rs is actually one row forward of iterator */ peeked, finished };
        
        ResultSet rs;
        State state = State.readingData;
        
        protected DbQueryResultRowIterator(ResultSet rs) { 
            this.rs = rs; 
            hasNext(); // this forces the statement to really be executed; important for timing
        }
        
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
    
    public abstract static class DbQueryResultSet implements Iterable<DbQueryResultRow> {

        public Stream<DbQueryResultRow> stream() {
            return StreamSupport.stream(spliterator(), false);
        }
        
        /** 
         * Reads all rows in the result set, finds the string column "stringColumnName" and creates objects of type "cl" by
         * calling its constructor taking a single string argument. 
         */
        public <T> List<T> toObjectList(Class<T> cl, String stringColumnName) {
            try {
                Iterator<DbQueryResultRow> i = iterator();
                List<T> result = new ArrayList<>();
                while (i.hasNext()) {
                    String val = i.next().getString(stringColumnName);
                    T obj = cl.getConstructor(String.class).newInstance(val);
                    result.add(obj);
                }
                return result;
            }
            catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | InstantiationException e) {
                throw new RuntimeException(e);
            }
        }
        
        /** 
         * Reads all rows in the result set, finds the string column "stringColumnName" and creates objects of type "cl" by
         * calling its constructor taking a single string argument. 
         */
        public <T> Set<T> toObjectSet(Class<T> cl, String stringColumnName) {
            return new HashSet<T>(toObjectList(cl, stringColumnName));
        }

        public Set<Integer> toIntegerSet(String columnName) {
            Set<Integer> result = new HashSet<>();
            for (DbQueryResultRow row : this) result.add(row.getInt(columnName));
            return result;
        }

        public Set<Long> toLongSet(String columnName) {
            Set<Long> result = new HashSet<>();
            for (DbQueryResultRow row : this) result.add(row.getLong(columnName));
            return result;
        }
    }
    
    // ---------------------------------------------------------------------------------------------------------------
    // Internal methods
    // ---------------------------------------------------------------------------------------------------------------

    protected void logNewTransaction() {
        Logger.getLogger(DbTransaction.class.getName() + "." + "newTransaction").info("Starting new transaction...");
    }
    
    protected Connection getConnection() {
        if (connection == null) throw new IllegalStateException("connection already committed or rolledback");
        return connection;
    }
    
    protected PreparedStatement getPreparedStatement(String sql) {
        Connection c = getConnection(); // throws if already committed/rolledback
        
        PreparedStatement ps = (PreparedStatement) preparedStatements.get(sql);
        if (ps != null) return ps;
        
        try {
            ps = c.prepareStatement(sql);
            ps.setFetchSize(50);
        }
        catch (SQLException e) { throw new RuntimeException(e); }
        
        preparedStatements.put(sql, ps);
        return ps;
    }
    
    protected PreparedStatement insertParamsToPreparedStatement(String sql, Object... args) {
        Calendar utc = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        
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
                else if (args[i] instanceof BigDecimal)
                    ps.setBigDecimal(i+1, (BigDecimal) args[i]);
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
                            ps.setTimestamp(i+1, ts, utc);
                    }
                else if (args[i] instanceof YearMonthDay)
                    switch (product) {
                        case postgres: 
                            ps.setDate(i+1, new java.sql.Date(((YearMonthDay) args[i]).getMidnightUtcAtStart().getTime()), utc); 
                            break;
                        default:
                            ps.setString(i+1, ((YearMonthDay) args[i]).toYYYYMMDD()); 
                    }
                else if (args[i] instanceof LocalTime)
                    ps.setTime(i+1, java.sql.Time.valueOf((LocalTime) args[i]));
                else if (args[i] instanceof LocalDate)
                    ps.setDate(i+1, java.sql.Date.valueOf((LocalDate) args[i]));
                else if (args[i] instanceof LocalDateTime)
                    ps.setTimestamp(i+1, java.sql.Timestamp.valueOf((LocalDateTime) args[i]));
                else if (args[i] instanceof byte[])
                    ps.setBytes(i+1, (byte[]) args[i]);
                else if (args[i] instanceof Enum<?>)
                    ps.setString(i+1, ((Enum<?>) args[i]).name());
                else if (args[i] instanceof String[])
                    ps.setArray(i+1, connection.createArrayOf("varchar", (String[]) args[i]));
                else if (args[i] instanceof Integer[])
                    ps.setArray(i+1, connection.createArrayOf("int", (Integer[]) args[i]));
                else if (args[i] instanceof Long[])
                    ps.setArray(i+1, connection.createArrayOf("int", (Long[]) args[i]));
                else if (args[i] instanceof Enum<?>[])
                    switch (product) {
                        case postgres:
                            ps.setArray(i+1, connection.createArrayOf(postgresTypeForEnum.get(args[i].getClass().getComponentType()), (Enum<?>[]) args[i]));
                            break;
                        default:
                            throw new RuntimeException("Enum Arrays are not supported for: " + product);
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
        catch (SQLException e) { throw new SqlException(e); }
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
    
    /** @return the name of the unique constraint that was violated, or null if the error was not about a unique constraint violation */
    public static String parseUniqueConstraintViolationOrNull(String msg) {
        { Matcher m = Pattern.compile("Duplicate entry '.*' for key '(.*)'").matcher(msg); if (m.find()) return m.group(1); } // MySQL
        { Matcher m = Pattern.compile("violates unique constraint \"(.*)\"").matcher(msg); if (m.find()) return m.group(1); } // PostgreSQL
        { Matcher m = Pattern.compile("verletzt Unique-Constraint „(.*)“").matcher(msg); if (m.find()) return m.group(1); } // PostgreSQL German
        return null;
    }
    
    public static boolean isForeignKeyConstraintViolation(String msg) {
        if (msg.contains("foreign key constraint")) return true; // MySQL, PostgreSQL 
        if (msg.contains("verletzt Fremdschl")) return true;     // PostgreSQL German
        return false;
    }
    
    /**
     * If "exception" represents a violation exception it is thrown and the connection is rolled back to "initialState",
     * otherwise the original exception is re-thrown.
     */
    protected void rollbackToSavepointAndThrowConstraintViolation(Savepoint initialState, RuntimeException exception) 
    throws UniqueConstraintViolation, ForeignKeyConstraintViolation {
        try {
            String uniqueConstraintViolationOrNull = parseUniqueConstraintViolationOrNull(exception.getMessage());
            boolean isForeignKeyConstraintViolation = isForeignKeyConstraintViolation(exception.getMessage());
            
            if (uniqueConstraintViolationOrNull!=null || isForeignKeyConstraintViolation) {
                if (initialState != null) {
                    connection.rollback(initialState);
                    connection.releaseSavepoint(initialState);
                }
                if (uniqueConstraintViolationOrNull!=null) throw new UniqueConstraintViolation(uniqueConstraintViolationOrNull, exception);
                else if (isForeignKeyConstraintViolation) throw new ForeignKeyConstraintViolation(exception);
                else throw new RuntimeException();
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
        catch (SQLException ignored) {  }  // ignore errors on closing
    }
    
    // ---------------------------------------------------------------------------------------------------------------
    // Public API
    // ---------------------------------------------------------------------------------------------------------------
    
    public DbTransaction(String jdbcUrl) throws CannotConnectToDatabaseException {
        try {
            logNewTransaction();

            if (jdbcUrl.contains(":mysql")) product = DbServerProduct.mysql;
            else if (jdbcUrl.contains(":postgres")) product = DbServerProduct.postgres;
            else if (jdbcUrl.contains(":sqlserver")) product = DbServerProduct.sqlserver;
            else throw new CannotConnectToDatabaseException("Unrecognized server product (mysql or postgres?): " + jdbcUrl);
            
            switch (product) {   // load the classes so that getConnection recognizes the :mysql: etc part of JDBC url
                case mysql: new com.mysql.jdbc.Driver(); break;
                case postgres: new org.postgresql.Driver(); break;
                case sqlserver: new SQLServerDriver(); break;
                default: throw new RuntimeException("Unreachable");
            }
            
            connection = DriverManager.getConnection(jdbcUrl);
            connection.setAutoCommit(false);

            execute("SET TRANSACTION ISOLATION LEVEL REPEATABLE READ");
        }
        catch (SQLException e) {
            throw new CannotConnectToDatabaseException("cannot connect to database '"+jdbcUrl+"': JBDC driver is OK, "+
                "connection is NOT OK: "+e.getMessage(), e);
        }
    }

    public DbTransaction(DbServerProduct product, Connection connection) {
        this.product = product;
        this.connection = connection;
    }

    public void addPostgresTypeForEnum(Class<? extends Enum<?>> enumClass, String postgresType) {
        postgresTypeForEnum.put(enumClass, postgresType);
    }
    
    /**
     * For example, during insert in jOOQ:
     * <pre> 
     * db.addRollbackListener(() -&gt; {
     *    venue.setVid(null); 
     *    venue.changed(VENUES.VID, false);
     * } );
     * </pre>
     */
    public void addRollbackListener(RollbackListener listener) {
        rollbackListeners.add(listener);
    }
    
    public DSLContext jooq() {
        SQLDialect d;
        switch (product) {
            case mysql: d = SQLDialect.MYSQL; break;
            case postgres: d = SQLDialect.POSTGRES; break;
            default: throw new RuntimeException();
        }
        
        return DSL.using(getConnection(), d);
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
    public DbQueryResultSet query(final String sql, final Object... args) {
        return new DbQueryResultSet() {
            public Iterator<DbQueryResultRow> iterator() {
                try (Timer ignored = new Timer("SQL: " + getSqlForLog(sql, args))) {
                    PreparedStatement ps = insertParamsToPreparedStatement(sql, args);
                    ResultSet rs = ps.executeQuery();
                    return new DbQueryResultRowIterator(rs);
                }
                catch (SQLException e) { throw new SqlException(getSqlForLog(sql, args), e); }
            }
        };
    }
    
    /** @return Never retuns null (but may return an empty iterable) */
    public DbQueryResultSet query(CharSequence sql, List<?> args) {
        return query(sql.toString(), args.toArray());
    }
    
    public void execute(String sql, Object... args) throws SqlException {
        try { insertParamsToPreparedStatement(sql, args).executeUpdate(); } // returns int = row count processed; we ignore
        catch (SQLException e) { throw new SqlException("database error ("+ getSqlForLog(sql, args)+")", e); }
    }

    /**
     * Sets a savepoint as is necessary on PostgreSQL, runs the code,
     * then rolls back to the savepoint on RuntimeException or discards the savepoint on success.
     */
    public void attempt(Runnable r) {
        try {
            Savepoint initialState = connection.setSavepoint();
            try {
                r.run();
            }
            catch (RuntimeException e) {
                connection.rollback(initialState);
                throw e;
            }
            finally {
                connection.releaseSavepoint(initialState);
            }
        }
        catch (SQLException e) { throw new SqlException(e); }
    }

    /** For normal delete where you don't expect a possible foreign key constraint violation, use {@link #execute(String, Object...)} instead */
    public void deleteOrThrowForeignKeyConstraintViolation(String table, String where, Object... args) throws ForeignKeyConstraintViolation {
        try {
            Savepoint initialState = null;
            if (product == DbServerProduct.postgres) initialState = connection.setSavepoint();
            try { 
                execute("DELETE FROM " + table + " WHERE " + where, args);
                if (initialState != null) connection.releaseSavepoint(initialState);
            }
            catch (RuntimeException e) { 
                rollbackToSavepointAndThrowConstraintViolation(initialState, e); 
            }
        }
        catch (UniqueConstraintViolation e) { throw new RuntimeException("Unreachable", e); }
        catch (SQLException e) { throw new SqlException(e); }
    }
    
    public void execute(CharSequence sql, List<?> args) {
        execute(sql.toString(), args.toArray());
    }
    
    protected void appendSetClauses(StringBuilder sql, List<Object> params, Map<String, ?> cols) {
        boolean first = true;
        for (Entry<String, ?> c : cols.entrySet()) {
            if (first) first = false; else sql.append(", ");
            sql.append(getSchemaQuote() + c.getKey() + getSchemaQuote());
            sql.append(" = ");
            sql.append(getQuestionMarkForValue(c.getValue()));
            params.add(c.getValue());
        }
    }

    protected void appendInsertStatement(StringBuilder sql, List<Object> params, String table, Map<String, ?> cols) {
        if (cols.isEmpty() && product == DbServerProduct.postgres) {
            // if no columns:
            //     MySQL:      INSERT INTO mytable () VALUES ();
            //     PostgreSQL: INSERT INTO mytable DEFAULT VALUES;
            sql.append("INSERT INTO "+table+" DEFAULT VALUES");
        } else if (product == DbServerProduct.mysql) { // statement is easier to read, therefore easier to debug
            sql.append(" INSERT INTO ");
            sql.append(table);
            sql.append(" SET ");
            appendSetClauses(sql, params, cols);
        } else {
            StringBuilder keys = new StringBuilder();
            StringBuilder questionMarks = new StringBuilder();
            for (Entry<String, ?> c : cols.entrySet()) {
                if (keys.length() > 0) { keys.append(", "); questionMarks.append(", "); }
                keys.append(c.getKey());
                questionMarks.append(getQuestionMarkForValue(c.getValue()));
                params.add(c.getValue());
            }

            sql.append("INSERT INTO "+table+" ("+keys+") VALUES ("+questionMarks+")");
        }
    }
    
    @SuppressFBWarnings("SQL_PREPARED_STATEMENT_GENERATED_FROM_NONCONSTANT_STRING")
    public void insert(String table, Map<String, ?> cols) {
        StringBuilder sql = new StringBuilder();
        List<Object> params = new ArrayList<>();
        appendInsertStatement(sql, params, table, cols);
        execute(sql, params);
    }

    public void insert(TableRecord<?> record) {
        record.attach(jooq().configuration());
        record.insert();
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
                rollbackToSavepointAndThrowConstraintViolation(initialState, e); 
            }
        }
        catch (ForeignKeyConstraintViolation | SQLException e) { throw new SqlException(e); }
    }
    
    public void insertIgnoringUniqueConstraintViolations(String table, Map<String, ?> cols) {
        try { insertOrThrowUniqueConstraintViolation(table, cols); }
        catch (UniqueConstraintViolation ignored) { } // ignore
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
    
    public void update(String table, Map<String, ?> cols, String where, Object... whereParams) {
        StringBuilder sql = new StringBuilder();
        List<Object> params = new ArrayList<>();
        
        sql.append(" UPDATE ");
        sql.append(table);
        sql.append(" SET ");
        appendSetClauses(sql, params, cols);
        sql.append(" WHERE ");
        sql.append(where);
        params.addAll(Arrays.asList(whereParams));
        
        execute(sql, params);
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
                rollbackToSavepointAndThrowConstraintViolation(initialState, e); 
            }
        }
        catch (ForeignKeyConstraintViolation | SQLException e) { throw new SqlException(e); }
    }
    
    public void updateIgnoringUniqueConstraintViolations(String table, Map<String, ?> cols, String where, Object... whereParams) {
        try { updateOrThrowUniqueConstraintViolation(table, cols, where, whereParams); }
        catch (UniqueConstraintViolation ignored) { } // ignore
    }

    /**
     * Inserts (colsToInsert + colsToUpdate) and, if that fails because the row already exists,
     * updates (colsToUpdate) where (primaryKeyColumns out of colsToInsert).
     * @see <a href="http://www.databasesandlife.com/jit-inserting-rows-into-a-db/">"Just-in-time" inserting rows into a database (Databases &amp; Life)</a> 
     */
    public void insertOrUpdate(
        String table,
        Map<String, ?> colsToUpdate,
        Map<String, ?> colsToInsert,
        String... primaryKeyColumns
    ) {
        Map<String, Object> newRow = new HashMap<>();
        newRow.putAll(colsToUpdate);
        newRow.putAll(colsToInsert);

        switch (product) {
            case mysql: // Deadlock if multiple sessions attempt the "default" way
                StringBuilder sql = new StringBuilder();
                List<Object> params = new ArrayList<>(primaryKeyColumns.length);
                appendInsertStatement(sql, params, table, newRow);
                sql.append(" ON DUPLICATE KEY UPDATE ");
                appendSetClauses(sql, params, colsToUpdate);
                execute(sql, params);
                break;

            default:
                try { insertOrThrowUniqueConstraintViolation(table, newRow); }
                catch (UniqueConstraintViolation e) {
                    if (colsToUpdate.isEmpty()) return;
                    StringBuilder where = new StringBuilder();
                    List<Object> whereParams = new ArrayList<>(primaryKeyColumns.length);
                    where.append(" TRUE ");
                    for (String col : primaryKeyColumns) {
                        where.append(" AND ");
                        where.append(getSchemaQuote()).append(col).append(getSchemaQuote());
                        where.append(" = ").append(getQuestionMarkForValue(colsToInsert.get(col)));
                        whereParams.add(colsToInsert.get(col));
                    }
                    update(table, colsToUpdate, where.toString(), whereParams.toArray());
                }
        }
    }
    
    public void rollback() {
        try {
            getConnection().rollback();
            for (RollbackListener l : rollbackListeners) l.transactionHasRolledback();
            closeConnection();
        }
        catch (SQLException e) { throw new SqlException("Can't rollback", e); }
    }
    
    public void commit() {
        try {
            getConnection().commit();
            closeConnection();
        }
        catch (SQLException e) { throw new SqlException("Can't commit", e); }
    }
    
    public void rollbackIfConnectionStillOpen() {
        if (connection != null) rollback();
    }

    public String getFromDual() {
        switch (product) {
            case sqlserver:
            case postgres:
                return "";
            case mysql:
                return " FROM dual ";
            default:
                throw new RuntimeException();
        }
    }
    
    public String getSchemaQuote() {
        switch (product) {
            case sqlserver: return "";
            case postgres: return "\"";
            case mysql: return "`";
            default: throw new RuntimeException();
        }
    }
    
    /** Writes "foo IN (?,?,?)" */
    public <V> void appendIn(Appendable sql, List<? super V> sqlParams, String field, Collection<? extends V> values) {
        try {
            if (values.isEmpty())
                sql.append("FALSE");
            else {
                boolean first = true;
                sql.append(field);
                sql.append(" IN (");
                for (V v : values) {
                    if (first) first=false; else sql.append(",");
                    sql.append(getQuestionMarkForValue(v));
                    sqlParams.add(v);
                }
                sql.append(")");
            }
        }
        catch (IOException e) { throw new RuntimeException(e); }
    }
}
