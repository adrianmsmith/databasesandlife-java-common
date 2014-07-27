package com.databasesandlife.util.socialnetwork.linkedin;

import com.databasesandlife.util.socialnetwork.SocialNetwork;
import com.databasesandlife.util.socialnetwork.SocialUserExternalId;

/** The ID of a Linked In user */
@SuppressWarnings("serial")
public class LinkedInUserId extends SocialUserExternalId {
    public LinkedInUserId(String x) { super(x); }

    @Override
    public SocialNetwork getSocialNetwork() {
        return SocialNetwork.LinkedIn;
    }
}
