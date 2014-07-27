package com.databasesandlife.util.socialnetwork;

import java.io.Serializable;

import com.databasesandlife.util.YearMonth;

@SuppressWarnings("serial")
public class School implements Serializable{

    protected String id;
    protected String name;
    protected String type;
    protected String concentration;
    protected String description;
    protected YearMonth startDate;
    protected YearMonth endDate;
    
    public String getId(){ return id;}
    public String getName(){ return name;}
    public String getType(){ return type;}
    public String getConcentration(){ return concentration;}
    public YearMonth getStartDate(){ return startDate;}
    public YearMonth getEndDate(){ return endDate;}
    public String getDescription(){ return description;}
    
    public String prettyPrint(){
        StringBuilder sb = new StringBuilder();
        sb.append("Name: ").append(getName());
        sb.append("ID: ").append(getId());
        sb.append("Type: ").append(getType());
        if(concentration != null) sb.append("Concentration: ").append(getConcentration());
        return sb.toString();
    }
    
    
}
