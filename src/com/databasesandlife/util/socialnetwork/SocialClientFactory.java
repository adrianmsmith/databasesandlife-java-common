package com.databasesandlife.util.socialnetwork;

import com.databasesandlife.util.socialnetwork.facebook.FacebookClient;
import com.databasesandlife.util.socialnetwork.google.GoogleClient;
import com.databasesandlife.util.socialnetwork.linkedin.LinkedInClient;
import com.databasesandlife.util.socialnetwork.twitter.TwitterClient;
import com.databasesandlife.util.socialnetwork.xing.XingClient;

import java.io.Serializable;

public interface SocialClientFactory extends Serializable {

    public abstract XingClient getXingClient();
    public abstract LinkedInClient getLinkedInClient();
    public abstract FacebookClient getFacebookClient();
    public abstract GoogleClient getGoogleClient();
    public abstract TwitterClient getTwitterClient();

}
