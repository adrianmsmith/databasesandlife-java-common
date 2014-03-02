package com.databasesandlife.util.socialnetwork.twitter;

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;


import com.databasesandlife.util.socialnetwork.*;
import org.apache.wicket.protocol.http.WebSession;
import org.scribe.builder.ServiceBuilder;
import org.scribe.builder.api.TwitterApi;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;
import org.scribe.model.Token;
import org.scribe.model.Verb;
import org.scribe.model.Verifier;
import org.scribe.oauth.OAuthService;

import com.databasesandlife.util.socialnetwork.SocialNetworkPostId;

public class TwitterClient extends OAuthClient {

    private final String tweetUrl = "https://api.twitter.com/1.1/statuses/update.json";
    private final String oauthUrl = "https://api.twitter.com/1.1/account/verify_credentials.json";
    private final String removeUrl = "https://api.twitter.com/1.1/statuses/destroy/ID.json";

    public TwitterClient(String appID, String appSecret) {
        super(appID, appSecret);
    }

    @Override
    public SocialNetworkPostId postToWall(SocialNetworkToken token, String title, String message, String imageUrl, String description, URL link) throws SocialNetworkUnavailableException {
        try {
            OAuthRequest post = new OAuthRequest(Verb.POST,tweetUrl);
            post.addBodyParameter("status", URLEncoder.encode(message, "UTF-8").replace("+", " "));
            getOAuthService("","").signRequest(token.getAccessToken(), post);
            Response response = post.send();
            return new TwitterParser().getPostId(response.getBody());
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        } catch (SocialNetworkUserException e) {
            throw new RuntimeException(e);
        }
    }

    public void deletePost(SocialNetworkToken token, SocialNetworkPostId id) throws SocialNetworkUserException {
        OAuthRequest remove = new OAuthRequest(Verb.POST,removeUrl.replace("ID",id.getId()));
        getOAuthService("","").signRequest(token.getAccessToken(),remove);
        Response r = remove.send();
        System.out.println(r.getBody());
    }

    @Override
    public String[] getScopeForConnection() {
        return new String[0];
    }

    @Override
    public OAuthService getOAuthService(String scope, String callback) {
        OAuthService service = new ServiceBuilder()
                .provider(TwitterApi.SSL.class)
                .apiKey(appID)
                .apiSecret(appSecret)
                .callback(callback)
                .build();
        return service;
    }

    @Override
    public String getAuthenticationURL(SocialNetworkRequestTokenDatabase tdb, String callback, String... scope) throws SocialNetworkUnavailableException {
        //twitter has no scope, therefore no need to pass a value
        Token request = getOAuthService("", callback).getRequestToken();
        SocialNetworkRequestTokenDatabase ndb = ((SocialNetworkRequestTokenDatabase) WebSession.get());
        ndb.setTwitterRequestToken(request);
        return getOAuthService("",callback).getAuthorizationUrl(request);
    }

    @Override
    public String getAuthenticationUrlWithCallbackForPage(SocialNetworkRequestTokenDatabase tdb, URL url, String... scope) throws SocialNetworkUnavailableException {
        return getAuthenticationURL(tdb, url.toExternalForm()+"&network=twitter", scope);
    }

    @Override
    public Iterable<String> getFriendsInformation(Token accessToken, String scope) throws SocialNetworkUnavailableException {
        return null;
    }

    @Override
    public String getUserInformation(Token accessToken, String scope) throws SocialNetworkUnavailableException {
        return getInformations(accessToken,scope).getBody();
    }

    @Override
    public String getApiURL() {
        return oauthUrl;
    }

    @Override
    public SocialNetworkToken buildAccessToken(Token requestToken, String verifier) {
        Verifier v = new Verifier(verifier);
        Token accessToken = getOAuthService("","").getAccessToken(requestToken,v);
        return new TwitterToken(accessToken);
    }

    @Override
    public SocialParser getParser() {
        return new TwitterParser();
    }

    @Override
    public String getFieldsForUserInformation() {
        return null;
    }
}
