package com.databasesandlife.util.socialnetwork.wicket;

import java.io.Serializable;

import com.databasesandlife.util.socialnetwork.SocialNetworkToken;


public interface SocialNetworkCallback extends Serializable{

	public void onAuthentication(SocialNetworkToken networkToken);
	
	public void onFailure(Exception e);
}
