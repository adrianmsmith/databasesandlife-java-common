package com.databasesandlife.util.socialnetwork;

import java.io.Serializable;
import java.io.StringWriter;
import java.net.URL;
import java.util.Iterator;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.log4j.Logger;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;
import org.scribe.model.Token;
import org.scribe.model.Verb;
import org.scribe.oauth.OAuthService;
import org.w3c.dom.Node;

import com.databasesandlife.util.Future;
import com.databasesandlife.util.socialnetwork.facebook.FacebookClient;
import com.databasesandlife.util.socialnetwork.google.GoogleClient;
import com.databasesandlife.util.socialnetwork.wicket.SocialNetworkIntermediatePage;
import com.databasesandlife.util.socialnetwork.xing.XingClient;

@SuppressWarnings("serial")
public abstract class OAuthClient implements Serializable{
    
	protected String appID;
	protected String appSecret;
	
	private SocialNetworkUserException userException;
	
	public OAuthClient(String appID,String appSecret){
		this.appID = appID;
		this.appSecret = appSecret;
	}
	
	public static OAuthClient getClientForNetwork(SocialClientFactory fac, SocialNetwork network){
		switch(network){
			case Facebook: return fac.getFacebookClient();
			case LinkedIn: return fac.getLinkedInClient();
			case Xing: return fac.getXingClient();
			case Google: return fac.getGoogleClient();
			default: return null;
		}
	}
	
	/**
	 * Used to post something on the wall/feed of the social network
	 */
	public abstract PostId postToWall(SocialNetworkToken token, String title, String message, String imageUrl, String description, URL link) throws SocialNetworkUnavailableException;
	
	public abstract String[] getScopeForConnection();
	
	public abstract OAuthService getOAuthService(String scope,String callback);
	
	/**
	 * return a url to a network with the given callback
	 * the scope differs between the networks
	 * @see {@link LinkedinClient#getAuthenticationURL(String, String...)}, {@link FacebookClient#getAuthenticationURL(String, String...)},
	 *  {@link XingClient#getAuthenticationURL(String, String...)}, {@link GoogleClient#getAuthenticationURL(String, String...)}
	 */
	public abstract String getAuthenticationURL(SocialNetworkRequestTokenDatabase tdb, String callback,String...scope) throws SocialNetworkUnavailableException;
	
	/**
	 * returns a url to the social network with a callback to a page<br>
	 * the page should always be an instance of {@link SocialNetworkIntermediatePage} in order to work properly
	 * <br>
	 * The scope differs between the networks
	 * 	@see {@link LinkedinClient#getAuthenticationURL(String, String...)}, {@link FacebookClient#getAuthenticationURL(String, String...)},
	 *  {@link XingClient#getAuthenticationURL(String, String...)}, {@link GoogleClient#getAuthenticationURL(String, String...)}
	 * @param url for example <code>JobWebsiteApplication.get().newAbsoluteUrl(page)</code>
	 */
	public abstract String getAuthenticationUrlWithCallbackForPage(SocialNetworkRequestTokenDatabase tdb, URL url,String...scope) throws SocialNetworkUnavailableException;
	
	/**
	 * returns a future object containing JSON/XML informations about the friends of the user
	 * <br>
	 * Best practice is to process this object at the very last since it will run as long in parallel
	 * as long its not called
	 * @see {@link Futureable}, {@link Future}
	 */
	public abstract Iterable<String> getFriendsInformation(Token accessToken,String scope) throws SocialNetworkUnavailableException;
	
	/**
	 * Returns the user informations as single JSON/XML string
	 */
	public abstract String getUserInformation(Token accessToken,String scope) throws SocialNetworkUnavailableException;
	
	public abstract String getApiURL();
	
	public abstract SocialNetworkToken buildAccessToken(Token requestToken,String verifier);
	
	public abstract SocialParser getParser();
	
	public abstract String getFieldsForUserInformation();

    public abstract void deletePost(SocialNetworkToken token, PostId id) throws SocialNetworkUserException;

	public SocialUser<?> getUserInformation(Token accessToken) throws SocialNetworkUnavailableException, SocialNetworkUserException {
	    return getParser().getUserInformation(getUserInformation(accessToken, getFieldsForUserInformation()));
	}
	
	protected Response getInformations(Token accessToken,String scope){
		OAuthRequest authRequest = new OAuthRequest(Verb.GET,getApiURL()+scope);
		getOAuthService(scope,"").signRequest(accessToken, authRequest);
		Response r = authRequest.send();
		
        Logger log = Logger.getLogger(OAuthClient.class.getName() + ".requestLog");
		log.trace("Social Network Request to '" + getApiURL()+scope +
	        "', response code '" + r.getCode() + "', body is:\n" + r.getBody());
		
		return r;
	}

	public <T> Iterable<T> createFuture(final Futureable<T> data) throws SocialNetworkUnavailableException{
		try{
			final Thread t = new Thread(data);
			t.start();
			return new Iterable<T>() {
				public Iterator<T> iterator() {
					try {
						t.join();
						return data.getResult().iterator();
					} catch (InterruptedException e) {
						throw new RuntimeException(e);
					} catch (SocialNetworkUnavailableException e) {
						throw new RuntimeException(e);
					} catch (SocialNetworkUserException e) {
						throw new RuntimeException(e);
					}
				}
			};
		}catch(Exception e){
			throw new SocialNetworkUnavailableException(e);
		}
	}
	
	public static String xmlToString(Node node) {
        try {
            Source source = new DOMSource(node);
            StringWriter stringWriter = new StringWriter();
            Result result = new StreamResult(stringWriter);
            TransformerFactory factory = TransformerFactory.newInstance();
            Transformer transformer = factory.newTransformer();
            transformer.transform(source, result);
            return stringWriter.getBuffer().toString();
        } catch (TransformerConfigurationException e) {
            e.printStackTrace();
        } catch (TransformerException e) {
            e.printStackTrace();
        }
        return null;
    }
	
}
