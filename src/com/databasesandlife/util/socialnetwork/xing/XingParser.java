package com.databasesandlife.util.socialnetwork.xing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.databasesandlife.util.socialnetwork.*;
import org.apache.commons.lang.ArrayUtils;

import com.databasesandlife.util.Gender;
import com.databasesandlife.util.YearMonth;
import com.databasesandlife.util.YearMonth.YearMonthParseException;
import com.databasesandlife.util.socialnetwork.SocialNetworkPostId;
import com.databasesandlife.util.YearMonthDay;
import com.google.gson.Gson;

@SuppressWarnings({ "unchecked", "serial" })
public class XingParser extends SocialParser {

    @Override
	public List<SocialFriend<?>> getFriends(String json) throws SocialNetworkUnavailableException{
        class XingFriendInit extends XingFriend {
            public XingFriendInit(Map<String,?> m) {
                id = new XingUserId(m.get("id").toString());
                //Xing does not support first/last name
                firstName = m.get("display_name").toString();
                lastName = "";
                education = new School[0];
                pictureUrl = ((Map<String,?>) m.get("photo_urls")).get("large").toString();
                List<Work> workList = new ArrayList<Work>();
                Map<String,?> workMap = (Map<String, ?>) m.get("professional_experience");
                Map<String,?> primaryCompanies = (Map<String, ?>) workMap.get("primary_company");
                if(primaryCompanies != null){
                    workList.add(mapToWork(primaryCompanies));
                }
                List<Map<String,?>> nonPrimaryCompanies = (List<Map<String, ?>>) workMap.get("non_primary_companies");
                if (nonPrimaryCompanies != null){ 
                    for(Map<String,?> map : nonPrimaryCompanies){
                        workList.add(mapToWork(map));
                    }
                }
                work = workList.toArray(new Work[0]);
            }
        }
        
		try{
			Gson gson = new Gson();
			List<SocialFriend<?>> xfs = new ArrayList<SocialFriend<?>>();
			Map<String,?> data = new HashMap<String,Object>(gson.fromJson(json, Map.class));
			Map<String,?> contactMap = new HashMap<String,Object>((Map<String,?>)data.get("contacts"));
			if (contactMap != null) {
    		    List<Map<String,?>> users = (List<Map<String,?>>) contactMap.get("users");
    		    for(Map<String,?> map : users){
    		    	XingFriend x = new XingFriendInit(map);
    		    	x.parseSearchData();
    		    	xfs.add(x);
    		    }
			}
		    return xfs;
		}catch(Exception e){
			throw new SocialNetworkUnavailableException(e);
		}
	}

    private Work mapToWork(Map<String,?> map){
        class XingOwnWorkInit extends Work {
    	    public XingOwnWorkInit(Map<String,?> map){
        		employer = map.get("name") != null ? map.get("name").toString() : null;
        		position = map.get("title") != null ? map.get("title").toString() : null;
        		tags = map.get("tag") != null ? map.get("tag").toString() : "";
        		tags += map.get("industry") != null ? " "+map.get("industry").toString() : "";
        		tags += map.get("description") != null ? " "+map.get("description").toString() : "";
     	    }
	    };
     	return new XingOwnWorkInit(map);
    }
    
	@Override
	public SocialUser<XingUserId> getUserInformation(String json) throws SocialNetworkUnavailableException{
		try{
			System.out.println(json);
			Gson gson = new Gson();
			Map<String,?> data = gson.fromJson(json, Map.class);
			Map<String, ?> userMap = ((List<Map<String, ?>>)data.get("users")).get(0);
			
			class XingSocialUserInit extends XingSocialUser {
			    public XingSocialUserInit(Map<String, ?> userMap) throws SocialNetworkUnavailableException {
			    	try {
			            String parsedFirstName = (userMap.get("first_name") != null) ? userMap.get("first_name").toString() : "";
			            String parsedLastName = (userMap.get("last_name") != null) ? userMap.get("last_name").toString() : "";
			            Gender parsedGender = (userMap.get("gender") != null) ? (userMap.get("gender").equals("m") ? Gender.Male : Gender.Female) : null;
			            Map<String, ?> birthdayMap = (Map<String, ?>) userMap.get("birth_date");
			            String birthday = new StringBuilder().append(
			                    String.format("%.0f",birthdayMap.get("year"))).append("-")
			                    .append(String.format("%02.0f",birthdayMap.get("month")))
			                    .append("-").append(String.format("%02.0f",birthdayMap.get("day"))).toString();
			            YearMonthDay ymd = YearMonthDay.newForYYYYMMDD(birthday); 
			            String parsedId = userMap.get("id").toString();
			            Map<String, ?> pictureUrls = (Map<String, ?>) userMap.get("photo_urls");
			            String pictureLarge = pictureUrls.get("large").toString();
			            Map<String,?> privateAddressMap = (Map<String,?>)userMap.get("private_address");
			            String mobilePhoneNumber = privateAddressMap.get("mobile_phone") != null ? 
			                    privateAddressMap.get("mobile_phone").toString() : null;
			            String parsedEmailAddress = privateAddressMap.get("email") != null ? privateAddressMap.get("email").toString() : null;
			            String parsedStreet = privateAddressMap.get("street") != null ? privateAddressMap.get("street").toString() : null;
			            String parsedZipCode = privateAddressMap.get("zip_code") != null ? privateAddressMap.get("zip_code").toString() : null;
			            String parsedCity = privateAddressMap.get("city") != null ? privateAddressMap.get("city").toString() : null;
			            String parsedCountry = privateAddressMap.get("country") != null ? privateAddressMap.get("country").toString() : null;
			            String parsedProfileUrl = userMap.get("permalink") != null ? userMap.get("permalink").toString() : "";
	
			            Map<String,String> languages = (Map<String,String>)userMap.get("languages");
			            List<Language> langs = new ArrayList<Language>();
			            for(Entry<String,String> e : languages.entrySet()){
			                langs.add(new Language(e.getKey().toString(),e.getValue() != null ? e.getValue().toString() : ""));
			            }
			            
				        this.id = new XingUserId(parsedId);
	    				this.birthday = ymd;
	    				Map<String,?> eduMap = (Map<String,?>) userMap.get("educational_background");
	    				education = XingParser.this.getEducation((List<Map<String,?>>)eduMap.get("schools"));
	    				Map<String,?> workMap = (Map<String,?>) userMap.get("professional_experience");
	    				work = XingParser.this.getWork((List<Map<String,?>>) workMap.get("non_primary_companies"));
					 	if(((Map<String,?>)workMap.get("primary_company")).get("name") != null) work = (Work[])ArrayUtils.addAll(new Work[]{new XingFriendWorkInit((Map<String,?>)workMap.get("primary_company"))},work);
	    				this.firstName = parsedFirstName;
	    				this.lastName = parsedLastName;
	    				firstName = parsedFirstName;
	    				lastName = parsedLastName;
	    				this.gender = parsedGender;
	    				setEmailAddress(parsedEmailAddress);
	    				mobilePhone = mobilePhoneNumber != null ? "+"+mobilePhoneNumber.replaceAll("\\|", " ") : null;
	    				street = parsedStreet;
	    				zipCode = parsedZipCode;
	    				city = parsedCity;
	    				country = parsedCountry;
	    				pictureUrl = pictureLarge;
	    				this.profileUrl = parsedProfileUrl;
	    				this.languages = langs.toArray(new Language[0]);
	    				List<String> qualis = (List<String>) eduMap.get("qualifications");
	    				this.qualifications = qualis.toArray(new String[0]);
			    	}catch (YearMonthParseException e) {
    					throw new SocialNetworkUnavailableException(e);
				 	}
			    }
			}
			return new XingSocialUserInit(userMap);
		}catch(Exception e){
			throw new SocialNetworkUnavailableException(e);
		}
	}

