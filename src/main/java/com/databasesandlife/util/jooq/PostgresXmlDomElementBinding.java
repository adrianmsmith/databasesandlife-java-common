package com.databasesandlife.util.jooq;

import com.databasesandlife.util.DomParser;
import com.databasesandlife.util.gwtsafe.ConfigurationException;
import org.jooq.*;
import org.jooq.conf.ParamType;
import org.jooq.impl.DSL;
import org.w3c.dom.Element;

import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.Types;
import java.util.Objects;

/**
 * Binding to allow PostgreSQL XML columns to be mapped to/from DOM {@link org.w3c.dom.Element} objects.
 */
public class PostgresXmlDomElementBinding
implements Binding<Object, Element> {

    @Override
    public Converter<Object, Element> converter() {
        return new Converter<Object, Element>() {
            @Override public Class<Object> fromType() {
                return Object.class; 
            }
            
            @Override public Class<Element> toType() { 
                return Element.class;
            }
            
            @Override public Element from(Object db) {
                try {
                    if (db == null) return null;
                    return DomParser.from(db.toString());
                }
                catch (ConfigurationException e) { throw new RuntimeException(e); }
            }
            
            @Override public Object to(Element java) {
                if (java == null) return null;
                return DomParser.formatXml(java);
            }
        };
    }

    @Override
    public void sql(BindingSQLContext<Element> ctx) throws SQLException {
        // Depending on how you generate your SQL, you may need to explicitly distinguish
        // between jOOQ generating bind variables or inlined literals.
        if (ctx.render().paramType() == ParamType.INLINED)
            ctx.render().visit(DSL.inline(ctx.convert(converter()).value())).sql("::xml");
        else
            ctx.render().sql("?::xml");
    }

    @Override
    public void register(BindingRegisterContext<Element> ctx) throws SQLException {
        ctx.statement().registerOutParameter(ctx.index(), Types.VARCHAR);
    }

    @Override
    public void set(BindingSetStatementContext<Element> ctx) throws SQLException {
        Object value = ctx.convert(converter()).value();
        ctx.statement().setString(
            ctx.index(), 
            value == null ? null : Objects.toString(value));
    }

    @Override
    public void get(BindingGetResultSetContext<Element> ctx) throws SQLException {
        ctx.convert(converter()).value(ctx.resultSet().getString(ctx.index()));
    }

    @Override
    public void get(BindingGetStatementContext<Element> ctx) throws SQLException {
        ctx.convert(converter()).value(ctx.statement().getString(ctx.index()));
    }

    // The below methods aren't needed in PostgreSQL:

    @Override
    public void set(BindingSetSQLOutputContext<Element> ctx) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public void get(BindingGetSQLInputContext<Element> ctx) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }
}