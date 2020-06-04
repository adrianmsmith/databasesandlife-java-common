package com.databasesandlife.util;

import com.databasesandlife.util.gwtsafe.CleartextPassword;
import org.mindrot.jbcrypt.BCrypt;

import javax.annotation.Nonnull;
import java.io.Serializable;

/**
 * Wraps a string containing a BCrypt encrypted password.
 * The purpose of this class is to increase type-safety over using a plain string.
 *
 * @author This source is copyright <a href="http://www.databasesandlife.com">Adrian Smith</a> and licensed under the LGPL 3.
 * @see <a href="https://github.com/adrianmsmith/databasesandlife-java-common">Project on GitHub</a>
 */
public class BCryptPassword implements Serializable {

    public final @Nonnull String bcrypt;

    public BCryptPassword(@Nonnull String b) {
        bcrypt = b;
    }

    public BCryptPassword(@Nonnull CleartextPassword b) {
        bcrypt = BCrypt.hashpw(b.getCleartext(), BCrypt.gensalt());
    }

    public boolean is(@Nonnull CleartextPassword b) {
        return BCrypt.checkpw(b.getCleartext(), bcrypt);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || !getClass().equals(o.getClass())) return false;

        BCryptPassword that = (BCryptPassword) o;
        return bcrypt.equals(that.bcrypt);
    }

    @Override
    public int hashCode() {
        return 3452356 + bcrypt.hashCode();
    }
}
