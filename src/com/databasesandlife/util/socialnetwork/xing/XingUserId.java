package com.databasesandlife.util.socialnetwork.xing;

import com.databasesandlife.util.socialnetwork.SocialNetwork;
import com.databasesandlife.util.socialnetwork.SocialUserExternalId;

/** The ID of a XING user */
@SuppressWarnings("serial")
public class XingUserId extends SocialUserExternalId {
    public XingUserId(String x) { super(x); }

	@Override
	public SocialNetwork getSocialNetwork() {
	    return SocialNetwork.Xing;
	}
}
