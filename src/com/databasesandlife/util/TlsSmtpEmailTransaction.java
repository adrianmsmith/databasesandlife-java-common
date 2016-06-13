package com.databasesandlife.util;

import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;

import com.databasesandlife.util.gwtsafe.ConfigurationException;

public class TlsSmtpEmailTransaction extends SmtpEmailTransaction {
    
    public static class SmtpEmailServerAddress {
        public String server;
        public int port = 25;
    }
    
    public static class TlsSmtpEmailServerAddress extends SmtpEmailServerAddress {
        public String username, password;
        public TlsSmtpEmailServerAddress() { port = 587; }
    }
    
    /** Can be "foo" or "foo:123" or "foo:123|adrian|password" */
    public static SmtpEmailServerAddress parseAddress(String str) throws ConfigurationException {
        Matcher m;
        
        m = Pattern.compile("^([^|:]+)(:(\\d+))?(\\|(.+)\\|(.+))?$").matcher(str);
        if (!m.matches()) throw new ConfigurationException("SMTP config '"+str+"' not understood");
        
        SmtpEmailServerAddress result;
        if (m.group(4) != null) {
            result = new TlsSmtpEmailServerAddress();
            ((TlsSmtpEmailServerAddress)result).username = m.group(5);
            ((TlsSmtpEmailServerAddress)result).password = m.group(6);
        } else {
            result = new SmtpEmailServerAddress();
        }
        
        result.server = m.group(1);
        if (m.group(2) != null) result.port = Integer.parseInt(m.group(3));
        
        return result;
    }
    
    final SmtpEmailServerAddress config;

    public TlsSmtpEmailTransaction(SmtpEmailServerAddress config) {
        super(config.server + ":" + config.port);
        this.config = config;
    }

    @Override protected Properties newSessionProperties() {
        Properties props = super.newSessionProperties();
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
