package com.databasesandlife.util.jooq;

import org.jooq.*;
import org.jooq.conf.ParamType;
import org.jooq.impl.DSL;

import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.Types;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * Convert from a MySQL DATETIME (representing a UTC time) to a Java {@link Instant} by using string manipulation.
 *
 * <p>jOOQ uses resultSet.getTimezone() by default. The MySQL JDBC driver does timezone conversions.
 * This means that the {@link LocalDateTime} read from the database isn't the same as the DATETIME database
 * value in the database. Use this class to avoid that conversion.</p>
 *
 * <p>This is only necessary with MySQL; The PostgreSQL JDBC driver does not suffer from this problem.</p>
 */
@SuppressWarnings("unchecked")
public class UtcInstantViaStringBinding<T>
implements Binding<T, Instant> {

    @Override
    public Converter<T, Instant> converter() {
        return new Converter<T, Instant>() {
            @Override public Class<T> fromType() { return (Class<T>) Object.class; }
            @Override public Class<Instant> toType() { return Instant.class; }
            
            @Override
            public Instant from(Object x) {
                if (x == null) return null;
                return LocalDateTime.parse("" + x, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")).atOffset(ZoneOffset.UTC).toInstant();
            }

            @Override
            public T to(Instant m) {
                if (m == null) return null;
                return (T) m.atOffset(ZoneOffset.UTC).toLocalDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            }
        };
    }

    @Override
    public void sql(BindingSQLContext<Instant> ctx) throws SQLException {
        // Depending on how you generate your SQL, you may need to explicitly distinguish
        // between jOOQ generating bind variables or inlined literals.
        if (ctx.render().paramType() == ParamType.INLINED)
            ctx.render().visit(DSL.inline(ctx.convert(converter()).value()));
        else
            ctx.render().sql("?");
    }
    
    @Override
    public void register(BindingRegisterContext<Instant> ctx) throws SQLException {
        ctx.statement().registerOutParameter(ctx.index(), Types.VARCHAR);
    }

    @Override
    public void set(BindingSetStatementContext<Instant> ctx) throws SQLException {
        ctx.statement().setString(
            ctx.index(), 
            Objects.toString(ctx.convert(converter()).value()));
    }

    @Override /// XXX
    public void get(BindingGetResultSetContext<Instant> ctx) throws SQLException {
        ctx.convert(converter()).value((T) ctx.resultSet().getString(ctx.index()));
    }

    @Override
    public void get(BindingGetStatementContext<Instant> ctx) throws SQLException {
        ctx.convert(converter()).value((T) ctx.statement().getString(ctx.index()));
    }

    @Override
    public void set(BindingSetSQLOutputContext<Instant> ctx) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public void get(BindingGetSQLInputContext<Instant> ctx) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }
}