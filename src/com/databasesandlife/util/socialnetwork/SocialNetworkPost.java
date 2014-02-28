package com.databasesandlife.util.socialnetwork;

import java.net.URL;

public class SocialNetworkPost {

    private SocialNetworkToken token;
    private String title;
    private String message;
    private String imageUrl;
    private String description;
    private URL link;
    private SocialNetworkPostId id;
    private SocialClientFactory fac;

    public SocialNetworkPost(SocialNetworkToken token, String title, String message, String imageUrl, String description, URL link, SocialClientFactory fac) {
        this.token = token;
        this.title = title;
        this.message = message;
        this.imageUrl = imageUrl;
        this.description = description;
        this.fac = fac;
        this.link = link;
    }

    /**
     * Posts the information to a social network.
     * @throws SocialNetworkUnavailableException
     */
    public SocialNetworkPostId post() throws SocialNetworkUnavailableException {
        SocialNetworkPostId id = token.getClient(fac).postToWall(token,title,message,imageUrl,description,link);
        this.id = id;
        return id;
    }

    /**
     * It "rollbacks" the post to the network. By rollback, actually a delete will
     * be executed. The post will very likely be visible on the social network for a short amount of time
     * @throws SocialNetworkUserException
     */
    public void rollback() throws SocialNetworkUserException {
        if (id == null) { return; }
        token.getClient(fac).deletePost(token,id);
    }

}
