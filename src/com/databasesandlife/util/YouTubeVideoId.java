package com.databasesandlife.util;

import java.io.Serializable;

/** 
 * Represents the ID of a youtube Video 
 *
 * @author The Java source is copyright <a href="http://www.databasesandlife.com">Adrian Smith</a> and licensed under the LGPL 3.
 * @version $Revision$
 */
@SuppressWarnings("serial")
public class YouTubeVideoId implements Serializable {
    
    public String id;
    
    public YouTubeVideoId(String i) { id=i; }

}
