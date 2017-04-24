package com.databasesandlife.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.Message.RecipientType;
import javax.mail.internet.MimeMessage;

import org.xbill.DNS.Lookup;
import org.xbill.DNS.MXRecord;
import org.xbill.DNS.Record;
import org.xbill.DNS.TextParseException;
import org.xbill.DNS.Type;

import com.databasesandlife.util.gwtsafe.ConfigurationException;

/**
 * An transaction allowing emails to be sent over SMTP
 *   <p>
 * This object allows emails to take part in <b>transactional</b> operations.
 * Any part of the code may {@link #send(Message)} emails using this transaction.
 * They get stored in memory, and only really get sent when {@link #commit()} is called.
 * This way, if an exception occurs after the email gets sent, and the operation is successfully
 * retried, the recipient only gets one email based on the successful operation and no emails
 * based on the unsuccessful operation.
 *   <p>
 * There is no need to <b>rollback</b> this object if an operation has been not successful.
 * Simply throw the object away.
 *
 * @author This source is copyright <a href="http://www.databasesandlife.com">Adrian Smith</a> and licensed under the LGPL 3.
 * @see <a href="https://github.com/adrianmsmith/databasesandlife-java-common">Project on GitHub</a>
 */
public class EmailTransaction {
    
    // ------------------------------------------------------------------------
    // Configuration classes
    // ------------------------------------------------------------------------
    
    public static abstract class SmtpServerConfiguration { }

    public static class MxSmtpConfiguration extends SmtpServerConfiguration {
        public String mxAddress;
    }
    
    public static class SmtpServerAddress extends SmtpServerConfiguration {
        public String host;
        public int port = 25;
    }
    
    public static class TlsSmtpServerAddress extends SmtpServerAddress {
        public String username, password;
        public TlsSmtpServerAddress() { port = 587; }
    }
    
    // ------------------------------------------------------------------------
    // State
    // ------------------------------------------------------------------------
    
    protected final SmtpServerConfiguration server;
    protected final List<Message> messages = new ArrayList<>();

    // ------------------------------------------------------------------------
    // Internal methods
    // ------------------------------------------------------------------------
    
    protected static String getHostForMxRecord(String mxAddress) throws ConfigurationException {
        try {
            Record[] r = new Lookup(mxAddress, Type.MX).run(); // null, or array with at least one element
            if (r == null) throw new ConfigurationException("No results found for MX lookup '"+mxAddress+"'");
            MXRecord[] records = Arrays.copyOf(r, r.length, MXRecord[].class);
            Arrays.sort(records, (x,y) -> (Integer.compare(y.getPriority(), x.getPriority())));
            return records[0].getTarget().toString(true); // true is omit final dot
        }
        catch (TextParseException e) { throw new ConfigurationException(e); }
    }
    
    protected Properties newSessionProperties() throws ConfigurationException {
        Properties props = new Properties();
        props.put("mail.transport.protocol", "smtp");
        
        if (server instanceof MxSmtpConfiguration) {
            props.put("mail.smtp.host", getHostForMxRecord(((MxSmtpConfiguration)server).mxAddress));
        }
        if (server instanceof SmtpServerAddress) {
            props.put("mail.smtp.host", ((SmtpServerAddress)server).host);
            props.put("mail.smtp.port", ((SmtpServerAddress)server).port);
        }
        if (server instanceof TlsSmtpServerAddress) {
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
        }
        return props;
    }

    protected Session newSession() throws ConfigurationException {
        if (server instanceof TlsSmtpServerAddress) {
            Authenticator auth = new Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    TlsSmtpServerAddress c = (TlsSmtpServerAddress) server;
                    return new PasswordAuthentication(c.username, c.password);
                }
            };
            return Session.getInstance(newSessionProperties(), auth);
        }
        else {
            return Session.getDefaultInstance(newSessionProperties());
        }
    }
    
    // ------------------------------------------------------------------------
    // Public methods
    // ------------------------------------------------------------------------
    
    public EmailTransaction(SmtpServerConfiguration server) throws ConfigurationException {
        this.server = server;
        newSessionProperties(); // do MX lookup to check it works
    }
    
    /** Can be "foo" or "foo:123" or "foo:123|adrian|password" or "MX:foo.com" */
    public static SmtpServerConfiguration parseAddress(String str) throws ConfigurationException {
        Matcher m;
        
        m = Pattern.compile("^MX:(.+)$").matcher(str);
        if (m.matches()) {
            MxSmtpConfiguration result = new MxSmtpConfiguration();
            result.mxAddress = m.group(1);
            return result;
        }
        
        m = Pattern.compile("^([^|:]+)(:(\\d+))?(\\|(.+)\\|(.+))?$").matcher(str);
        if (m.matches()) {
            SmtpServerAddress result;
            if (m.group(4) != null) {
                result = new TlsSmtpServerAddress();
                ((TlsSmtpServerAddress)result).username = m.group(5);
                ((TlsSmtpServerAddress)result).password = m.group(6);
            } else {
                result = new SmtpServerAddress();
            }
            
            result.host = m.group(1);
            if (m.group(2) != null) result.port = Integer.parseInt(m.group(3));
            
            return result;
        }
        
        throw new ConfigurationException("SMTP config '"+str+"' not understood");
    }

    public MimeMessage newMimeMessage() {
        try { return new MimeMessage(newSession()); }
        catch (ConfigurationException e) { throw new RuntimeException(e); }
    }
    
    public void send(Message msg) {
        this.messages.add(msg);
    }
    
    public void commit() {
        try (Timer t = new Timer(getClass().getSimpleName()+".commit")) {
            ThreadPool threads = new ThreadPool();
            threads.setThreadCount(3); // Have some parallelism but do not overload the remote SMTP server
            for (Message msg : messages) {
                threads.addTask(() -> {
                    try (Timer t2 = new Timer("Send email to '" + msg.getRecipients(RecipientType.TO)[0]+"'")) {
                        Transport.send(msg);
                    }
                    catch (MessagingException e) { throw new RuntimeException(e); }
                });
            }        
            threads.execute();
        }
    }
   
    public int getEmailCountForTesting() { return messages.size(); }
    public String getEmailBodyForTesting(int idx) {
        try { return (String) messages.get(idx).getContent(); }
        catch (IOException e) { throw new RuntimeException(e); }
        catch (MessagingException e) { throw new RuntimeException(e); }
    }
}
