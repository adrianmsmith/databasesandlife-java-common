package com.databasesandlife.util.socialnetwork.wicket;


import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.protocol.http.WebSession;
import org.apache.wicket.request.IRequestParameters;
import org.apache.wicket.request.cycle.RequestCycle;

import com.databasesandlife.util.socialnetwork.SocialClientFactory;
import com.databasesandlife.util.socialnetwork.SocialNetworkRequestTokenDatabase;
import com.databasesandlife.util.socialnetwork.SocialNetworkToken;
import com.databasesandlife.util.wicket.UrlFromPageGenerator;

@SuppressWarnings("serial")
public class SocialNetworkIntermediatePage extends WebPage {

    private SocialClientFactory fac;
	private SocialNetworkCallback callback;
	
	public SocialNetworkIntermediatePage(SocialClientFactory fac, SocialNetworkCallback callback) {
	    this.fac = fac;
		this.callback = callback;
	}
	
	@Override
	public void onBeforeRender(){
		super.onBeforeRender();
		SocialNetworkToken token = null;
		try{
			IRequestParameters params = RequestCycle.get().getRequest().getQueryParameters();
			if(!params.getParameterValue("network").isNull()){
				String network = params.getParameterValue("network").toString();
				String verifier = params.getParameterValue("oauth_verifier").toOptionalString();
				String code = params.getParameterValue("code").toOptionalString();
				SocialNetworkRequestTokenDatabase tdb = ((SocialNetworkRequestTokenDatabase) WebSession.get());
				UrlFromPageGenerator app = ((UrlFromPageGenerator)WebApplication.get());
				if(network.equalsIgnoreCase("xing")){
					token = fac.getXingClient().buildAccessToken(tdb.getXingRequestToken(), verifier);
				}else if(network.equalsIgnoreCase("linkedin")){
					token = fac.getLinkedInClient().buildTokenHomebrewed(code, app.newAbsoluteUrl(SocialNetworkIntermediatePage.this));
				}else if(network.equalsIgnoreCase("facebook")){
					token = fac.getFacebookClient().buildToken(code, app.newAbsoluteUrl(SocialNetworkIntermediatePage.this));
				}else if(network.equalsIgnoreCase("google")){
					token = fac.getGoogleClient().buildAccessToken(tdb.getGoogleRequestToken(), verifier);
				}
			}
			callback.onAuthentication(token);
		}catch(Exception e){
			callback.onFailure(e);
		}
	}
}
