package com.databasesandlife.util.socialnetwork;

import java.io.Serializable;

import com.databasesandlife.util.socialnetwork.facebook.FacebookUserId;
import com.databasesandlife.util.socialnetwork.google.GoogleContactId;
import com.databasesandlife.util.socialnetwork.linkedin.LinkedInUserId;
import com.databasesandlife.util.socialnetwork.xing.XingUserId;

/**
 * Represents the immutable external identity of a user on a social network, e.g. the Facebook User ID.
 * Can also represent external identities such as email addresses.
  */
@SuppressWarnings("serial")
public abstract class SocialUserExternalId implements Serializable{
    protected String id;
    public SocialUserExternalId(String id) {
        if (id == null || id.equals("")) throw new RuntimeException("id='" + id + "'");
        this.id = id; 
    }
    
    public static SocialUserExternalId newSocialUserExternalIdForSocialNetwork(SocialNetwork socialNetwork, String id){
        switch (socialNetwork) {
            case Facebook:
                return new FacebookUserId(id);
            case Google:
                return new GoogleContactId(id);
            case LinkedIn:
                return new LinkedInUserId(id);
            case Xing:
                return new XingUserId(id);
            default:
                throw new RuntimeException("SocialNetwork not supported: " + socialNetwork);
        }
    }
    
    public String getId() { return id; }
    @Override public String toString() { return id; }
    @Override public int hashCode() { return 383474 + getClass().hashCode() + id.hashCode(); }
    @Override public boolean equals(Object obj) { 
        return (obj != null) && obj.getClass().equals(getClass()) && ((SocialUserExternalId) obj).id.equals(id); }
    
    public abstract SocialNetwork getSocialNetwork();
}
