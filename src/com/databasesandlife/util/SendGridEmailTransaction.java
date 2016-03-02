package com.databasesandlife.util;

import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;

public class SendGridEmailTransaction extends SmtpEmailTransaction {

    protected final String username, password;

    public SendGridEmailTransaction(String username, String password) {
        super("smtp.sendgrid.net");
        this.username = username;
        this.password = password;
    }

    @Override protected Properties newSessionProperties() {
        Properties props = super.newSessionProperties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.port", "587");
        props.put("mail.transport.protocol", "smtp");
        return props;
    }

    @Override protected Session newSession() {
        Authenticator auth = new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        };
        return Session.getInstance(newSessionProperties(), auth);
    }
}
