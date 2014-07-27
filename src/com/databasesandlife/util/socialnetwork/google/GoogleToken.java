package com.databasesandlife.util.socialnetwork.google;


import org.scribe.model.Token;

import com.databasesandlife.util.socialnetwork.OAuthClient;
import com.databasesandlife.util.socialnetwork.SocialClientFactory;
import com.databasesandlife.util.socialnetwork.SocialNetwork;
import com.databasesandlife.util.socialnetwork.SocialNetworkToken;

@SuppressWarnings("serial")
public class GoogleToken extends SocialNetworkToken {

    public GoogleToken(Token t) {
        super(t);
    }

    @Override
    public OAuthClient getClient(SocialClientFactory fac) {
        return fac.getGoogleClient();
    }

    @Override
    public SocialNetwork getSocialNetwork() {
        return SocialNetwork.Google;
    }

    @Override
    public String getScope() {
        return "";
    }

}
