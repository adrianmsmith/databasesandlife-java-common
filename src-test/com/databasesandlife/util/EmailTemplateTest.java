package com.databasesandlife.util;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.mail.internet.InternetAddress;

import junit.framework.TestCase;

import com.databasesandlife.util.EmailTemplate.ByteArrayAttachment;
import com.databasesandlife.util.emailtemplatetest.MyEmailTemplate;

public class EmailTemplateTest extends TestCase {
    
    String recipient = "Mr Unit Tester <adrian.m.smith@gmail.com>";
    
    public void test() throws Exception {
        Map<String,String> params = new HashMap<String,String>();
        params.put("NAME", "Adrian \u263c < >");
        
        EmailTransaction tx = new EmailTransaction("localhost");
        
        new MyEmailTemplate().send(tx, new InternetAddress(recipient), new Locale("de"),
            params, new ByteArrayAttachment("attachment.txt", "text/plain", "Hello".getBytes()));
        System.out.println("One email has been sent to '" + recipient + "'.");
        
        EmailTemplate.setLastBodyForTestingInsteadOfSendingEmails();
        new MyEmailTemplate().send(tx, new InternetAddress(recipient), new Locale("de"),
            params, new ByteArrayAttachment("attachment.txt", "text/plain", "Hello".getBytes()));
        assertTrue(EmailTemplate.getLastBodyForTesting().contains("alternative"));
        
        tx.commit();
    }
}
