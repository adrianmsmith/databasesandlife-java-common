package com.databasesandlife.util.socialnetwork;

import java.util.ArrayList;
import java.util.List;

import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;
import org.scribe.model.Token;
import org.scribe.model.Verb;

public class SocialNetworkRequestThread implements Futureable<String> {

    private String result;
    private SocialNetworkUserException e;
    private Exception e1;
    
    private OAuthClient c;
    private Token accessToken;
    private String scope;
    
    public SocialNetworkRequestThread(OAuthClient c,Token accessToken,String scope){
        this.c = c;
        this.accessToken = accessToken;
        this.scope = scope;
    }
    
    public void run() {
        try{
            OAuthRequest authRequest = new OAuthRequest(Verb.GET,c.getApiURL()+scope);
            c.getOAuthService("","").signRequest(accessToken, authRequest);
            Response r = authRequest.send();
            if(r.getCode() == 403) e = new SocialNetworkTryLaterException("RATE_LIMIT_EXCEEDED");
            result = r.getBody();
        }catch(Exception e){
            this.e1 = e;
        }
    }
    
    public List<String> getResult() throws SocialNetworkUserException,SocialNetworkUnavailableException{
        if(e != null) throw e;
        else if(e1 != null) throw new SocialNetworkUnavailableException(e1);
        ArrayList<String> a = new ArrayList<String>();
        a.add(result);
        return a;
    }

}
