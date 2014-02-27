package com.databasesandlife.util.socialnetwork.google;

import com.databasesandlife.util.socialnetwork.SocialFriend;

@SuppressWarnings("serial")
public class GoogleContact extends SocialFriend<GoogleContactId>{

	protected String firstName;
	protected String lastName;
	protected String emailAdress;
	
	public String prettyPrint(){
		StringBuilder sb = new StringBuilder();
		sb.append("Name: ").append(firstName).append(" ").append(lastName)
		.append(" ").append("EmailAdress: ").append(emailAdress);
		return sb.toString();
	}

	public void setId(GoogleContactId id){
		this.id = id;
	}
	
	@Override
	public String getName(){
		return firstName + " " + lastName;
	}
	
	@Override
	public void parseSearchData(){
		StringBuilder sb = new StringBuilder();
		sb.append(firstName != null ? firstName : "").append(" ");
		sb.append(lastName != null ? lastName : "").append(" ");
		sb.append(emailAdress);
		searchData = sb.toString();
	}
}
