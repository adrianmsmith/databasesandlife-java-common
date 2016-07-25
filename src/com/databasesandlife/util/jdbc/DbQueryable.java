package com.databasesandlife.util.jdbc;

import java.util.List;

import com.databasesandlife.util.jdbc.DbTransaction.DbQueryResultSet;

/**
 * @author This source is copyright <a href="http://www.databasesandlife.com">Adrian Smith</a> and licensed under the LGPL 3.
 * @see <a href="https://github.com/adrianmsmith/databasesandlife-java-common">Project on GitHub</a>
 */
public interface DbQueryable {

    DbQueryResultSet query(final String sql, final Object... args);
    DbQueryResultSet query(CharSequence sql, List<Object> args);

}