    @Override
    protected SocialNetworkPostId getPostId(String response) throws SocialNetworkUserException {
        return new SocialNetworkPostId(0);
    }

    private School[] getEducation(List<Map<String,?>> e) throws SocialNetworkUnavailableException{
	    class XingFriendSchoolInit extends School {
	        public XingFriendSchoolInit(Map<String,?> m) throws YearMonthParseException {
                concentration = (m.get("subject") != null) ? m.get("subject").toString() : "";
                name = (m.get("name") != null) ? m.get("name").toString() : "";
                id = "";
                type = "High School";
                if(m.get("begin_date") != null) {
                	//this happens when the user has entered the year, but not the month
                	if(m.get("begin_date").toString().length() == 4) {
                		startDate = YearMonth.newForYYYYMM(m.get("begin_date").toString()+"-01");
                	}else{
                		startDate = YearMonth.newForYYYYMM(m.get("begin_date").toString());
                	}
                }
                if(m.get("end_date") != null) {
                	//this happens when the user has entered the year, but not the month
                	if(m.get("end_date").toString().length() == 4) {
                		endDate = YearMonth.newForYYYYMM(m.get("end_date").toString()+"-01");
                	}else{
                		endDate = YearMonth.newForYYYYMM(m.get("end_date").toString());
                	}
                }
                description = m.get("notes") != null ? m.get("notes").toString() : "";
	        }
	    }
	    
		try{
			if(e == null || e.size() == 0) return new School[0];
			School[] edu = new School[e.size()];
			for(int i = 0;i<e.size();i++) edu[i] = new XingFriendSchoolInit(e.get(i));
			return edu;
		}catch(Exception ex){
			throw new SocialNetworkUnavailableException(ex);
		}
	}
	
	private Work[] getWork(List<Map<String,?>> w) throws SocialNetworkUnavailableException{
		try{
			if(w == null || w.size() == 0) return new Work[0];
			Work[] wo = new Work[w.size()];
			for(int i = 0;i<w.size();i++)
				wo[i] = new XingFriendWorkInit(new HashMap<String,Object>(w.get(i)));
			return wo;
		}catch(Exception e){
			throw new SocialNetworkUnavailableException(e);
		}
	}
	
	private class XingFriendWorkInit extends Work {
        public XingFriendWorkInit(Map<String,?> m) throws YearMonthParseException {
            employer = m.get("name").toString();
            position = m.get("title").toString();
            if(m.get("begin_date") != null) {
            	//this happens when the user has entered the year, but not the month
            	if(m.get("begin_date").toString().length() == 4) {
            		startDate = YearMonth.newForYYYYMM(m.get("begin_date").toString()+"-01");
            	}else{
            		startDate = YearMonth.newForYYYYMM(m.get("begin_date").toString());
            	}
            }
            if(m.get("end_date") != null) {
            	//this happens when the user has entered the year, but not the month
            	if(m.get("end_date").toString().length() == 4) {
            		endDate = YearMonth.newForYYYYMM(m.get("end_date").toString()+"-01");
            	}else{
            		endDate = YearMonth.newForYYYYMM(m.get("end_date").toString());
            	}
            }
            description = m.get("description") != null ? m.get("description").toString() : "";
        }
    }
}
