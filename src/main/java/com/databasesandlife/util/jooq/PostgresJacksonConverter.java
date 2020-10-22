package com.databasesandlife.util.jooq;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.NullNode;
import org.jooq.Converter;
import org.jooq.JSONB;

import java.io.IOException;

/**
 * Allows PostgreSQL JSON columns to be mapped to/from Java Jackson {@link JsonNode} objects.
 * For jOOQ 3.12 upwards.
 * 
 * @see <a href="https://stackoverflow.com/a/27146852/220627">StackOverflow Question</a>
 */
public class PostgresJacksonConverter implements Converter<JSONB, JsonNode> {  // db, java

    @Override public Class<JSONB> fromType() {
        return JSONB.class;
    }

    @Override public Class<JsonNode> toType() {
        return JsonNode.class;
    }

    @Override
    public JsonNode from(JSONB db) {
        try {
            if (db == null) return null;
            else return new ObjectMapper().readTree(db.data());
        } 
        catch (IOException e) { throw new RuntimeException(e); }
    }

    @Override
    public JSONB to(JsonNode u) {
        try {
            if (u == null || u.equals(NullNode.instance)) return null;
            else return JSONB.valueOf(new ObjectMapper().writeValueAsString(u));
        } 
        catch (IOException e) { throw new RuntimeException(e); }
    }
}