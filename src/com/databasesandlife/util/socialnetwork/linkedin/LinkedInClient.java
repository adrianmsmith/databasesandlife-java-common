package com.databasesandlife.util.socialnetwork.linkedin;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;


import org.scribe.builder.ServiceBuilder;
import org.scribe.model.Token;
import org.scribe.model.Verifier;
import org.scribe.oauth.OAuthService;
import org.scribe.utils.OAuthEncoder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.databasesandlife.util.socialnetwork.OAuth2Request;
import com.databasesandlife.util.socialnetwork.OAuthClient;
import com.databasesandlife.util.socialnetwork.PostId;
import com.databasesandlife.util.socialnetwork.SocialNetworkRequestTokenDatabase;
import com.databasesandlife.util.socialnetwork.SocialNetworkToken;
import com.databasesandlife.util.socialnetwork.SocialNetworkUnavailableException;
import com.databasesandlife.util.socialnetwork.SocialNetworkUserException;
import com.databasesandlife.util.socialnetwork.SocialParser;
import com.google.gson.Gson;

@SuppressWarnings("serial")
public class LinkedInClient extends OAuthClient{

	public enum LinkedInVisibility{
		connectionsOnly,anyone; 
	}
	
	public LinkedInClient(String appID,String secret){
		super(appID,secret);
	}
	
	private final String linkedInUrl = "https://api.linkedin.com/v1/people/~";
	
	public PostId postToWall(SocialNetworkToken token, String title, String message, String imageUrl, String description, URL link) throws SocialNetworkUnavailableException{
		String xml = xmlToString(createNewShareXml(message,title,description,link,"connections-only"));
		OAuth2Request.doPost(getApiURL()+"/shares?oauth2_access_token="+token.getAccessToken().getToken(),xml);
        return new PostId(0);
	}

	/**
	 * Creates a new w3c xml Element which looks like this<br>
	 * 
	 * &lt;share><br>
	 * 	&nbsp;&nbsp;&lt;comment>Example comment&lt;/comment><br>
	 * 	&nbsp;&nbsp;&lt;content><br>
	 * 		&nbsp;&nbsp;&nbsp;&nbsp;&lt;title>Example&lt;/title><br>
	 * 		&nbsp;&nbsp;&nbsp;&nbsp;&lt;description>Random Text&lt;/description><br>
	 * 		&nbsp;&nbsp;&nbsp;&nbsp;&lt;submitted-url>www.example.com&lt;/submitted-url><br>
	 * 	&nbsp;&nbsp;&lt;/content><br>
	 * 	&nbsp;&nbsp;&lt;visibility><br>
	 * 		&nbsp;&nbsp;&nbsp;&nbsp;&lt;code>connections-only&lt;/code><br>
	 * 	&nbsp;&nbsp;&lt;/visiblity><br>
	 * &lt;/share>
	 * @return the element
	 */
	private Element createNewShareXml(String userComment,String title,String desc,URL link,String visiblity){
		Document doc;
		try {
			doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
			Element share = doc.createElement("share");
			Element comment = doc.createElement("comment");
			comment.appendChild(doc.createTextNode(userComment));
			share.appendChild(comment);
			Element content = doc.createElement("content");
			Element contentTitle = doc.createElement("title");
			contentTitle.appendChild(doc.createTextNode(title));
			content.appendChild(contentTitle);
			Element description = doc.createElement("description");
			description.appendChild(doc.createTextNode(desc));
			content.appendChild(description);
			Element url = doc.createElement("submitted-url");
			url.appendChild(doc.createTextNode(link.toExternalForm()));
			content.appendChild(url);
			Element visibility = doc.createElement("visibility");
			Element code = doc.createElement("code");
			code.appendChild(doc.createTextNode(visiblity));
			visibility.appendChild(code);
			share.appendChild(content);
			share.appendChild(visibility);
			return share;
		} catch (ParserConfigurationException e) {
			throw new RuntimeException(e);
		}
	}

	public OAuthService getOAuthService(String scope,String callback) {
		ServiceBuilder sb =  new ServiceBuilder()
        .provider(LinkedIn2Api.class)
        .apiKey(appID)
        .apiSecret(appSecret);
		if(!callback.isEmpty())sb.callback(callback);
		sb.scope(scope);
		return sb.build();
	}
	
	public String getApiURL(){
		return linkedInUrl;
	}
	
