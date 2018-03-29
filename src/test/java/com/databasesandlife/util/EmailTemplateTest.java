package com.databasesandlife.util;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.mail.internet.InternetAddress;

import junit.framework.TestCase;

import com.databasesandlife.util.EmailTemplate.ByteArrayAttachment;
import com.databasesandlife.util.emailtemplatetest.MyEmailTemplate;

/**
 * @author This source is copyright <a href="http://www.databasesandlife.com">Adrian Smith</a> and licensed under the LGPL 3.
 * @see <a href="https://github.com/adrianmsmith/databasesandlife-java-common">Project on GitHub</a>
 */
public class EmailTemplateTest extends TestCase {
    
    String recipient = "Mr Unit Tester <adrian.m.smith@gmail.com>";
    
    public void test() throws Exception {
        Map<String,String> params = new HashMap<>();
        params.put("NAME", "Adrian \u263c < >");
        
        EmailTransaction tx = new EmailTransaction(EmailTransaction.parseAddress("localhost"));
        
//        new MyEmailTemplate().send(tx, new InternetAddress(recipient), new Locale("de"),
//            params, new ByteArrayAttachment("attachment.txt", "text/plain", "Hello".getBytes()));
//        System.out.println("One email has been sent to '" + recipient + "'.");
        
        EmailTemplate.setLastBodyForTestingInsteadOfSendingEmails();
        new MyEmailTemplate().send(tx, new InternetAddress(recipient), new Locale("de"),
            params, new ByteArrayAttachment("attachment.txt", "text/plain", "Hello".getBytes(StandardCharsets.UTF_8)));
        assertTrue(EmailTemplate.getLastBodyForTesting().contains("alternative"));
        
        tx.commit();
    }
}
