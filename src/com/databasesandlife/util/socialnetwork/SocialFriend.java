package com.databasesandlife.util.socialnetwork;

import java.io.Serializable;

/**
 * This class represents a friend or contact from a social network
 */
@SuppressWarnings("serial")
public abstract class SocialFriend<ID extends SocialUserExternalId> implements Serializable{
	
    protected ID id;
	protected String firstName,lastName;
	protected Work[] work;
	protected School[] education;
	protected String searchData;
	protected String pictureUrl;
	
    public ID getId(){ return id;}
	public String getName(){ return firstName + " " + lastName;}
	public String getFirstName(){ return firstName;}
	public String getLastName(){ return lastName;}
	public Work[] getWork(){ return work;}
	public School[] getEducation(){ return education;}
	public String getPictureUrl(){ return pictureUrl != null ? pictureUrl : "";}
	public String[] getSearchData(){
		if(searchData == null) parseSearchData();
		return searchData.split(" ",2);
	}
	
	public void parseSearchData(){
		StringBuilder sb = new StringBuilder();
		sb.append(getName()).append(" ");
		for(Work w : work){
			sb.append(w.getEmployer()).append(" ");
			sb.append(w.getPosition()).append(" ");
			sb.append(w.getTags() != null ? w.getTags() : "");
		}
		for(School s : education){
			sb.append(s.getName()).append(" ");
			sb.append(s.getConcentration()).append(" ");
			sb.append(s.getType()).append(" ");
		}
		searchData = sb.toString();
	}
	
	public String toString(){ return getName();}
	
	public abstract String prettyPrint();
}