	@Override
	public String getAuthenticationURL(SocialNetworkRequestTokenDatabase tdb, String callback,String... scope) 
    throws SocialNetworkUnavailableException {
		try{
			StringBuilder sb = new StringBuilder();
			for(int i = 0;i<scope.length;i++){
				if(i > 0) sb.append(" ");
				sb.append(scope[i]);
			}
			return getOAuthService(sb.toString(),callback).getAuthorizationUrl(null);
		}catch(Exception e){
			throw new SocialNetworkUnavailableException(e);
		}
	}
	
	@Override
	public String getAuthenticationUrlWithCallbackForPage(SocialNetworkRequestTokenDatabase tdb, URL page, String... scope) 
    throws SocialNetworkUnavailableException{
		return getAuthenticationURL(tdb, page+"&network=linkedin",scope);
	}
	
	public SocialNetworkToken buildAccessToken(Token requestToken,String verifier){
		return new LinkedInToken(getOAuthService("","").getAccessToken(requestToken, new Verifier(verifier)));
	}
	
	public SocialNetworkToken buildToken(String verifier,URL page){
		return buildTokenHomebrewed(verifier, page);
	}
	
	@SuppressWarnings("unchecked")
    public SocialNetworkToken buildTokenHomebrewed(String verifier,URL page){
		String postUrl = "https://www.linkedin.com/uas/oauth2/accessToken?grant_type=authorization_code"+
            "&code=%s"+
            "&redirect_uri=%s"+
            "&client_id=%s"+
            "&client_secret=%s";
		postUrl = String.format(postUrl, OAuthEncoder.encode(verifier),OAuthEncoder.encode(page+"&network=linkedin"),OAuthEncoder.encode(this.appID),OAuthEncoder.encode(this.appSecret));
		String value = OAuth2Request.doPost(postUrl);
        Gson gson = new Gson();
        Map<String,String> values = gson.fromJson(value, HashMap.class);
        return new LinkedInToken(new Token(values.get("access_token"),""));
	}
	
	public void sendMessage(SocialNetworkToken accessToken,String recipientId,String subject,String body){
		String xml = createMessageXml(recipientId,subject,body);
		OAuth2Request.doPost(getApiURL()+"/mailbox?oauth2_access_token="+accessToken.getAccessToken().getToken(), xml);
	}
	
	private String createMessageXml(String recipientId,String subject,String body){
		Document doc = null;
		try{
			doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
			Element mailItem = doc.createElement("mailbox-item");
			Element recipients = doc.createElement("recipients");
			Element recipient = doc.createElement("recipient");
			Element person = doc.createElement("person");
			person.setAttribute("path", "/people/"+recipientId);
			recipient.appendChild(person);
			recipients.appendChild(recipient);
			mailItem.appendChild(recipients);
			Element subjectNode = doc.createElement("subject");
			subjectNode.appendChild(doc.createTextNode(subject));
			mailItem.appendChild(subjectNode);
			Element bodyNode = doc.createElement("body");
			bodyNode.appendChild(doc.createTextNode(body));
			mailItem.appendChild(bodyNode);
			return xmlToString(mailItem);
		} catch (ParserConfigurationException e) {
			throw new RuntimeException(e);
		}
	}

	private String getLinkedinInformations(Token accessToken,String scope) throws SocialNetworkUnavailableException {
		String getUrl= "https://api.linkedin.com/v1/people/%s?oauth2_access_token=%s";
		return OAuth2Request.doGet(String.format(getUrl, scope,accessToken.getToken()));
	}
	
	@Override
	public Iterable<String> getFriendsInformation(Token accessToken, String scope) throws SocialNetworkUnavailableException{
		List<String> a = new ArrayList<String>();
		a.add(getLinkedinInformations(accessToken, "~/connections"));
		return a;
	}

	@Override
	public String getUserInformation(Token accessToken, String scope) throws SocialNetworkUnavailableException {
		String getUrl= "https://api.linkedin.com/v1/people/%s?oauth2_access_token=%s";
		getUrl = String.format(getUrl,scope, accessToken.getToken());
		return OAuth2Request.doGet(getUrl);
	}

	@Override
	public SocialParser getParser() {
		return new LinkedInParser();
	}
	
	@Override
	public String getFieldsForUserInformation() {
	    return "~:(id,first-name,last-name,industry,skills,educations,certifications,email-address,positions," +
    		"picture-url,phone-numbers,languages,main-address,picture-urls::(original))";
	}

    @Override
    public void deletePost(SocialNetworkToken token, PostId id) throws SocialNetworkUserException {
        throw new RuntimeException("Delete Post for Linkedin is currently not supported");
    }

    public String[] getScopeForConnection(){
		return new String[]{"r_network","rw_nus","r_fullprofile","w_messages","r_emailaddress","r_contactinfo"};
	}
}
