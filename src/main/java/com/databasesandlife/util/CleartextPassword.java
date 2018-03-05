package com.databasesandlife.util;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang.RandomStringUtils;

import javax.annotation.Nonnull;
import java.io.Serializable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static java.util.Arrays.asList;

/**
 * Wraps a string containing a cleartext password.
 * The purpose of this class is to increase type-safety over using a plain string.
 *
 * @author This source is copyright <a href="http://www.databasesandlife.com">Adrian Smith</a> and licensed under the LGPL 3.
 * @see <a href="https://github.com/adrianmsmith/databasesandlife-java-common">Project on GitHub</a>
 */
public class CleartextPassword implements Serializable {

    public final @Nonnull String cleartext;

    @JsonCreator
    public CleartextPassword(@Nonnull String c) {
        cleartext = c;
    }

    @JsonValue
    public @Nonnull String getCleartext() { return cleartext; }

    /**
     * Generates a random password.
     * <p>A-Z, a-z, 0-9 characters are used, with the exception confusing characters such as uppercase O and digit 0 are not used.</p>
     * <p>The objective is that:
     * <ul>
     *     <li>the passwords can be written down (thus confusing characters are removed)</li>
     *     <li>and also that the password can be used in situations where special encoding is needed for example GET requests
     *          (thus there are no special characters.)</li>
     * </ul></p>
     * <p>Make up for "lack of randomness" by making the password longer.</p>
     */
    public static @Nonnull CleartextPassword newRandom(int length) {
        Set<Character> chars = new HashSet<>();

        for (char c = 'A'; c <= 'Z'; c++) chars.add(c);
        for (char c = 'a'; c <= 'z'; c++) chars.add(c);
        for (char c = '0'; c <= '9'; c++) chars.add(c);

        chars.removeAll(asList('O', '0'));
        chars.removeAll(asList('I', 'l', '1'));
        chars.removeAll(asList('S', '5'));
        chars.removeAll(asList('2', 'Z'));

        StringBuilder result = new StringBuilder();
        for (char c : chars) result.append(c);

        String password = RandomStringUtils.random(length, result.toString());
        return new CleartextPassword(password);
    }

    /** Generates a new password of a reasonable length; see {@link #newRandom(int)} */
    public static @Nonnull CleartextPassword newRandom() {
        return newRandom(15);
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

    @Override
    public String toString() {
        return getClass().getSimpleName()+"("+cleartext+")";
    }
}
