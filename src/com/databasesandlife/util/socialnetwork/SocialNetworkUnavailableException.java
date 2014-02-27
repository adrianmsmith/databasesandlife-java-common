package com.databasesandlife.util.socialnetwork;

/** If social network is unreachable or behaves strangely for example sends wrongly formatted responses */
@SuppressWarnings("serial")
public class SocialNetworkUnavailableException extends Exception {


	public SocialNetworkUnavailableException(String arg0) {
		super(arg0);
	}

	public SocialNetworkUnavailableException(Throwable arg0) {
		super(arg0);
	}

	public SocialNetworkUnavailableException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

}
