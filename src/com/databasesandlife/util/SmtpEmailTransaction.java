package com.databasesandlife.util;

import java.util.Properties;

import javax.mail.Message;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.Transport;

/**
 * An transaction allowing emails to be sent.
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
 */
public class SmtpEmailTransaction extends EmailTransaction {
    
    protected final String smtpServer;
    
    public SmtpEmailTransaction(String smtpServer) {
        this.smtpServer = smtpServer;
    }
    
    protected Properties newSessionProperties() {
        Properties props = super.newSessionProperties();
        props.put("mail.smtp.host", smtpServer);
        return props;
    }
    
    @Override public void commit() {
        for (Message msg : messages) {
            try (Timer t = new Timer(getClass().getSimpleName()+".commit: Send email to '" + msg.getRecipients(RecipientType.TO)[0]+"'")) {
                Transport.send(msg);
            }
            catch (MessagingException e) { throw new RuntimeException(e); }
        }
    }
}
