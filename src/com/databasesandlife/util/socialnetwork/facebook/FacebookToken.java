package com.databasesandlife.util.socialnetwork.facebook;


import org.scribe.model.Token;

import com.databasesandlife.util.socialnetwork.OAuthClient;
import com.databasesandlife.util.socialnetwork.SocialClientFactory;
import com.databasesandlife.util.socialnetwork.SocialNetwork;
import com.databasesandlife.util.socialnetwork.SocialNetworkToken;

@SuppressWarnings("serial")
public class FacebookToken extends SocialNetworkToken {

    public FacebookToken(Token t) {
        super(t);
    }

    @Override
    public OAuthClient getClient(SocialClientFactory fac) {
        return fac.getFacebookClient();
    }

    @Override
    public SocialNetwork getSocialNetwork() {
        return SocialNetwork.Facebook;
    }

    @Override
    public String getScope() {
        return "";
    }

}
