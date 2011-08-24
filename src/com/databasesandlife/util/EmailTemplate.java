package com.databasesandlife.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

public class EmailTemplate {
    
    protected Package directory;
    
    public static class FileNotFoundInEmailTemplateDirectoryException extends RuntimeException {
        public FileNotFoundInEmailTemplateDirectoryException(String msg) { super(msg); }
    }
    
    protected class FileAttachmentDataSource implements DataSource {
        String leafNameWithoutExtension, extension;
        FileAttachmentDataSource(String l, String e) { leafNameWithoutExtension=l; extension=e; }
        public String getContentType() {
            if ("jpg".equalsIgnoreCase(extension)) return "image/jpeg";
            if ("png".equalsIgnoreCase(extension)) return "image/png";
            throw new RuntimeException("Extension '" + extension + "' unknown");
        }
        public String getName() { return leafNameWithoutExtension + "." + extension; }
        public InputStream getInputStream() { return newInputStreamForBinaryFile(getName()); }
        public OutputStream getOutputStream() { throw new RuntimeException(); }
    }
    
    public EmailTemplate(Package directory) {
        this.directory = directory;
    }
    
    protected InputStream newInputStreamForBinaryFile(String leafName) throws FileNotFoundInEmailTemplateDirectoryException {
        String name = directory.getName().replaceAll("\\.", "/"); // e.g. "com/myproject/mtpl/registrationemail"
        InputStream i = getClass().getClassLoader().getResourceAsStream(name + "/" + leafName);
        if (i == null) throw new FileNotFoundInEmailTemplateDirectoryException(
            "File '" + leafName +"' not found in email tpl package '" + directory + "'");
        return i;
    }
    
    protected String readTextFile(String leafName) throws FileNotFoundInEmailTemplateDirectoryException {
        try {
            InputStream i = newInputStreamForBinaryFile(leafName);
            return InputOutputStreamUtil.readStringFromReader(new InputStreamReader(i, "UTF-8"));
        }
        catch (IOException e) { throw new RuntimeException(e); }
    }
    
    /**
     * @param language for example "de"
     */
    public void sendEmailTo(String recipientEmailAddress, String language, Map<String,String> parameters) {
        try {
            boolean bodyFound = false;
            Multipart messageBody = new MimeMultipart("alternative");
            
            // Read the subject; determine if we need to fallback to "en" in case the desired language isn't present in the template
            String subject;
            try {
                subject = readTextFile("subject-" + language + ".txt");
            }
            catch (FileNotFoundInEmailTemplateDirectoryException e) {
                language = "en";
                subject = readTextFile("subject-" + language + ".txt");
            }
            for (Entry<String,String> paramEntry : parameters.entrySet()) 
                subject = subject.replace("${" + paramEntry.getKey() + "}", paramEntry.getValue());

            // Add the text part (if present)
            try {
                String textContents = readTextFile("body-" + language + ".txt");
                for (Entry<String,String> paramEntry : parameters.entrySet()) 
                    textContents = textContents.replace("${" + paramEntry.getKey() + "}", paramEntry.getValue());
                
                BodyPart textPart = new MimeBodyPart();
                textPart.setText(textContents);
                
                messageBody.addBodyPart(textPart);
            }
            catch (FileNotFoundInEmailTemplateDirectoryException e) { }
            
            // Add the HTML part (if present)
            try {
                String htmlContents = readTextFile("body-" + language + ".html");
                
                for (Entry<String,String> paramEntry : parameters.entrySet()) {
                    String paramKey = paramEntry.getKey();
                    String paramValue = paramEntry.getValue();
                    if ( ! paramKey.matches("^.*_HTML$")) paramValue = WebEncodingUtils.encodeHtml(paramValue); 
                    htmlContents = htmlContents.replace("${" + paramKey + "}", paramValue);
                }
                
                Map<String, BodyPart> referencedFiles = new TreeMap<String, BodyPart>();
                StringBuffer htmlContentsWithCid = new StringBuffer();
                Matcher fileMatcher = Pattern.compile("(['\"])([\\w\\-]+)\\.(\\w{3,4})['\"]").matcher(htmlContents);
                while (fileMatcher.find()) {
                    String quote = fileMatcher.group(1);
                    String leafNameWithoutExtension = fileMatcher.group(2);
                    String extension = fileMatcher.group(3);
                    String leafNameWithExtension = leafNameWithoutExtension + "." + extension;
                    
                    BodyPart filePart = new MimeBodyPart();
                    DataSource source = new FileAttachmentDataSource(leafNameWithoutExtension, extension);
                    filePart.setDataHandler(new DataHandler(source));
                    filePart.setFileName(leafNameWithExtension);
                    filePart.setHeader("Content-ID", "<"+leafNameWithExtension+">");
                    filePart.setDisposition(Part.INLINE);

                    referencedFiles.put(leafNameWithExtension, filePart);
                    fileMatcher.appendReplacement(htmlContentsWithCid, quote + "cid:" + leafNameWithExtension + quote);
                }
                fileMatcher.appendTail(htmlContentsWithCid);

                BodyPart htmlContentsPart = new MimeBodyPart();
                htmlContentsPart.setContent(htmlContentsWithCid.toString(), "text/html");
                
                Multipart htmlMultiPart = new MimeMultipart("related");
                htmlMultiPart.addBodyPart(htmlContentsPart);
                for (BodyPart attachmentPart : referencedFiles.values())
                    htmlMultiPart.addBodyPart(attachmentPart);
                
                BodyPart htmlPart = new MimeBodyPart();
                htmlPart.setContent(htmlMultiPart);
                
                messageBody.addBodyPart(htmlPart);
            }
            catch (FileNotFoundInEmailTemplateDirectoryException e) { }
            
            // No bodies? Error
            if (messageBody.getCount() == 0) throw new RuntimeException("No bodies found for language='" + language + "'");

            // Create the message from the subject and body
            Properties props = new Properties();
            props.put("mail.smtp.host", "localhost");
            Session mailSession = Session.getDefaultInstance(props);
            Message msg = new MimeMessage(mailSession);
            msg.setFrom(new InternetAddress("adrian@uboot.com"));
            msg.addRecipient(RecipientType.TO, new InternetAddress(recipientEmailAddress));
            msg.setSubject(subject);
            msg.setContent(messageBody);
            
            // Send the message
            Transport.send(msg);
        }
        catch (MessagingException e) { throw new RuntimeException(e); }
    }
}
