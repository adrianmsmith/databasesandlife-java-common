package com.databasesandlife.util;

import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;

public class MandrillEmailTransaction extends EmailTransaction {

    protected final String username;
    protected final String apiKey;

    public MandrillEmailTransaction(String username, String apiKey) {
        super("smtp.mandrillapp.com");
        this.username = username;
        this.apiKey = apiKey;
    }

    @Override protected Properties newSessionProperties() {
        Properties props = super.newSessionProperties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.port", "587");
        props.put("mail.debug", "true");
        props.put("mail.transport.protocol", "smtp");
        return props;
    }

    @Override protected Session newSession() {
        Authenticator auth = new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, apiKey);
            }
        };
        return Session.getInstance(newSessionProperties(), auth);
    }
}
