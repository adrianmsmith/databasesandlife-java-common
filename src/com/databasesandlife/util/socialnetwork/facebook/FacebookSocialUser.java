package com.databasesandlife.util.socialnetwork.facebook;

import com.databasesandlife.util.socialnetwork.School;
import com.databasesandlife.util.socialnetwork.SocialUser;
import com.databasesandlife.util.socialnetwork.Work;

@SuppressWarnings("serial")
public class FacebookSocialUser extends SocialUser<FacebookUserId> {

	protected Work[] work;
	protected School[] edu;
	
	@Override
	public Work[] getWork() {
		return work;
	}

	@Override
	public School[] getEducation() {
		return edu;
	}

}
