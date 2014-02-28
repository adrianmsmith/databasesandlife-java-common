package com.databasesandlife.util.socialnetwork.linkedin;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import com.databasesandlife.util.socialnetwork.*;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.databasesandlife.util.YearMonth;
import com.databasesandlife.util.YearMonth.YearMonthParseException;
import com.databasesandlife.util.socialnetwork.SocialNetworkPostId;

@SuppressWarnings("serial")
public class LinkedInParser extends SocialParser{

    @Override
	public List<SocialFriend<?>> getFriends(String json) throws SocialNetworkUnavailableException{
        class LinkedInFriendInit extends LinkedInFriend {
            public LinkedInFriendInit(Element e) {
                id = new LinkedInUserId(e.getElementsByTagName("id").item(0).getTextContent());
                firstName = e.getElementsByTagName("first-name").item(0).getTextContent();
                lastName = e.getElementsByTagName("last-name").item(0).getTextContent();
                work = new Work[0];
                education = new School[0];
                pictureUrl = e.getElementsByTagName("picture-url").getLength() == 0 ? null 
                        : e.getElementsByTagName("picture-url").item(0).getTextContent();
                industry = e.getElementsByTagName("industry").getLength() == 0 ? null
                        : e.getElementsByTagName("industry").item(0).getTextContent();
                headline = e.getElementsByTagName("headline").getLength() == 0 ? null
                        : e.getElementsByTagName("headline").item(0).getTextContent();
            }
        }
        
		List<SocialFriend<?>> friends = new ArrayList<SocialFriend<?>>();
		try{
			DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document d = db.parse(new InputSource(new StringReader(json)));
			Element friendsList = d.getDocumentElement();
			validateResponse(friendsList, json);
			List<Element> persons = getElementList(friendsList.getElementsByTagName("person"));
			for(Element e : persons){
				LinkedInFriend lif = new LinkedInFriendInit(e);
				lif.parseSearchData();
				friends.add(lif);
			}
			return friends;
		} catch (ParserConfigurationException e) {
			throw new RuntimeException(e);
		} catch (SAXException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}catch(Exception e){
			throw new SocialNetworkUnavailableException(e);
		}
	}
	
	private School[] getEducation(Element edu) throws SocialNetworkUnavailableException{
	    class LinkedInSchool extends School {
	        public LinkedInSchool(Element e) {
                name = getFirstTextContentOrNull(e,"school-name");
                id = getFirstTextContentOrNull(e, "id");
                this.startDate = getMonth(e.getElementsByTagName("start-date"));
                this.endDate = getMonth(e.getElementsByTagName("end-date"));
                type = "High School";
                concentration = getFirstTextContentOrNull(e, "field-of-study");
	        }
	    }
	    
		try{
			List<Element> educations = getElementList(edu.getElementsByTagName("education"));
			School[] edus = new School[educations.size()];
			for(int i = 0;i<educations.size();i++) edus[i] = new LinkedInSchool(educations.get(i));
			return edus;
		}catch(Exception e){
			throw new SocialNetworkUnavailableException(e);
		}
	}
	
	private String getFirstTextContentOrNull(Element e,String tagName){
		NodeList list = e.getElementsByTagName(tagName);
		if(list.getLength() > 0){
			return list.item(0).getTextContent();
		}else{
			return null;
		}
	}
	
	private YearMonth getMonth(NodeList nl){
		try{
			if(nl.getLength() > 0){
				Element date = (Element) nl.item(0);
				return YearMonth.newForYYYYMM(date.getTextContent().replaceAll("\\s","")+"-01");
			}else{
				return null;
			}
		}catch(DOMException e){
			throw new RuntimeException(e);
		}catch(YearMonthParseException e){
			throw new RuntimeException(e);
		}
	}
	
	private Work[] getWork(Element w) throws SocialNetworkUnavailableException{
        class LinkedInWork extends Work {
            public LinkedInWork(Element e) throws YearMonthParseException {
                NodeList companyList = e.getElementsByTagName("company");
                if(companyList.getLength() > 0){
                    Element company = (Element) companyList.item(0);
                    employer = getFirstTextContentOrNull(company,"name");
                }
                position = getFirstTextContentOrNull(e, "title");
                List<Element> start = getElementList(e.getElementsByTagName("start-date"));
                if(start.size() > 0)
                    this.startDate = getYearMonthForWork(start.get(0));
                
                boolean isCurrent = false;
                NodeList currentJobList = e.getElementsByTagName("is-current");
                if(currentJobList.getLength() > 0 ){
                    Element currentJob = (Element) currentJobList.item(0);
                    if(currentJob.getTextContent().equals("true")) isCurrent = true;
                }
                if(!isCurrent){
                    List<Element> end = getElementList(e.getElementsByTagName("end-date"));
                    this.endDate = getYearMonthForWork(end.get(0));
                }
                this.description = getFirstTextContentOrNull(e, "summary");
            }
        }

	    try{
			List<Element> works = getElementList(w.getElementsByTagName("position"));
			Work[] work = new Work[works.size()];
			for(int i = 0;i<works.size();i++){
				work[i] = new LinkedInWork(works.get(i));
			}
			return work;
		}catch(Exception e){
			throw new SocialNetworkUnavailableException(e);
		}
	}
	
	private YearMonth getYearMonthForWork(Element e) throws YearMonthParseException{
		String year = getFirstTextContentOrNull(e, "year");
		String month = getFirstTextContentOrNull(e, "month");
		if(year != null && month != null){
			return YearMonth.newForYYYYMM(year.replaceAll("\\s", "") + "-" + String.format("%02d", Integer.parseInt(month.replaceAll("\\s", ""))));
		}else{
			return null;
		}
	}

