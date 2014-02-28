package com.databasesandlife.util.socialnetwork;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import com.databasesandlife.util.YearMonth;

@SuppressWarnings("serial")
public abstract class SocialParser implements Serializable{

    @SuppressWarnings("unchecked")
    protected Work[] getWork(Object o) throws SocialNetworkUnavailableException{
		try{
			if(o == null) return new Work[0];
			if(!(o instanceof List)) return new Work[0];
			List<Map<String,?>> work = (List<Map<String,?>>)o;
			Work[] works = new Work[work.size()];
			for(int i = 0;i<work.size();i++){
				Map<String,?> m = work.get(i);
				Work w = new Work();
				Map<String,?> employerMap = (Map<String,?>)m.get("employer");
				w.employer = (employerMap != null) ? employerMap.get("name").toString() : "";
				Map<String,?> positionMap = (Map<String,?>)m.get("position");
				w.position = (positionMap != null) ? positionMap.get("name").toString() : "";
				w.startDate = m.get("start_date") != null ? YearMonth.newForYYYYMM(m.get("start_date").toString()) : null;
				w.endDate = (m.get("end_date") != null) ? YearMonth.newForYYYYMM(m.get("end_date").toString()) : null;
				works[i] = w;
			}
			return works;
		}catch(Exception e){
			throw new SocialNetworkUnavailableException(e);
		}
	}

	
	@SuppressWarnings("unchecked")
    protected School[] getEducation(Object o) throws SocialNetworkUnavailableException{
		try{
			if(o == null) return new School[0];
			if(!(o instanceof List)) return new School[0];
			List<Map<String,?>> edu = (List<Map<String,?>>)o;
			School[] education = new School[edu.size()];
			for(int i = 0;i<edu.size();i++){
				Map<String,?> m = edu.get(i);
				School s = new School();
				Map<String,?> schoolMap = (Map<String,?>)m.get("school");
				s.name = schoolMap.get("name").toString();
				s.id = schoolMap.get("id").toString();
//				Map<String,?> yearMap = (Map<String,?>)m.get("year");
//				s.year = (yearMap != null) ? Integer.parseInt(yearMap.get("name").toString()): 0;
				List<Map<String,?>> concentrationList = ((List<Map<String,?>>)m.get("concentration"));
				s.concentration = (concentrationList != null) ? ((Map<String,?>)concentrationList.get(0)).get("name").toString() : "";
				s.type = m.get("type").toString();
				education[i] = s;
			}
			return education;
		}catch(Exception e){
			throw new SocialNetworkUnavailableException(e);
		}
	}
	
	public abstract List<SocialFriend<?>> getFriends(String json) throws SocialNetworkUnavailableException,SocialNetworkUserException;
	
	public abstract SocialUser<?> getUserInformation(String json) throws SocialNetworkUnavailableException,SocialNetworkUserException;

    protected abstract SocialNetworkPostId getPostId(String response) throws SocialNetworkUserException;
	
}
