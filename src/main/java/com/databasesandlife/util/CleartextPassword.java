package com.databasesandlife.util;

import javax.annotation.Nonnull;

/**
 * Wraps a string containing a cleartext password.
 * The purpose of this class is to increase type-safety over using a plain string.
 *
 * @author This source is copyright <a href="http://www.databasesandlife.com">Adrian Smith</a> and licensed under the LGPL 3.
 * @see <a href="https://github.com/adrianmsmith/databasesandlife-java-common">Project on GitHub</a>
 */
public class CleartextPassword {

    public final @Nonnull String cleartext;

    public CleartextPassword(@Nonnull String c) {
        cleartext = c;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || !getClass().equals(o.getClass())) return false;

        CleartextPassword that = (CleartextPassword) o;
        return cleartext.equals(that.cleartext);
    }

    @Override
    public int hashCode() {
        return 3458 + cleartext.hashCode();
    }
}
