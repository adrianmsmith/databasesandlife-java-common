package com.databasesandlife.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;

public abstract class EmailTransaction {

    protected final List<Message> messages = new ArrayList<Message>();

    public abstract void commit();

    protected Properties newSessionProperties() {
        return new Properties();
    }
    
    protected Session newSession() {
        return Session.getDefaultInstance(newSessionProperties());
    }
    
    public MimeMessage newMimeMessage() {
        return new MimeMessage(newSession());
    }
    
    public void send(Message msg) {
        this.messages.add(msg);
    }
    
    public int getEmailCountForTesting() { return messages.size(); }
    public String getEmailBodyForTesting(int idx) {
        try { return (String) messages.get(idx).getContent(); }
        catch (IOException e) { throw new RuntimeException(e); }
        catch (MessagingException e) { throw new RuntimeException(e); }
    }
}
