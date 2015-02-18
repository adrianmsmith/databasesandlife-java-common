package com.databasesandlife.util.jdbc;

import java.util.List;

import com.databasesandlife.util.jdbc.DbTransaction.DbQueryResultSet;

public interface DbQueryable {

    DbQueryResultSet query(final String sql, final Object... args);
    DbQueryResultSet query(CharSequence sql, List<Object> args);

}
