package com.databasesandlife.util.socialnetwork;

import org.scribe.model.Token;

/**
 * We have to store these tokens because they are needed to
 * create the final access token. However, with every request they get overwritten
 * by new ones, so they should not do any harm.
 */
public interface SocialNetworkRequestTokenDatabase {

    public Token getXingRequestToken();
    public void setXingRequestToken(Token t);
    
    public Token getGoogleRequestToken();
    public void setGoogleRequestToken(Token t);
}
