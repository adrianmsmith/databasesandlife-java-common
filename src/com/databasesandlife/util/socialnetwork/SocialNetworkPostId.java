package com.databasesandlife.util.socialnetwork;

import java.io.Serializable;

@SuppressWarnings("serial")
public class SocialNetworkPostId implements Serializable {

    private String id;

    public SocialNetworkPostId(long id) {
        this.id = String.valueOf(id);
    }

    public SocialNetworkPostId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

}
