package com.databasesandlife.util.socialnetwork;

import java.io.Serializable;

@SuppressWarnings("serial")
public class PostId implements Serializable {

    private String id;

    public PostId(long id) {
        this.id = String.valueOf(id);
    }

    public PostId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

}
