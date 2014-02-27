package com.databasesandlife.util.socialnetwork;

import java.io.Serializable;

import com.databasesandlife.util.YearMonth;

@SuppressWarnings("serial")
public class Work implements Serializable{

	protected String employer;
	protected String position;
	protected YearMonth startDate;
	protected YearMonth endDate;
	protected String description;
	protected String tags;
	
	public String getEmployer() { return employer; }
	public String getPosition() { return position; }
	public YearMonth getStartDate() { return startDate; }
	public YearMonth getEndDate() { return endDate; }
	public String getDescription(){ return description;}
	public String getTags(){ return tags;}
	
	public String prettyPrint(){
		StringBuilder sb = new StringBuilder();
		if(employer != null) sb.append("Employer: ").append(employer);
		if(position != null) sb.append("Position: ").append(position);
		return sb.toString();
	}
	
    public static Work getCurrentOrNull(Work[] workList){
    	if(workList == null || workList.length == 0) return null; 
        Work current = null;
        for(Work w : workList){
            if(w.getEndDate() == null){
                current = w; break;
            }
        }
        return current;
    }
}
