package com.databasesandlife.util;

import java.io.Serializable;

import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNURL;

import com.databasesandlife.util.gwtsafe.ConfigurationException;

/**
 * Represents a SVN URL, username, password.
 *
 * @author The Java source is copyright <a href="http://www.databasesandlife.com">Adrian Smith</a> and licensed under the LGPL 3.
 * @version $Revision$
 */
@SuppressWarnings("serial")
public class SvnUrlWithUsernamePassword implements Serializable {

    public SVNURL url;
    public String username, password;

    /** @param pipeSeparated "url|username|password" */
    public static SvnUrlWithUsernamePassword parse(String pipeSeparated) throws ConfigurationException {
        String[] parts = pipeSeparated.split("\\|");
        if (parts.length != 3) throw new ConfigurationException("SVN '" + pipeSeparated + "' should have 'url|user|pw' form");

        try {
            SvnUrlWithUsernamePassword result = new SvnUrlWithUsernamePassword();
            result.url = SVNURL.parseURIEncoded(parts[0]);
            result.username = parts[1];
            result.password = parts[2];
            return result;
        }
        catch (SVNException e) { throw new ConfigurationException("SVN URL '" + parts[0] + "' malformed: " + e.getMessage()); }
    }
}
