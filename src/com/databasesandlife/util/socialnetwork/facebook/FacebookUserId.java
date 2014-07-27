package com.databasesandlife.util.socialnetwork.facebook;

import com.databasesandlife.util.socialnetwork.SocialNetwork;
import com.databasesandlife.util.socialnetwork.SocialUserExternalId;

/** The ID of a Facebook user */
@SuppressWarnings("serial")
public class FacebookUserId extends SocialUserExternalId {
    public FacebookUserId(String x) { super(x); }

    @Override
    public SocialNetwork getSocialNetwork() {
        return SocialNetwork.Facebook;
    }
}
