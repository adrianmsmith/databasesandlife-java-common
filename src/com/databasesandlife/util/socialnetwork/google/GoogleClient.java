package com.databasesandlife.util.socialnetwork.google;

import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


import com.databasesandlife.util.socialnetwork.*;
import org.scribe.builder.ServiceBuilder;
import org.scribe.builder.api.GoogleApi;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;
import org.scribe.model.Token;
import org.scribe.model.Verb;
import org.scribe.model.Verifier;
import org.scribe.oauth.OAuthService;

import com.databasesandlife.util.socialnetwork.SocialNetworkPostId;

@SuppressWarnings("serial")
public class GoogleClient extends OAuthClient implements Serializable{

	 private static final String PROTECTED_RESOURCE_URL = "https://www.google.com/m8/feeds/contacts/default/full?max-results=100000";
	 private static final String SCOPE = "https://www.google.com/m8/feeds"; 
	
	public GoogleClient(String appId, String secret) {
		super(appId, secret);
		super.appID = appId;
		super.appSecret = secret;
	}

	public String getApiURL() {
		return PROTECTED_RESOURCE_URL;
	}

	@Override
	public SocialNetworkToken buildAccessToken(Token requestToken,String verifier){
		return new GoogleToken(getOAuthService("","").getAccessToken(requestToken, new Verifier(verifier)));
	}
	
	private String getInformations(Token accessToken){
		OAuthRequest request = new OAuthRequest(Verb.GET, PROTECTED_RESOURCE_URL);
	    getOAuthService("","").signRequest(accessToken, request);
	    request.addHeader("GData-Version", "3.0");
	    Response r = request.send();
	    return r.getBody();
	}

	@Override
	public Iterable<String> getFriendsInformation(Token accessToken, String scope)
			throws SocialNetworkUnavailableException {
		List<String> a = new ArrayList<String>();
		a.add(getInformations(accessToken));
		return a;
	}

	@Override
	public String getUserInformation(Token accessToken, String scope)
			throws SocialNetworkUnavailableException {
		return getInformations(accessToken);
	}

	@Override
	public OAuthService getOAuthService(String scope, String callback) {
		return new ServiceBuilder()
        .provider(GoogleApi.class)
        .apiKey(appID)
        .apiSecret(appSecret)
        .scope(SCOPE)
        .callback(callback)
        .build();
	}

	@Override
	public String getAuthenticationURL(SocialNetworkRequestTokenDatabase tdb, String callback, String... scope) {
		Token requestToken = getOAuthService("", callback).getRequestToken();
		tdb.setGoogleRequestToken(requestToken);
		return getOAuthService("", callback).getAuthorizationUrl(requestToken);
	}

	@Override
	public String getAuthenticationUrlWithCallbackForPage(SocialNetworkRequestTokenDatabase tdb, URL page, String... scope) {
		return getAuthenticationURL(tdb, page+"&network=google", "");
	}

	@Override
	public SocialParser getParser() {
		return new GoogleParser();
	}

	@Override
	public SocialNetworkPostId postToWall(SocialNetworkToken token, String message, String title,
                             String imageUrl, String description, URL link) throws SocialNetworkUnavailableException{
		throw new RuntimeException("Method not supported by google");
	}
	
	@Override
	public String[] getScopeForConnection() { return new String[0];}

	@Override
	public String getFieldsForUserInformation() {
		return "";
	}

    @Override
    public void deletePost(SocialNetworkToken token, SocialNetworkPostId id) throws SocialNetworkUserException {
        throw new RuntimeException("Delete Post for Google is currently not supported");
    }
}
