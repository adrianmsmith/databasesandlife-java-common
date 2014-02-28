package com.databasesandlife.util.socialnetwork.twitter;

import com.databasesandlife.util.socialnetwork.*;
import com.databasesandlife.util.socialnetwork.SocialNetworkPostId;
import com.google.gson.Gson;

import java.util.List;
import java.util.Map;

public class TwitterParser extends SocialParser {

    @Override
    public List<SocialFriend<?>> getFriends(String json) throws SocialNetworkUnavailableException, SocialNetworkUserException {
        return null;
    }

    @Override
    public SocialUser<?> getUserInformation(String json) throws SocialNetworkUnavailableException, SocialNetworkUserException {
        Gson gson = new Gson();
        Map values = gson.fromJson(json, Map.class);
        TwitterUser user = new TwitterUser();
        class TwitterSocialUserInit extends SocialUser<TwitterUserId> {
            public TwitterSocialUserInit(Map data) throws SocialNetworkUserException {
                id = new TwitterUserId((String) data.get("id_str"));
                username = (String) data.get("screen_name");
            }
            @Override public Work[] getWork() { return new Work[0]; }
            @Override public School[] getEducation() { return new School[0]; }
        }
        return new TwitterSocialUserInit(values);
    }

    protected SocialNetworkPostId getPostId(String response) throws SocialNetworkUserException {
        System.out.println(response);
        Gson gson = new Gson();
        Map values = gson.fromJson(response,Map.class);
        return new SocialNetworkPostId(((Double) values.get("id")).longValue());
    }

}
