package com.databasesandlife.util;

import java.io.Serializable;

import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNURL;

/**
 * Represents a SVN URL, username, password.
 */
public class SvnUrlWithUsernamePassword implements Serializable {

    public SVNURL url;
    public String username, password;

    public static class MalformedException extends RuntimeException {
        MalformedException(String arg) { super(arg); }
    }

    /** @param pipeSeparated "url|username|password" */
    public static SvnUrlWithUsernamePassword parse(String pipeSeparated) throws MalformedException {
        String[] parts = pipeSeparated.split("\\|");
        if (parts.length != 3) throw new MalformedException("SVN '" + pipeSeparated + "' should have 'url|user|pw' form");

        try {
            SvnUrlWithUsernamePassword result = new SvnUrlWithUsernamePassword();
            result.url = SVNURL.parseURIEncoded(parts[0]);
            result.username = parts[1];
            result.password = parts[2];
            return result;
        }
        catch (SVNException e) { throw new MalformedException("SVN URL '" + parts[0] + "' malformed: " + e.getMessage()); }
    }
}
