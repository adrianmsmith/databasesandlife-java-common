package com.databasesandlife.util.socialnetwork.xing;

import com.databasesandlife.util.socialnetwork.SocialFriend;

@SuppressWarnings("serial")
public class XingFriend extends SocialFriend<XingUserId> {

    @Override
    public String prettyPrint() {
        return id + " " + getName();
    }
}
