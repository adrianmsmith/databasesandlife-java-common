package com.databasesandlife.util.socialnetwork;

import java.io.Serializable;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import com.databasesandlife.util.Gender;
import com.databasesandlife.util.YearMonthDay;

@SuppressWarnings("serial")
public abstract class SocialUser<ID extends SocialUserExternalId> implements Serializable{
    
    protected ID id;
    protected String firstName, lastName;
    protected String username;
    protected YearMonthDay birthday;
    protected InternetAddress emailAddress;
    protected Gender gender = null;
    protected String zipCode;
    protected String city;
    protected String street;
    protected String country;
    protected String mobilePhone;
    protected String pictureUrl;
    protected Language[] languages;
    protected String[] qualifications;
    protected String profileUrl;
    
    public ID getId(){ return id;}
    public String getFirstName(){ return firstName;}
    public String getLastName(){ return lastName;}
    /** currently just for twitter */ public String getUsername() { return username; }
    public YearMonthDay getBirthday(){ return birthday;}
    public InternetAddress getEmailAddress(){ return emailAddress;}
    public Gender getGender(){ return gender;}
    public String getZipCode(){ return zipCode;}
    public String getCity(){ return city;}
    public String getCountry(){ return country;}
    public String getStreet(){ return street;}
    public String getMobilePhoneNumber(){ return mobilePhone;}
    public String getPictureUrl(){ return pictureUrl;}
    public String getProfileUrl(){ return profileUrl;}
    public Language[] getLanguages(){ return languages;}
    public String[] getQualifications(){ return qualifications == null ? new String[0] : qualifications;}
    
    public abstract Work[] getWork();
    public abstract School[] getEducation();
    
    public String prettyPrint(){
        StringBuilder sb = new StringBuilder();
        sb.append("Name: ").append(firstName).append(" ").append(lastName).
            append("ID: ").append(id).append("Birthday: ").append(birthday != null ? birthday : "");
        sb.append("Email: ").append(emailAddress != null ? emailAddress : "").append("Gender: ").append(gender.toString());
        sb.append("Work: ");
        for(Work w : getWork()){
            sb.append(w.prettyPrint());
        }
        sb.append("Education:");
        for(School s : getEducation()){
            sb.append(s.prettyPrint());
        }
        return sb.toString();
    }

    protected void setName(String name) {
        int space = name.lastIndexOf(" ");
        if (space == -1) { 
            firstName = name; 
            lastName = null; 
        } else {
            firstName = name.substring(0, space); 
            lastName = name.substring(space+1); 
        }
    }
    
    protected void setEmailAddress(String str) {
        if (str == null) emailAddress = null;
        else if (str.isEmpty()) emailAddress = null;
        else try { emailAddress = new InternetAddress(str); }
        catch (AddressException e) { emailAddress = null; }
    }
}
