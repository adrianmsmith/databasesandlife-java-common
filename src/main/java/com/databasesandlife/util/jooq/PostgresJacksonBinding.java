package com.databasesandlife.util.jooq;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.NullNode;
import org.jooq.*;
import org.jooq.conf.ParamType;
import org.jooq.impl.DSL;

import java.io.IOException;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.Types;
import java.util.Objects;

/**
 * Binding to allow PostgreSQL JSON columns to be mapped to/from Java Jackson {@link JsonNode} objects.
 * 
 * @see <a href="https://stackoverflow.com/a/27146852/220627">StackOverflow Question</a>
 */
public class PostgresJacksonBinding
implements Binding<Object, JsonNode> {

    @Override
    public Converter<Object, JsonNode> converter() {
        return new Converter<Object, JsonNode>() {
            @Override public Class<Object> fromType() {
                return Object.class; 
            }
            
            @Override public Class<JsonNode> toType() { 
                return JsonNode.class;
            }
            
            @Override public JsonNode from(Object t) {
                try {
                    return t == null ? NullNode.instance : new ObjectMapper().readTree(t + "");
                }
                catch (IOException e) { throw new RuntimeException(e); }
            }
            
            @Override public Object to(JsonNode u) {
                try {
                    return u == null || u.equals(NullNode.instance) ? null : new ObjectMapper().writeValueAsString(u);
                }
                catch (IOException e) { throw new RuntimeException(e); }
            }
        };
    }

    @Override
    public void sql(BindingSQLContext<JsonNode> ctx) throws SQLException {
        // Depending on how you generate your SQL, you may need to explicitly distinguish
        // between jOOQ generating bind variables or inlined literals.
        if (ctx.render().paramType() == ParamType.INLINED)
            ctx.render().visit(DSL.inline(ctx.convert(converter()).value())).sql("::json");
        else
            ctx.render().sql("?::json");
    }

    @Override
    public void register(BindingRegisterContext<JsonNode> ctx) throws SQLException {
        ctx.statement().registerOutParameter(ctx.index(), Types.VARCHAR);
    }

    @Override
    public void set(BindingSetStatementContext<JsonNode> ctx) throws SQLException {
        ctx.statement().setString(
            ctx.index(), 
            Objects.toString(ctx.convert(converter()).value()));
    }

    @Override
    public void get(BindingGetResultSetContext<JsonNode> ctx) throws SQLException {
        ctx.convert(converter()).value(ctx.resultSet().getString(ctx.index()));
    }

    @Override
    public void get(BindingGetStatementContext<JsonNode> ctx) throws SQLException {
        ctx.convert(converter()).value(ctx.statement().getString(ctx.index()));
    }

    // The below methods aren't needed in PostgreSQL:

    @Override
    public void set(BindingSetSQLOutputContext<JsonNode> ctx) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public void get(BindingGetSQLInputContext<JsonNode> ctx) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }
}