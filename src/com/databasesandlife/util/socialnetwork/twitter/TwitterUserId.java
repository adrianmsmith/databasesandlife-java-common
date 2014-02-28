package com.databasesandlife.util.socialnetwork.twitter;

import com.databasesandlife.util.socialnetwork.SocialNetwork;
import com.databasesandlife.util.socialnetwork.SocialUserExternalId;

public class TwitterUserId extends SocialUserExternalId {

    public TwitterUserId(String id) {
        super(id);
    }

    @Override
    public SocialNetwork getSocialNetwork() {
        return SocialNetwork.Twitter;
    }
}
