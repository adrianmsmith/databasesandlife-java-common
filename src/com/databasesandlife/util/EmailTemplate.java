package com.databasesandlife.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.MimetypesFileTypeMap;
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
    
    /** For example "com.project.emailtpl.xyz" */
    protected String packageStr;
    
    public static class FileNotFoundInEmailTemplateDirectoryException extends RuntimeException {
        public FileNotFoundInEmailTemplateDirectoryException(String msg) { super(msg); }
    }
    
    protected class FileAttachmentDataSource implements DataSource {
        String leafNameWithoutExtension, extension;
        FileAttachmentDataSource(String l, String e) { leafNameWithoutExtension=l; extension=e; }
        public String getContentType() { return new MimetypesFileTypeMap().getContentType(getName()); }
        public String getName() { return leafNameWithoutExtension + "." + extension; }
        public InputStream getInputStream() { return newInputStreamForBinaryFile(getName()); }
        public OutputStream getOutputStream() { throw new RuntimeException(); }
    }
    
    public EmailTemplate(Package pkg) {
        this.packageStr = pkg.getName();
    }
    
    public EmailTemplate(String pkgStr) {
        this.packageStr = pkgStr;
    }
    
    protected InputStream newInputStreamForBinaryFile(String leafName) 
    throws FileNotFoundInEmailTemplateDirectoryException {
        String packageWithSlashes = packageStr.replaceAll("\\.", "/"); // e.g. "com/myproject/mtpl/registrationemail"
        InputStream i = getClass().getClassLoader().getResourceAsStream(packageWithSlashes + "/" + leafName);
        if (i == null) throw new FileNotFoundInEmailTemplateDirectoryException(
            "File '" + leafName +"' not found in email tpl package '" + packageStr + "'");
        return i;
    }
    
    protected String readTextFile(String leafName) 
    throws FileNotFoundInEmailTemplateDirectoryException {
        try {
            InputStream i = newInputStreamForBinaryFile(leafName);
            return InputOutputStreamUtil.readStringFromReader(new InputStreamReader(i, "UTF-8"));
        }
        catch (IOException e) { throw new RuntimeException(e); }
    }
    
    protected String readLocaleTextFile(String leafNameStem, Locale locale, String extension) 
    throws FileNotFoundInEmailTemplateDirectoryException {
        try {
            return readTextFile(leafNameStem + "_" + locale.getLanguage() + "." + extension);
        }
        catch (FileNotFoundInEmailTemplateDirectoryException e) {
            return readTextFile(leafNameStem + "." + extension);
        }
    }
    
    protected String replacePlainTextParameters(String template, Map<String,String> parameters) {
        for (Entry<String,String> paramEntry : parameters.entrySet()) 
            template = template.replace("${" + paramEntry.getKey() + "}", paramEntry.getValue());
        return template;
    }
    
    public void sendEmailTo(String smtpServer, InternetAddress recipientEmailAddress, Locale locale, Map<String,String> parameters) {
        try {
            // Read the subject
            String subject = readLocaleTextFile("subject", locale, "txt");
            subject = replacePlainTextParameters(subject, parameters);
            
            // Read the plain/text part
            BodyPart plainTextBodyPart;
            try {
                String textContents = readLocaleTextFile("body", locale, "txt");
                textContents = replacePlainTextParameters(textContents, parameters);
                
                plainTextBodyPart = new MimeBodyPart();
                plainTextBodyPart.setText(textContents);
            }
            catch (FileNotFoundInEmailTemplateDirectoryException e) {
                plainTextBodyPart = null;
            }
            
            // Read the HTML part
            BodyPart htmlBodyPart;
            try {
                String htmlContents = readLocaleTextFile("body", locale, "html");
                
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
                htmlContentsPart.setContent(htmlContentsWithCid.toString(), "text/html; charset=UTF-8");
                
                Multipart htmlMultiPart = new MimeMultipart("related");
                htmlMultiPart.addBodyPart(htmlContentsPart);
                for (BodyPart attachmentPart : referencedFiles.values())
                    htmlMultiPart.addBodyPart(attachmentPart);
                
                htmlBodyPart = new MimeBodyPart();
                htmlBodyPart.setContent(htmlMultiPart);
            }
            catch (FileNotFoundInEmailTemplateDirectoryException e) {
                htmlBodyPart = null;
            }
            
            // Create the "message body" which is the multipart/alternative of the plain/text and HTML versions
            Multipart messageBody = new MimeMultipart("alternative");
            if (plainTextBodyPart == null && htmlBodyPart == null)
                throw new RuntimeException("No bodies for email template: " + packageStr);
            if (plainTextBodyPart != null) messageBody.addBodyPart(plainTextBodyPart);
            if (htmlBodyPart != null) messageBody.addBodyPart(htmlBodyPart);

            // Create the message from the subject and body
            Properties props = new Properties();
            props.put("mail.smtp.host", smtpServer);
            Session mailSession = Session.getDefaultInstance(props);
            Message msg = new MimeMessage(mailSession);
            msg.setFrom(new InternetAddress(readLocaleTextFile("from", locale, "txt")));
            msg.addRecipient(RecipientType.TO, recipientEmailAddress);
            msg.setSubject(subject);
            msg.setContent(messageBody);
            
            // Send the message
            Transport.send(msg);
        }
        catch (MessagingException e) { throw new RuntimeException(e); }
    }
}
