package com.databasesandlife.util.socialnetwork.linkedin;

import org.scribe.builder.api.DefaultApi20;
import org.scribe.model.OAuthConfig;
import org.scribe.model.Verb;
import org.scribe.utils.OAuthEncoder;

import com.databasesandlife.util.MD5Hex;

public class LinkedIn2Api extends DefaultApi20 {

	private static final String AUTHORIZE_URL = "https://www.linkedin.com/uas/oauth2/authorization?response_type=code"+
                                           "&client_id=%s&state=%s&redirect_uri=%s";
	private static final String SCOPED_AUTHORIZE_URL = AUTHORIZE_URL+ "&scope=%s";
	
	@Override
    public String getAuthorizationUrl(OAuthConfig config)
    {
		// Append scope if present
	    if(config.hasScope())
	    {
	     return String.format(SCOPED_AUTHORIZE_URL, OAuthEncoder.encode(config.getApiKey()),OAuthEncoder.encode(MD5Hex.md5(config.getCallback()+config.getApiSecret())),OAuthEncoder.encode(config.getCallback()), OAuthEncoder.encode(config.getScope()));
	    }
	    else
	    {
	      return String.format(AUTHORIZE_URL, OAuthEncoder.encode(config.getApiKey()),OAuthEncoder.encode(MD5Hex.md5(config.getCallback()+config.getApiSecret())),OAuthEncoder.encode(config.getCallback()));
	    }
    }

	@Override
	public String getAccessTokenEndpoint() {
		return "https://www.linkedin.com/uas/oauth2/authorization?grant_type=authorization_code";
//		?response_type=code&client_id=YOUR_API_KEY&scope=SCOPE&state=STATE"+
//	            "&redirect_uri=YOUR_REDIRECT_URI";
	}
	
	@Override
	public Verb getAccessTokenVerb(){
		return Verb.POST;
	}
	
}
