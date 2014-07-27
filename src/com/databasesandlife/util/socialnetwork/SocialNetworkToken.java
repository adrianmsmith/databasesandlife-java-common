package com.databasesandlife.util.socialnetwork;

import java.io.Serializable;

import org.scribe.model.Token;

import com.databasesandlife.util.socialnetwork.facebook.FacebookToken;
import com.databasesandlife.util.socialnetwork.google.GoogleToken;
import com.databasesandlife.util.socialnetwork.linkedin.LinkedInToken;
import com.databasesandlife.util.socialnetwork.twitter.TwitterToken;
import com.databasesandlife.util.socialnetwork.xing.XingToken;

/**
 *  represents the access token of the social network 
 */
@SuppressWarnings("serial")
public abstract class SocialNetworkToken implements Serializable {

    protected Token accessToken;
    
    public SocialNetworkToken(Token t){
        this.accessToken = t;
    }
    
    public static SocialNetworkToken newSocialNetworkToken(SocialNetwork socialNetwork, Token token){
        switch (socialNetwork) {
            case Facebook:
                return new FacebookToken(token);
            case Google:
                return new GoogleToken(token);
            case LinkedIn:
                return new LinkedInToken(token);
            case Xing:
                return new XingToken(token);
            case Twitter:
                return new TwitterToken(token);
            default:
                throw new RuntimeException("SocialNetwork not supported: " + socialNetwork);
        }
    }
    
    public Token getAccessToken(){
        return accessToken;
    }
    
    public abstract OAuthClient getClient(SocialClientFactory fac);
    
    public abstract SocialNetwork getSocialNetwork();
    
    public abstract String getScope();
}
