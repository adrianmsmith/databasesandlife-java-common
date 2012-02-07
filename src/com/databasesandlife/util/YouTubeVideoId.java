package com.databasesandlife.util;

import java.io.Serializable;

/** Represents the ID of a youtube Video */
public class YouTubeVideoId implements Serializable {
    
    public String id;
    
    public YouTubeVideoId(String i) { id=i; }

}
