package com.databasesandlife.util.socialnetwork.linkedin;

import com.databasesandlife.util.socialnetwork.SocialFriend;

@SuppressWarnings("serial")
public class LinkedInFriend extends SocialFriend<LinkedInUserId> {

	protected String industry;
	protected String headline;
	
	public String getIndustry(){ return industry;}
	public String getHeadline(){ return headline;}
	
	@Override
	public void parseSearchData(){
		super.parseSearchData();
		if(industry != null) searchData += " "+industry;
		if(headline != null) searchData += " "+headline;
	}
	
	@Override
	public String prettyPrint() {
		return new StringBuilder().append("Name: ").append(getName()).append("ID: ").append(id).append(" ").append("Education: ").append(education)
				.append("Work: ").append(work).toString();
	}
}
