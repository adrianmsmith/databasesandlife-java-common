package com.databasesandlife.util.socialnetwork.linkedin;


import org.scribe.model.Token;

import com.databasesandlife.util.socialnetwork.OAuthClient;
import com.databasesandlife.util.socialnetwork.SocialClientFactory;
import com.databasesandlife.util.socialnetwork.SocialNetwork;
import com.databasesandlife.util.socialnetwork.SocialNetworkToken;

@SuppressWarnings("serial")
public class LinkedInToken extends SocialNetworkToken {

	private static final String SCOPE = "~:(id,first-name,last-name,industry,skills,educations,certifications,email-address,positions,picture-url,phone-numbers,languages,main-address,site-standard-profile-request,public-profile-url,picture-urls::(original))";
	
	public LinkedInToken(Token t) {
		super(t);
	}

	@Override
	public OAuthClient getClient(SocialClientFactory fac) {
		return fac.getLinkedInClient();
	}

	@Override
	public SocialNetwork getSocialNetwork() {
	    return SocialNetwork.LinkedIn;
	}

	@Override
	public String getScope() {
		return SCOPE;
	}
}
