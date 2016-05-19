package com.databasesandlife.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNURL;

import com.databasesandlife.util.gwtsafe.ConfigurationException;

/**
 * Represents a SVN URL, username, password.
 *
 * @author The Java source is copyright <a href="http://www.databasesandlife.com">Adrian Smith</a> and licensed under the LGPL 3.
 * @version $Revision$
 */
public class SvnUrlWithUsernamePassword {

    public SVNURL url;
    public String username, password;

    /** @param pipeSeparated "url" or "url|username|password" */
    public static SvnUrlWithUsernamePassword parse(String pipeSeparated) throws ConfigurationException {
        Matcher m = Pattern.compile("^([^|]+?)(\\|([^|]+)\\|([^|]+))?$").matcher(pipeSeparated);
        if (! m.matches()) throw new ConfigurationException("SVN '" + pipeSeparated + "' should have 'url|user|pw' form");
        try {
            SvnUrlWithUsernamePassword result = new SvnUrlWithUsernamePassword();
            result.url = SVNURL.parseURIEncoded(m.group(1));
            result.username = m.group(3);
            result.password = m.group(4);
            return result;
        }
        catch (SVNException e) { throw new ConfigurationException("SVN URL '" + m.group(1) + "' malformed: " + e.getMessage(), e); }
    }
}
