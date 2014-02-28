package com.databasesandlife.util.socialnetwork.twitter;

import com.databasesandlife.util.socialnetwork.School;
import com.databasesandlife.util.socialnetwork.SocialUser;
import com.databasesandlife.util.socialnetwork.Work;

public class TwitterUser extends SocialUser<TwitterUserId> {

    @Override
    public Work[] getWork() {
        return new Work[0];
    }

    @Override
    public School[] getEducation() {
        return new School[0];
    }
}
