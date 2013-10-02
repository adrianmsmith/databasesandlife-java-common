package com.databasesandlife.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.MimeMessage;

/**
 * An transaction allowing emails to be sent.
 *   <p>
 * This object allows emails to take part in transactional operations.
 * Any part of the code may {@link #send(Message)} emails using this transaction.
 * They get stored in memory, and only really get sent when {@link #commit()} is called.
 * This way, if an exception occurs after the email gets sent, and the operation is successfully
 * retried, the recipient only gets one email based on the successful operation and no emails
 * based on the unsuccessful operation.
 */
public class EmailTransaction {
    
    protected String smtpServer;
    protected List<Message> messages = new ArrayList<Message>();
    
    public EmailTransaction(String smtpServer) {
        this.smtpServer = smtpServer;
    }
    
    public MimeMessage newMimeMessage() {
        Properties props = new Properties();
        props.put("mail.smtp.host", smtpServer);
        Session mailSession = Session.getDefaultInstance(props);
        return new MimeMessage(mailSession);
    }
    
    public void send(Message msg) {
        this.messages.add(msg);
    }
    
    public void commit() {
        Timer.start("EmailTransaction-commit");
        try {
            for (Message msg : messages) {
                Timer.start("Send email to '" + msg.getRecipients(RecipientType.TO)[0]+"'");
                try { Transport.send(msg); }
                finally { Timer.end("Send email to '" + msg.getRecipients(RecipientType.TO)[0]+"'"); }
            }
        }
        catch (MessagingException e) { throw new RuntimeException(e); }
        finally { Timer.end("EmailTransaction-commit"); }
    }
    
    public int getEmailCountForTesting() { return messages.size(); }
    public String getEmailBodyForTesting(int idx) {
        try { return (String) messages.get(idx).getContent(); }
        catch (IOException e) { throw new RuntimeException(e); }
        catch (MessagingException e) { throw new RuntimeException(e); }
    }
}
