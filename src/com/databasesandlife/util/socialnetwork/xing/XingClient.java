package com.databasesandlife.util.socialnetwork.xing;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


import com.databasesandlife.util.socialnetwork.*;
import org.scribe.builder.ServiceBuilder;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;
import org.scribe.model.Token;
import org.scribe.model.Verb;
import org.scribe.model.Verifier;
import org.scribe.oauth.OAuthService;

import com.databasesandlife.util.CompositeIterable;
import com.databasesandlife.util.socialnetwork.SocialNetworkPostId;
import com.google.gson.Gson;

@SuppressWarnings("serial")
public class XingClient extends OAuthClient{

    private final String xingUrl = "https://api.xing.com/v1/";
    private final String XING_FRIEND_SCOPE = "users/me/contacts?user_fields=id,display_name,photo_urls,professional_experience";
    
    public XingClient(String appId,String secret){
        super(appId,secret);
    }

    @Override
    public SocialNetworkToken buildAccessToken(Token requestToken,String verifier){
        return new XingToken(getOAuthService("","").getAccessToken(requestToken, new Verifier(verifier)));
    }
    
    /**
     * Used to get informations from the XING social network
     * The accessToken must be a verfied access token which you can achieve
     * by calling the {@link XingClient#buildAccessToken(Token, String)} method
     * with the request Token and the received verifier code<br>
     * With the <b>scope</b> one can specify the information he wants to receive. To
     * get informations about the current user pass "users/me" as scope<br>
     * for more information about the XING scope please visit <a href="https://dev.xing.com/docs/resources">XING</a>
     * @param accessToken the verified access token
     * @param scope the scope for the information you want to get
     * @return
     */
    @Override
    public String getUserInformation(Token accessToken,String scope)  throws SocialNetworkUnavailableException{
        Response r = getInformations(accessToken, scope.isEmpty() ? "users/me" : scope);
//      if(r.getCode() == 403) throw new SocialNetworkTryLaterException("RATE_LIMIT_EXCEEDED");
        return r.getBody();
    }
    
    public String sendMessage(Token accessToken,String recipient,String subject,String message){
        OAuthRequest sendMessage = new OAuthRequest(Verb.POST,getApiURL()+"users/me/conversations");
        sendMessage.addBodyParameter("subject", subject);
        sendMessage.addBodyParameter("content", message);
        sendMessage.addBodyParameter("recipient_ids",recipient);
        getOAuthService("","").signRequest(accessToken, sendMessage);
        Response r = sendMessage.send();
        return r.getBody();
    }
    
    @Override
    public String getApiURL() {
        return xingUrl;
    }
    
    @Override
    public SocialNetworkPostId postToWall(SocialNetworkToken token, String title, String message, String imageUrl, String description, URL link) throws SocialNetworkUnavailableException{
        OAuthRequest post = new OAuthRequest(Verb.POST,getApiURL()+"users/me/status_message");
        post.addBodyParameter("message", message + "\n" + link.toExternalForm());
        getOAuthService("","").signRequest(token.getAccessToken(), post);
        post.send();
        return new SocialNetworkPostId(0);
    }

    public OAuthService getOAuthService(String scope,String callback){
        return new ServiceBuilder()
        .provider(XingApi.class)
        .apiKey(appID)
        .apiSecret(appSecret)
        .callback(callback)
        .build();
    }

    @Override
    public String getAuthenticationURL(SocialNetworkRequestTokenDatabase tdb, String callback,String... scope)
    throws SocialNetworkUnavailableException{
        try{
            Token token = getOAuthService("",callback).getRequestToken();
            tdb.setXingRequestToken(token);
            return getOAuthService("",callback).getAuthorizationUrl(token); 
        }catch(Exception e){
            throw new SocialNetworkUnavailableException(e);
        }
    }

    @Override
    public String getAuthenticationUrlWithCallbackForPage(SocialNetworkRequestTokenDatabase tdb, URL page, String... scope) 
    throws SocialNetworkUnavailableException{
        return getAuthenticationURL(tdb, page+"&network=xing", scope);
    }
    
    @Override
    public Iterable<String> getFriendsInformation(Token accessToken, String scope)  throws SocialNetworkUnavailableException{
        int amount = getFriendsAmount(accessToken);
        List<Iterable<String>> requests = new ArrayList<Iterable<String>>();
        for(int i = 0;i<(amount/100)+1;i++){
            final SocialNetworkRequestThread requestThread = new SocialNetworkRequestThread(this, accessToken,
                    scope.isEmpty() ? XING_FRIEND_SCOPE+"&limit=100&offset="+(100*i): scope);
            requests.add(createFuture(requestThread));
        }
        return new CompositeIterable<String>(requests);
    }
    
    @SuppressWarnings("unchecked")
    private int getFriendsAmount(Token accessToken) throws SocialNetworkUnavailableException{
        try{
            String amount = getInformations(accessToken, XING_FRIEND_SCOPE+"&limit=0").getBody();
            Gson gson = new Gson();
            Map<String,?> m = gson.fromJson(amount, Map.class);
            return (int) Double.parseDouble(((Map<String,?>)m.get("contacts")).get("total").toString());
        }catch(Exception e){
            throw new SocialNetworkUnavailableException(e);
        }
    }

    @Override
    public SocialParser getParser() {
        return new XingParser();
    }

    @Override
    public String[] getScopeForConnection() { return new String[0];}

    @Override
    public String getFieldsForUserInformation() {
        return "";
    }

    @Override
    public void deletePost(SocialNetworkToken token, SocialNetworkPostId id) throws SocialNetworkUserException {
        throw new RuntimeException("Delete Post for Xing is currently not supported");
    }
}
