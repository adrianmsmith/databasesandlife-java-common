package com.databasesandlife.util.jooq;

import org.jooq.*;
import org.jooq.conf.ParamType;
import org.jooq.impl.DSL;

import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.Types;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * Convert from a MySQL DATETIME to a Java {@link LocalDateTime} by using string manipulation.
 * 
 * <p>jOOQ uses resultSet.getTimezone() by default. The MySQL JDBC driver does timezone conversions.
 * This means that the {@link LocalDateTime} read from the database isn't the same as the DATETIME database
 * value in the database. Use this class to avoid that conversion.</p>
 * 
 * <p>This is only necessary with MySQL; The PostgreSQL JDBC driver does not suffer from this problem.</p>
 */
@SuppressWarnings("unchecked")
public class LocalDateTimeViaStringBinding<T>
implements Binding<T, LocalDateTime> {

    @Override
    public Converter<T, LocalDateTime> converter() {
        return new Converter<T, LocalDateTime>() {
            @Override public Class<T> fromType() { return (Class<T>) Object.class; }
            @Override public Class<LocalDateTime> toType() { return LocalDateTime.class; }
            
            @Override
            public LocalDateTime from(Object x) {
                if (x == null) return null;
                return LocalDateTime.parse("" + x, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            }

            @Override
            public T to(LocalDateTime m) {
                if (m == null) return null;
                return (T) m.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            }
        };
    }

    @Override
    public void sql(BindingSQLContext<LocalDateTime> ctx) throws SQLException {
        // Depending on how you generate your SQL, you may need to explicitly distinguish
        // between jOOQ generating bind variables or inlined literals.
        if (ctx.render().paramType() == ParamType.INLINED)
            ctx.render().visit(DSL.inline(ctx.convert(converter()).value()));
        else
            ctx.render().sql("?");
    }
    
    @Override
    public void register(BindingRegisterContext<LocalDateTime> ctx) throws SQLException {
        ctx.statement().registerOutParameter(ctx.index(), Types.VARCHAR);
    }

    @Override
    public void set(BindingSetStatementContext<LocalDateTime> ctx) throws SQLException {
        ctx.statement().setString(
            ctx.index(), 
            Objects.toString(ctx.convert(converter()).value()));
    }

    @Override /// XXX
    public void get(BindingGetResultSetContext<LocalDateTime> ctx) throws SQLException {
        ctx.convert(converter()).value((T) ctx.resultSet().getString(ctx.index()));
    }

    @Override
    public void get(BindingGetStatementContext<LocalDateTime> ctx) throws SQLException {
        ctx.convert(converter()).value((T) ctx.statement().getString(ctx.index()));
    }

    @Override
    public void set(BindingSetSQLOutputContext<LocalDateTime> ctx) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public void get(BindingGetSQLInputContext<LocalDateTime> ctx) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }
}