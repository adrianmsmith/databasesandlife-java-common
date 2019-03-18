package com.databasesandlife.util;

import com.databasesandlife.util.gwtsafe.ConfigurationException;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNURL;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents a URL, username, password.
 *
 * @author This source is copyright <a href="http://www.databasesandlife.com">Adrian Smith</a> and licensed under the LGPL 3.
 * @see <a href="https://github.com/adrianmsmith/databasesandlife-java-common">Project on GitHub</a>
 */
public class UrlWithUsernamePassword {

    public final @Nonnull URL url;
    public final @CheckForNull String username;
    public final @CheckForNull CleartextPassword password;

    /** @param pipeSeparated "url" or "url|username|password" */
    public UrlWithUsernamePassword(String pipeSeparated) throws ConfigurationException {
        Matcher m = Pattern.compile("^([^|]+?)(\\|([^|]+)\\|([^|]+))?$").matcher(pipeSeparated);
        if (! m.matches()) throw new ConfigurationException("'" + pipeSeparated + "' should have 'url|user|pw' form");
        try {
            url = new URL(m.group(1));
            username = m.group(3);
            password = Optional.ofNullable(m.group(4)).map(x -> new CleartextPassword(x)).orElse(null);
        }
        catch (MalformedURLException e) { throw new ConfigurationException("URL '" + m.group(1) + "' malformed: " + e.getMessage(), e); }
    }
}
