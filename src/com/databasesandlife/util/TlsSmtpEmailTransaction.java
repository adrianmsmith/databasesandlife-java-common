package com.databasesandlife.util;

import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;

public class TlsSmtpEmailTransaction extends SmtpEmailTransaction {
    
    public static class SmtpEmailServerAddress {
        public String server;
        public int port = 25;
    }
    
    public static class TlsSmtpEmailServerAddress extends SmtpEmailServerAddress {
        public String username, password;
        public TlsSmtpEmailServerAddress() { port = 587; }
    }
    
    final SmtpEmailServerAddress config;

    public TlsSmtpEmailTransaction(SmtpEmailServerAddress config) {
        super(config.server);
        this.config = config;
    }

    @Override protected Properties newSessionProperties() {
        Properties props = super.newSessionProperties();
        props.put("mail.smtp.port", ""+config.port);
        props.put("mail.transport.protocol", "smtp");
        if (config instanceof TlsSmtpEmailServerAddress) {
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
        }
        return props;
    }

    @Override protected Session newSession() {
        if (config instanceof TlsSmtpEmailServerAddress) {
            TlsSmtpEmailServerAddress c = (TlsSmtpEmailServerAddress) config;
            Authenticator auth = new Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(c.username, c.password);
                }
            };
            return Session.getInstance(newSessionProperties(), auth);
        }
        else return super.newSession();
    }
}
