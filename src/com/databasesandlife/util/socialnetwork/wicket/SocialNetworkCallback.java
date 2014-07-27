package com.databasesandlife.util.socialnetwork.wicket;

import java.io.Serializable;

import org.apache.wicket.PageReference;

import com.databasesandlife.util.socialnetwork.SocialNetworkToken;


/** 
 * Methods to be called when authorization is accepted or rejected by a social network.
 *    <p>
 * <b>NOTE:</b> Objects implementing this <b>should not be</b> (or reference) <b>pages</b>.
 * (As these objects are stored in pages, and pages should not reference other pages.)
 * If you need to reference a page, use {@link PageReference}. 
 */
public interface SocialNetworkCallback extends Serializable{

    public void onAuthentication(SocialNetworkToken networkToken);
    
    public void onFailure(Exception e);
}
