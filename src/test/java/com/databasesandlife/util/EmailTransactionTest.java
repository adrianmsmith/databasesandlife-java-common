package com.databasesandlife.util;

import com.databasesandlife.util.EmailTransaction.EmailSendingConfiguration;
import junit.framework.TestCase;

import javax.mail.Message;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Date;

public class EmailTransactionTest extends TestCase {

    public void testSend() throws Exception {
        EmailSendingConfiguration config = new EmailSendingConfiguration(EmailTransaction.parseAddress("localhost"));
        config.extraHeaders.put("X-Unittest-Header", "Foo");
        EmailTransaction tx = new EmailTransaction(config);

        MimeMessage msg = tx.newMimeMessage();
        msg.setFrom(new InternetAddress("example@example.com"));
        msg.addRecipient(Message.RecipientType.TO, new InternetAddress("adrian.m.smith@gmail.com"));
        msg.setSubject(getClass().getName());
        msg.setText("This is an email");
        msg.setSentDate(new Date());

        tx.send(msg);
//        tx.commit();
    }
}