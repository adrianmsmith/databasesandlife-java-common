package com.databasesandlife.util.socialnetwork.xing;


import org.scribe.model.Token;

import com.databasesandlife.util.socialnetwork.OAuthClient;
import com.databasesandlife.util.socialnetwork.SocialClientFactory;
import com.databasesandlife.util.socialnetwork.SocialNetwork;
import com.databasesandlife.util.socialnetwork.SocialNetworkToken;

@SuppressWarnings("serial")
public class XingToken extends SocialNetworkToken {

    public XingToken(Token t) {
        super(t);
    }

    @Override
    public OAuthClient getClient(SocialClientFactory fac) {
        return fac.getXingClient();
    }

    @Override
    public SocialNetwork getSocialNetwork() {
        return SocialNetwork.Xing;
    }

    @Override
    public String getScope() {
        return "";
    }
}
