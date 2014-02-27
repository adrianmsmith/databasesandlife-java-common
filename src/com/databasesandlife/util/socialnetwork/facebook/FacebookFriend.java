package com.databasesandlife.util.socialnetwork.facebook;

import com.databasesandlife.util.socialnetwork.School;
import com.databasesandlife.util.socialnetwork.SocialFriend;
import com.databasesandlife.util.socialnetwork.Work;

@SuppressWarnings("serial")
public class FacebookFriend extends SocialFriend<FacebookUserId> {

	public String prettyPrint(){
		StringBuilder sb = new StringBuilder();
		sb.append("Name: ").append(getName());
		sb.append("ID: ").append(getId());
		if(education != null)
			for(School s : education){
				sb.append(s.prettyPrint());
			}
		if(work != null)
			for(Work st : work){
				sb.append(st.prettyPrint());
			}
		return sb.toString();
	}
	
}
