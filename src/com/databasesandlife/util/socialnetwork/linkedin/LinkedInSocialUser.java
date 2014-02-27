package com.databasesandlife.util.socialnetwork.linkedin;

import com.databasesandlife.util.socialnetwork.School;
import com.databasesandlife.util.socialnetwork.SocialUser;
import com.databasesandlife.util.socialnetwork.Work;

@SuppressWarnings("serial")
public class LinkedInSocialUser extends SocialUser<LinkedInUserId> {

	protected String originalImage;
	@Override public String getPictureUrl(){ return originalImage;}
	protected Work[] work;
	protected School[] education;
	@Override
	public Work[] getWork() {
		return work;
	}
	@Override
	public School[] getEducation() {
		return education;
	}
	
	
	
}
