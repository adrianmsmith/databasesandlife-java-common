package com.databasesandlife.util.socialnetwork.twitter;


import org.scribe.model.Token;

import com.databasesandlife.util.socialnetwork.OAuthClient;
import com.databasesandlife.util.socialnetwork.SocialClientFactory;
import com.databasesandlife.util.socialnetwork.SocialNetwork;
import com.databasesandlife.util.socialnetwork.SocialNetworkToken;

@SuppressWarnings("serial")
public class TwitterToken extends SocialNetworkToken {

    public TwitterToken(Token t) {
        super(t);
    }

    @Override
    public OAuthClient getClient(SocialClientFactory fac) {
        return fac.getTwitterClient();
    }

    @Override
    public SocialNetwork getSocialNetwork() {
        return SocialNetwork.Twitter;
    }

    @Override
    public String getScope() {
        return "";
    }
}
