package com.databasesandlife.util.socialnetwork.facebook;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;


import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.SASLAuthentication;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.scribe.builder.ServiceBuilder;
import org.scribe.builder.api.FacebookApi;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;
import org.scribe.model.Token;
import org.scribe.model.Verb;
import org.scribe.model.Verifier;
import org.scribe.oauth.OAuthService;

import com.databasesandlife.util.socialnetwork.OAuthClient;
import com.databasesandlife.util.socialnetwork.PostId;
import com.databasesandlife.util.socialnetwork.SocialNetworkRequestTokenDatabase;
import com.databasesandlife.util.socialnetwork.SocialNetworkToken;
import com.databasesandlife.util.socialnetwork.SocialNetworkUnavailableException;
import com.databasesandlife.util.socialnetwork.SocialNetworkUserException;
import com.databasesandlife.util.socialnetwork.SocialParser;

@SuppressWarnings("serial")
public class FacebookClient extends OAuthClient {
	
	public FacebookClient(String appID, String appSecret) {
		super(appID, appSecret);
	}
	
	private final String graphUrl = "https://graph.facebook.com/";
	
	private final String FACEBOOK_FRIENDS_FIELDS = "friends.fields(education,work,first_name,last_name)";

	
	/**
	 * Posts something on your facebook wall<br>
	 * <br>
	 * for a list of keys see <a href="http://developers.facebook.com/docs/reference/api/post/">Facebook API</a>
	 * @return true if successful otherwise false
	 */
	public PostId postToWall(SocialNetworkToken token, String title, String message, String imageUrl, String description, URL link) throws SocialNetworkUnavailableException{
        try {
            OAuthRequest request = new OAuthRequest(Verb.POST,graphUrl+"me/feed");
            if(message != null && !message.isEmpty()) request.addBodyParameter("message",message);
            if(imageUrl != null && !imageUrl.isEmpty()) request.addBodyParameter("picture", imageUrl);
            if(link != null) request.addBodyParameter("link", link.toExternalForm());
            if(description != null && !description.isEmpty()) request.addBodyParameter("description",description);
            if(title != null && !title.isEmpty()) request.addBodyParameter("name", title);
            getOAuthService("","").signRequest(token.getAccessToken(), request);
            Response r = request.send();
            return new FacebookParser().getPostId(r.getBody());
        } catch (SocialNetworkUserException e) {
            throw new RuntimeException(e);
        }
    }

	@Override
	public String getUserInformation(Token accessToken,String fields) throws SocialNetworkUnavailableException{
		return getInformations(accessToken, fields.isEmpty() ? "/me/" : "/me/?fields=" + fields).getBody();
	}
	
	@Override
	public Iterable<String> getFriendsInformation(Token accessToken,String fields) throws SocialNetworkUnavailableException{
		List<String> a = new ArrayList<String>();
		a.add(getInformations(accessToken, fields.isEmpty() ? "/me/?fields="+FACEBOOK_FRIENDS_FIELDS : fields).getBody());
		return a;
	}
	
	//100000418154222 my fb id
	public void sendMessage(Token accessToken,String recipientId,String subject,String message) throws SocialNetworkUnavailableException{
		try{
			ConnectionConfiguration config = new ConnectionConfiguration("chat.facebook.com",5222);
			config.setSASLAuthenticationEnabled(true);
			XMPPConnection xmpp = new XMPPConnection(config);
			SASLAuthentication.registerSASLMechanism("X-FACEBOOK-PLATFORM", SASLXFacebookPlatformMechanism.class);
		    SASLAuthentication.supportSASLMechanism("X-FACEBOOK-PLATFORM", 0);
		    xmpp.connect();
		    xmpp.login(super.appID,accessToken.getToken(),"chat.facebook.com");
		    Chat c = xmpp.getChatManager().createChat("-"+recipientId+"@chat.facebook.com", new MessageListener() {
				public void processMessage(Chat arg0, Message arg1) {}
			});
		    Message msgObj = new Message("-"+ recipientId +"@chat.facebook.com", Message.Type.chat);
	        msgObj.setBody(message);
	        c.sendMessage(msgObj);
	        xmpp.disconnect();
		}catch(XMPPException e){ throw new SocialNetworkUnavailableException(e);}
	}

	@Override
	public String getApiURL() {
		return graphUrl;
	}

	@Override
	public SocialNetworkToken buildAccessToken(Token requestToken, String verifier) {
		return new FacebookToken(getOAuthService("","").getAccessToken(null, new Verifier(verifier)));
	}
	
	public SocialNetworkToken buildToken(String verifier,URL url){
		return new FacebookToken(getOAuthService("",url+"&network=facebook").getAccessToken(null, new Verifier(verifier)));
	}
	
	@Override
	public OAuthService getOAuthService(String scope, String callback) {
		ServiceBuilder sb = new ServiceBuilder()
        .provider(FacebookApi.class)
        .apiKey(appID)
        .apiSecret(appSecret)
        .callback(callback);
		if(!scope.equals("")) sb.scope(scope);
		return sb.build();
	}

	@Override
	public String getAuthenticationURL(SocialNetworkRequestTokenDatabase tdb, String callback,String ... scope) 
    throws SocialNetworkUnavailableException{
		StringBuilder sb = new StringBuilder();
		for(int i = 0;i<scope.length;i++){
			if(i > 0) sb.append(",");
			sb.append(scope[i]);
		}
		return getOAuthService(sb.toString(),callback).getAuthorizationUrl(null);
	}

	@Override
	public String getAuthenticationUrlWithCallbackForPage(SocialNetworkRequestTokenDatabase tdb, URL page, String... scope) 
    throws SocialNetworkUnavailableException{
		return getAuthenticationURL(tdb, page+"&network=facebook",scope);
	}

	@Override
	public SocialParser getParser() {
		return new FacebookParser();
	}
	
	@Override
	public String[] getScopeForConnection() { return new String[] {
                "friends_about_me","user_work_history","xmpp_login","user_education_history",
                "email","user_birthday","friends_education_history","friends_work_history","publish_stream" };
	}

	@Override
	public String getFieldsForUserInformation() {
		return "name,email,picture,birthday,work,education,gender,link,picture";
	}

    @Override
    public void deletePost(SocialNetworkToken token, PostId id) throws SocialNetworkUserException {
        OAuthRequest post = new OAuthRequest(Verb.DELETE,graphUrl + id.getId());
        getOAuthService("", "").signRequest(token.getAccessToken(), post);
        post.send();
    }
}
