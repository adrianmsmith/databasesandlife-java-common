package com.databasesandlife.util.socialnetwork.linkedin;

import org.scribe.builder.api.LinkedInApi;

public class LinkedInApiWithScope extends LinkedInApi {

	private static final String SCOPE = "r_network%20rw_nus%20r_fullprofile%20w_messages";

	@Override
	public String getRequestTokenEndpoint() {
		return addScope(super.getRequestTokenEndpoint());
	}
	
	private String addScope(String url) {
		url += url.contains("?") ? "&" : "?";
		return url + "scope=" + SCOPE;
	}
}