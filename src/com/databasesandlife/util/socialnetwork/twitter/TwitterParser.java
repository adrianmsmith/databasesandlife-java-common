package com.databasesandlife.util.socialnetwork.twitter;

import com.databasesandlife.util.socialnetwork.PostId;
import com.databasesandlife.util.socialnetwork.SocialFriend;
import com.databasesandlife.util.socialnetwork.SocialNetworkUnavailableException;
import com.databasesandlife.util.socialnetwork.SocialNetworkUserException;
import com.databasesandlife.util.socialnetwork.SocialParser;
import com.databasesandlife.util.socialnetwork.SocialUser;
import com.google.gson.Gson;

import java.util.List;
import java.util.Map;

/**
 * Created by mlinzmayer on 12.02.14.
 */
public class TwitterParser extends SocialParser {

    @Override
    public List<SocialFriend<?>> getFriends(String json) throws SocialNetworkUnavailableException, SocialNetworkUserException {
        return null;
    }

    @Override
    public SocialUser<?> getUserInformation(String json) throws SocialNetworkUnavailableException, SocialNetworkUserException {
        return null;
    }

    protected PostId getPostId(String response) throws SocialNetworkUserException {
        Gson gson = new Gson();
        Map values = gson.fromJson(response,Map.class);
        return new PostId(((Double) values.get("id")).longValue());
    }

}
