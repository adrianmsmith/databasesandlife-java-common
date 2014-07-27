package com.databasesandlife.util.socialnetwork.google;

import com.databasesandlife.util.socialnetwork.SocialNetwork;
import com.databasesandlife.util.socialnetwork.SocialUserExternalId;

@SuppressWarnings("serial")
public class GoogleContactId extends SocialUserExternalId {
    public GoogleContactId(String x) { super(x); }

    @Override
    public SocialNetwork getSocialNetwork() {
        return SocialNetwork.Google;
    }
    
}
