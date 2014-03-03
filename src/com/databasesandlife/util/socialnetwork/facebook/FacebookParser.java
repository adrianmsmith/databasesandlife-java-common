package com.databasesandlife.util.socialnetwork.facebook;

import com.databasesandlife.util.Gender;
import com.databasesandlife.util.YearMonthDay;
import com.databasesandlife.util.socialnetwork.*;
import com.google.gson.Gson;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@SuppressWarnings({ "unchecked", "serial" })
public class FacebookParser extends SocialParser implements Serializable{

	public List<SocialFriend<?>> getFriends(String json) throws SocialNetworkUnavailableException,SocialNetworkUserException{
	    class FacebookFriendInit extends FacebookFriend {
	        public FacebookFriendInit(Map<String,?> x) throws SocialNetworkUnavailableException {
                //m.get("first_name") and m.get("last_name") are always Strings
                firstName = x.get("first_name") != null ? x.get("first_name").toString() : "";
                lastName = x.get("last_name") != null ? x.get("last_name").toString() : "";
                //x.get("id") is always a String
                id = new FacebookUserId((x.get("id") != null) ? x.get("id").toString() : "");
                education = FacebookParser.this.getEducation(x.get("education"));
                work = FacebookParser.this.getWork(x.get("work"));
                pictureUrl = "http://graph.facebook.com/"+id+"/picture";
	        }
	    }
	    
		try{
			List<SocialFriend<?>> friends = new ArrayList<SocialFriend<?>>();
			Gson gson = new Gson();
			Map<String,?> data = gson.fromJson(json, Map.class);
			validateRequest(data);
			data = new HashMap<String,Object>((Map<String,?>) data.get("friends"));
			List<Map<String,?>> friendInformation = (List<Map<String,?>>)data.get("data");
			for(Map<String,?> m : friendInformation){
				FacebookFriend f = new FacebookFriendInit(m);
				f.parseSearchData();
				friends.add(f);
			}
			
			return friends;
		}catch(SocialNetworkUserException e){
			throw e;
		}catch(Exception e){
			throw new SocialNetworkUnavailableException(e);
		}
	}
	
	private void validateRequest(Map<String,?> m) throws SocialNetworkUnavailableException,SocialNetworkUserException{
		try{
			if(m.get("error") != null && ((Map)m.get("error")).get("code") != null){
				Map error = (Map)m.get("error");
				String code = error.get("code").toString();
				if(code.equals("190.0") && error.get("error_subcode") != null){
					String subcode = error.get("error_subcode").toString();
					if(subcode.equals("458.0")) throw new SocialNetworkNotAuthorizedException("Application is not authorized");
					else if(subcode.equals("463.0")) throw new SocialNetworkTokenExpiredException("Token has expired");
				}else if(code.equals("1.0") || code.equals("2.0") || code.equals("4.0") || code.equals("17.0")){
					throw new SocialNetworkTryLaterException("Error!Please try again later");
				}else if(code.equals("10") || (Integer.parseInt(code) <= 200 && Integer.parseInt(code) >= 299)){
					throw new SocialNetworkNotAuthorizedException("User has not granted the required permissions");
				}else throw new SocialNetworkUnavailableException("General error during Facebook Request!");
			}
		}catch(SocialNetworkUserException e){
			throw e;
		}catch(Exception e){
			throw new SocialNetworkUnavailableException(e);
		}
	}
	
	protected YearMonthDay parseAmericanDate(String x) throws SocialNetworkUnavailableException {
	    try {
    	    Date y = new SimpleDateFormat("MM/dd/yyyy").parse(x);
    	    Calendar z = Calendar.getInstance();
    	    z.setTime(y);
    	    return new YearMonthDay(z.get(Calendar.YEAR), z.get(Calendar.MONTH), z.get(Calendar.DAY_OF_MONTH));
	    }
	    catch (ParseException e) { throw new SocialNetworkUnavailableException(e); }
	}
	
	public FacebookSocialUser getUserInformation(String json) throws SocialNetworkUnavailableException{
	    class FacebookSocialUserInit extends FacebookSocialUser {
	        public FacebookSocialUserInit(Map<String,?> data) throws SocialNetworkUnavailableException {
                setName(data.get("name").toString());
                id = new FacebookUserId(data.get("id").toString());
                if (data.get("email") != null) setEmailAddress(data.get("email").toString());
                gender = (data.get("gender") != null) ? (data.get("gender").toString().equals("male") ? Gender.Male : Gender.Female) : null;
                birthday = (data.get("birthday") != null) ? parseAmericanDate(data.get("birthday").toString()) : YearMonthDay.newMinusInfinity();
                work = FacebookParser.this.getWork(data.get("work"));
                edu = FacebookParser.this.getEducation(data.get("education"));
                profileUrl = data.get("link") != null ? data.get("link").toString() : "";
                pictureUrl = data.get("picture") != null
                          && (Boolean) ((Map<String,Map<String,?>>)data.get("picture")).get("data").get("is_silhouette") == false
                           ? (String) ((Map<String,Map<String,?>>)data.get("picture")).get("data").get("url") : null;
	        }
	    }
	    
		try{
		    return new FacebookSocialUserInit(new Gson().fromJson(json, Map.class));
		}catch(Exception e){
			throw new SocialNetworkUnavailableException(e);
		}
	}

    @Override
    protected SocialNetworkPostId getPostId(String response) throws SocialNetworkUserException {
        System.out.println(response);
        Gson gson = new Gson();
        Map values = gson.fromJson(response,HashMap.class);
        if (values.get("error") != null) {
            if (Integer.parseInt(((Map)values.get("error")).get("code").toString()) == 506
                    && Integer.parseInt(((Map)values.get("error")).get("error_subcode").toString()) == 1455006) {
                throw new SocialNetworkUserException("Failed to post because the message is the same as the last post.");
            }
        }
        return new SocialNetworkPostId(values.get("id").toString());
    }
}