	private List<Element> getElementList(NodeList nl){
		List<Element> le = new ArrayList<Element>();
		for(int i = 0;i<nl.getLength();i++){
			if(nl.item(i) instanceof Element) le.add((Element)nl.item(i));
		}
		return le;
	}
	
	@Override
	public SocialUser<LinkedInUserId> getUserInformation(String json) throws SocialNetworkUnavailableException{
	    class LinkedInSocialUserInit extends LinkedInSocialUser {
	        public LinkedInSocialUserInit(Element person) throws SocialNetworkUnavailableException {
                firstName = getElementList(person.getElementsByTagName("first-name")).get(0).getTextContent();
                lastName = getElementList(person.getElementsByTagName("last-name")).get(0).getTextContent();
                id = new LinkedInUserId(person.getElementsByTagName("id").item(0).getTextContent());
                education = LinkedInParser.this.getEducation((Element)person.getElementsByTagName("educations").item(0));
                work = LinkedInParser.this.getWork((Element)person.getElementsByTagName("positions").item(0));
                pictureUrl = person.getElementsByTagName("picture-url").getLength() == 0 ? null 
                        : person.getElementsByTagName("picture-url").item(0).getTextContent();
                setEmailAddress(person.getElementsByTagName("email-address").getLength() == 0 ? null
                        : person.getElementsByTagName("email-address").item(0).getTextContent());
                street = getFirstTextContentOrNull(person, "main-address");
                profileUrl = person.getElementsByTagName("public-profile-url").getLength() == 0 ? null
                        : person.getElementsByTagName("public-profile-url").item(0).getTextContent();
                NodeList languagesList = person.getElementsByTagName("languages");
                if(languagesList.getLength() > 0 ){
                    String[] langs = getTextContents(((Element)languagesList.item(0)).getElementsByTagName("name"));
                    Language[] l = new Language[langs.length];
                    for(int i = 0;i < langs.length;i++){
                        l[i] = new Language(langs[i], "");
                    }
                    languages = l;
                }else{
                    languages = new Language[0];
                }
                
                Node pictureUrls = person.getElementsByTagName("picture-urls").getLength() > 0 ? 
                        person.getElementsByTagName("picture-urls").item(0) : null;
                if(pictureUrls != null && ((Element)pictureUrls).getAttribute("total") != "0"){
                    for(int i = 0;i<pictureUrls.getChildNodes().getLength();i++){
                        if(pictureUrls.getChildNodes().item(i) instanceof Element){
                            Element e = (Element) pictureUrls.getChildNodes().item(i);
                            String url = e.getTextContent();
                            originalImage = url;
                        }
                    }
                }
                
                NodeList phoneList = person.getElementsByTagName("phone-numbers");
                if(phoneList.getLength() > 0){
                    NodeList phoneNumbers = phoneList.item(0).getChildNodes();
                    for(int i = 0;i<phoneNumbers.getLength();i++){
                        if(!(phoneNumbers.item(i) instanceof Element)) continue;
                        Element e = (Element) phoneNumbers.item(i);
                        if(e.getElementsByTagName("phone-type").getLength() > 0){
                            if(e.getElementsByTagName("phone-type").item(0).getTextContent().equals("home")){
                                this.mobilePhone = e.getElementsByTagName("phone-number").item(0).getTextContent();
                            }
                        }
                    }
                }
                
                NodeList skillList = person.getElementsByTagName("skills");
                if(skillList.getLength() > 0){
                    skillList = skillList.item(0).getChildNodes();
                    List<String> skills = new ArrayList<String>();
                    for(int i = 0;i<skillList.getLength();i++){
                        if(skillList.item(i) instanceof Element){
                            NodeList nameList = ((Element)skillList.item(i)).getElementsByTagName("name");
                            if(nameList.getLength() > 0){
                                skills.add(nameList.item(0).getTextContent());
                            }
                        }
                    }
                    qualifications = skills.toArray(new String[0]);
                }
	        }
	    }
	    
		try {
			DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document d = db.parse(new InputSource(new StringReader(json)));
			Element person = d.getDocumentElement();
			validateResponse(person, json);
			LinkedInSocialUser lif = new LinkedInSocialUserInit(person);
			return lif;
		} catch (ParserConfigurationException e) {
			throw new RuntimeException(e);
		} catch (SAXException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}catch(Exception e){
			throw new SocialNetworkUnavailableException(e);
		}
	}

    @Override
    protected SocialNetworkPostId getPostId(String response) throws SocialNetworkUserException {
        return new SocialNetworkPostId(0);
    }

    private String[] getTextContents(NodeList nl){
		List<String> textContents = new ArrayList<String>();
		if(nl.getLength() > 0){
			for(int i = 0;i<nl.getLength();i++){
				if(nl.item(i) instanceof Element){
					textContents.add(nl.item(i).getTextContent());
				}
			}
			return textContents.toArray(new String[0]);
		}else return new String[0];
	}
	
	private void validateResponse(Element errorCandidate,String text){
		if(errorCandidate.getElementsByTagName("error").getLength() != 0){
//			String message = errorCandidate.getElementsByTagName("message").item(0).getTextContent();
//			Gson gson = new Gson();
//			Map m = gson.fromJson(message, Map.class);
//			int errorCode = Integer.parseInt(m.get("errorCode").toString());
//			if(errorCode == 1000 || errorCode == 1001) throw new RuntimeException("Invalid URL parameter or bad request");
			throw new RuntimeException("Bad Request: " + text);
		}
	}

}
