package com.databasesandlife.util.socialnetwork.google;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import com.databasesandlife.util.socialnetwork.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.databasesandlife.util.socialnetwork.SocialNetworkPostId;

@SuppressWarnings("serial")
public class GoogleParser extends SocialParser{
    
    /**
     * The method is used to parse the xml returned by the {@link GoogleClient#getInformations(org.scribe.model.Token)}
     * and will return the Contacts.<br>
     * To get contacts which do not have emails please see {@link GoogleParser#getContacts(String, boolean)}<br>
     * This method will call {@link GoogleParser#getContacts(String, boolean)} with <b>true</b>
     * @param xml
     * @return a list with {@link GoogleContact} Objects
     */
    public List<SocialFriend<?>> getContacts(String xml) throws SocialNetworkUnavailableException{
        return getContacts(xml,true);
    }
    
    public List<SocialFriend<?>> getContacts(String xml,boolean skipContactsWithoutEmail) throws SocialNetworkUnavailableException{
        DocumentBuilder db;
        List<SocialFriend<?>> contacts = new ArrayList<SocialFriend<?>>();
        try {
            db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document doc = db.parse(new InputSource(new StringReader(xml)));
            List<Element> entries = getElementList(doc.getDocumentElement().getElementsByTagName("entry"));
            for(Element entry : entries){
                GoogleContact gc = new GoogleContact();
                NodeList ea = entry.getElementsByTagName("gd:email");
                if(ea.getLength() > 0){
                    gc.emailAdress = ((Element)ea.item(0)).getAttribute("address");
                }else if(skipContactsWithoutEmail) continue;
                NodeList fn = entry.getElementsByTagName("gd:givenName");
                NodeList ln = entry.getElementsByTagName("gd:familyName");
                if(fn.getLength() > 0) gc.firstName = fn.item(0).getTextContent();
                else if(ln.getLength() == 0) gc.firstName = gc.emailAdress;
                if(ln.getLength() > 0) gc.lastName = ln.item(0).getTextContent();
                else gc.lastName = "";
                gc.setId(new GoogleContactId(gc.emailAdress));
                contacts.add(gc);
            }
            return contacts;
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
    
    private List<Element> getElementList(NodeList l){
        List<Element> el = new ArrayList<Element>();
        for(int i = 0;i<l.getLength();i++){
            if(l.item(i) instanceof Element){
                el.add((Element)l.item(i));
            }
        }
        return el;
    }

    @Override
    public List<SocialFriend<?>> getFriends(String json)
            throws SocialNetworkUnavailableException {
        return getContacts(json);
    }

    @Override
    public SocialUser<GoogleContactId> getUserInformation(String xml) throws SocialNetworkUnavailableException {
        class GoogleSocialUser extends SocialUser<GoogleContactId> {
            public GoogleSocialUser(Document doc){
                this.id = new GoogleContactId(doc.getDocumentElement().getElementsByTagName("id").item(0).getTextContent());
            }
            @Override public Work[] getWork() { return null; }
            @Override public School[] getEducation() { return null; }
        }
        
        try {
            DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            return new GoogleSocialUser(db.parse(new InputSource(new StringReader(xml))));
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

}
