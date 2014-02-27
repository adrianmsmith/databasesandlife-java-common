package com.databasesandlife.util.socialnetwork;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.scribe.model.Token;

import com.databasesandlife.util.Timer;

public class SocialClientThreadRunnable implements Futureable<SocialFriend<?>> {

	private OAuthClient c;
	private Token accessToken;
	private List<SocialFriend<?>> friends;
	private SocialNetworkUserException e;
	private SocialNetworkUnavailableException e1;
	
	public SocialClientThreadRunnable(OAuthClient c,Token accessToken){
		this.c = c;
		this.accessToken = accessToken;
		friends = new ArrayList<SocialFriend<?>>();
	}
	
	public void run() {
	    Timer.start("SocialClientThreadRunnable(" + c.getClass().getName() + ")");
		try{
			if(accessToken != null){
				//TODO use scope
				Iterable<String> strings = c.getFriendsInformation(accessToken,""); //empty means use default
				for(String s : strings){
					friends.addAll(c.getParser().getFriends(s));
				}
			}else{
				Logger.getLogger(getClass()).warn("Access token is null for " + c.getClass().getName() + " and will be skipped");
			}
		}catch(SocialNetworkUnavailableException e){
			this.e1 = e;
		} catch (SocialNetworkUserException e) {
			this.e = e;
		}
		finally { Timer.end ("SocialClientThreadRunnable(" + c.getClass().getName() + ")"); }
	}

	public List<SocialFriend<?>> getResult() throws SocialNetworkUserException,SocialNetworkUnavailableException{ 
		if(this.e != null) throw e;
		else if(this.e1 != null) throw e1;
		else return friends != null ? friends : new ArrayList<SocialFriend<?>>();
	}
	
}
