package com.databasesandlife.util;

import java.io.*;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
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
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.log.Log4JLogChute;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.apache.velocity.tools.generic.EscapeTool;

import com.google.gson.Gson;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**

Represents a directory in the classpath, which contains text and potentially graphics, in multiple languages and multiple formats, for outgoing notification emails.

<p>A directory within the classpath should be created, and filled with the following files:</p>

<p><ul>
        <li><b>body.velocity.utf8.txt</b> - UTF-8 formatted body of the text/plain part of the email to be sent; Velocity template</li>
        <li><b>body.velocity.utf8.html</b> - UTF-8 formatted HTML version of the email to be sent; Velocity template</li>
        <li><b>subject.velocity.utf8.txt</b> - UTF-8 formatted subject of the email to be sent; Velocity template</li>
        <li><b>from.velocity.utf8.txt </b>- An file containing an email address such as "John Smith &lt;bar@example.com&gt;"; Velocity template</li>
        <li><b>xyz.jpg</b> - Any resources required from the HTML version of the emails. They are referenced simply as &lt;img src="xyz.jpg"&gt; from the HTML versions, so the HTML version can be easily tested locally in a browser. This is replaced by &lt;img src="cid:xyz.jpg"&gt; by the software, as this is what is required in the email.</li>
        <li>Optionally <b>MyEmailTemplate.java</b> - Subclass of EmailTemplate, means that the whole template directory can be referenced via static typing, can be renamed with refactoring tools, and so on.</li>
</ul>

<p>Concerning <b>languages</b>, although e.g. "subject.utf8.txt" must be present, there may also be files with names such as "subject_de.utf8.txt" files for other Locales.</p>

<p>One or both of the <b>plain-text</b> and <b>HTML</b> versions of the email must be present. If they are both present then a "multipart/alternative" email is sent.</p>

<p>The templates are <b>Velocity templates</b> meaning that variables like <code>${XYZ}</code> can be used.
Velocity supports <code>#foreach</code> etc. 
For variables in HTML files use <code>$esc.html($xyz)</code>.

<p>For <b>unit testing</b>, use the static method {@link #setLastBodyForTestingInsteadOfSendingEmails()}.
After that method has been called, no emails will be sent,
instead the method {@link #getLastBodyForTesting} may be used to retrieve the last sent plain/text email body.
This allows one to assert that particular emails would be sent, and that they contain particular text.</p>

<p>For <b>testing with e.g. Litmus</b> (tools to test your emails across the many email clients),
 the facility {@link #writeHtmlPartToFile(Locale, URL, Map, File)} 
exists.
Emails can be written to disk as opposed to sent, in order that the real email that would be sent can be uploaded to 
Litmus for testing, without any velocity template commands, and with real user data.</p>

<p>Writing code such as <code>new EmailTemplate("myproject.mtpl.registration")</code> has the disadvantage that if that package is renamed, <b>refactoring tools</b> will not see this string, and not rename it. Errors will result at run-time. The solution is to create a class in the directory, which calls its superclass constructor with its package name. This class is then instanciated in the client code, instead of the general <code>EmailTemplate</code>.</p>

<p>You can send <b>attachments</b> with your email (e.g. PDF invoices) by passing multiple {@link Attachment} objects to the send method.
Either you implement your own attachment, providing the filename, mime type and a way to get an InputStream for the bytes of the attachment,
or you can just create a {@link ByteArrayAttachment} by passing the filename, mime type and a <code>byte[]</code>.</p>

<p>Concerning <b>naming</b>,</p>

<p><ul>
        <li>"Email" is used over Mail, in order to be consistent with the term "email address", which is never called "mail address".</li>
        <li>The word "template" is always used, as an "email" is a particular email which has been sent, whereas an object of this class represents the possibility to create such emails, i.e. is a template for such emails.</li>
        <li>"Email address" is used in preference to "email", as an "email" is the message which is sent, and it is necessary to <a href="http://www.databasesandlife.com/an-email-address-field-should-not-be-called-email/">differentiate</a> between the address to which a message is sent, and the message itself.</li>
        <li>"utf8" is used in the filenames to make absolutely clear to all concerned what encoding should be used for the contents of those files.</li>
</ul>

<h3>Usage</h3>

<p>In the directory containing the template files:
<pre>
class RegistrationEmailTemplate extends EmailTemplate {
   public RegistrationEmailTemplate() {
     super(RegistrationEmailTemplate.class.getPackage());
   }
   // other methods can be added, specific to this email template
}
</pre>
<p>In client code:
<pre>
class RegistrationProcess {
  public registerNewUser(InternetAddress emailAddress, Locale language, String name, ... ) {
    String smtpServer = "localhost";
    EmailTransaction tx = new EmailTransaction(smtpServer);

    Map&lt;String,String&gt; params = new HashMap&lt;String,String&gt;();
    params.put("USERNAME", name);

    RegistrationEmailTemplate tpl = new RegistrationEmailTemplate();
    tpl.send(tx, recipientEmailAddress, language, params);
    
    tx.commit();
  }
}
</pre>
<p>In unit test code:
<pre>
class RegistrationProcessTest extends TestCase {
  public testRegisterNewUser() {
    EmailTemplate.setLastBodyForTestingInsteadOfSendingEmails();
    new RegistrationProcess().registerNewUser("test@example.com", "Adrian");
    String txt = EmailTemplate.getLastBodyForTesting();
    assertTrue(txt.contains("Adrian"));
  }
}
</pre>

 * @author This source is copyright <a href="http://www.databasesandlife.com">Adrian Smith</a> and licensed under the LGPL 3.
 * @see <a href="https://github.com/adrianmsmith/databasesandlife-java-common">Project on GitHub</a>
 */
@SuppressWarnings("serial")
public class EmailTemplate {

    // --------------------------------------------------------------------------------------------------------
    // Internal
    // --------------------------------------------------------------------------------------------------------

    /** For example "com.project.emailtpl.xyz" */
    protected String packageStr;

    static protected boolean setLastBodyForTestingInsteadOfSendingEmails = false;
    static protected String lastBodyForTesting = "";

    protected class FileAttachmentJavamailDataSource implements DataSource {
        String leafNameWithoutExtension, extension;
        FileAttachmentJavamailDataSource(String l, String e) { leafNameWithoutExtension=l; extension=e; }
        public String getContentType() { return new MimetypesFileTypeMap().getContentType(getName()); }
        public String getName() { return leafNameWithoutExtension + "." + extension; }
        public InputStream getInputStream() { return getClass().getClassLoader().getResourceAsStream(findFile(getName())); }
        public OutputStream getOutputStream() { throw new RuntimeException(); }
    }

    /** @return full classpath name to the file */
    protected String findFile(String leafName)
    throws FileNotFoundInEmailTemplateDirectoryException {
        String packageWithSlashes = packageStr.replaceAll("\\.", "/"); // e.g. "com/myproject/mtpl/registrationemail"
        String result = packageWithSlashes + "/" + leafName;
        if (getClass().getClassLoader().getResource(result) == null)
            throw new FileNotFoundInEmailTemplateDirectoryException(
                "File '" + leafName +"' not found in email tpl package '" + packageStr + "'");
        return result;
    }
    
    /**
     * @param extension for example ".txt" 
     * @return full classpath name to the file
     */
    protected String findFile(String leafNameStem, Locale locale, String extension)
    throws FileNotFoundInEmailTemplateDirectoryException {
        try {
            return findFile(leafNameStem + "_" + locale.getLanguage() + extension);
        }
        catch (FileNotFoundInEmailTemplateDirectoryException e) {
            return findFile(leafNameStem +  extension);
        }
    }
    
    /**
     * Will find a file like "body.velocity.utf8.txt"
     * @param leafNameStem for example "body"
     * @param extension for example ".txt" 
     */
    protected String expandVelocityTemplate(String leafNameStem, Locale locale, String extension, Map<String, ? extends Object> parameters)
    throws FileNotFoundInEmailTemplateDirectoryException {
        VelocityEngine velocity = new VelocityEngine();
        velocity.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath");
        velocity.setProperty("classpath.resource.loader.class", ClasspathResourceLoader.class.getName());
        velocity.setProperty(RuntimeConstants.RUNTIME_LOG_LOGSYSTEM_CLASS, Log4JLogChute.class.getName());
        velocity.setProperty(Log4JLogChute.RUNTIME_LOG_LOG4J_LOGGER, getClass().getName());

        Template template = velocity.getTemplate(findFile(leafNameStem, locale, ".velocity.utf8" + extension), "UTF-8");
        VelocityContext ctx = new VelocityContext();
        for (Entry<String, ? extends Object> p : parameters.entrySet()) ctx.put(p.getKey(), p.getValue());
        ctx.put("esc", new EscapeTool());
        
        StringWriter result = new StringWriter();
        template.merge(ctx, result);
        return result.toString();
    }

    @SuppressFBWarnings("ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD")
    protected BodyPart parsePlainTextBodyPart(Locale locale, Map<String, ? extends Object> parameters)
    throws FileNotFoundInEmailTemplateDirectoryException, MessagingException {
        String textContents = expandVelocityTemplate("body", locale, ".txt", parameters);

        lastBodyForTesting = textContents;

        BodyPart result = new MimeBodyPart();
        result.setText(textContents);
        return result;
    }

    protected BodyPart parseHtmlBodyPart(Locale locale, Map<String, ? extends Object> parameters)
    throws FileNotFoundInEmailTemplateDirectoryException, MessagingException {
        String htmlContents = expandVelocityTemplate("body", locale, ".html", parameters);

        Map<String, BodyPart> referencedFiles = new TreeMap<>();
        StringBuffer htmlContentsWithCid = new StringBuffer();
        Matcher fileMatcher = Pattern.compile("(['\"])([\\w\\-]+)\\.(\\w{3,4})['\"]").matcher(htmlContents);
        while (fileMatcher.find()) {
            String quote = fileMatcher.group(1);
            String leafNameWithoutExtension = fileMatcher.group(2);
            String extension = fileMatcher.group(3);
            String leafNameWithExtension = leafNameWithoutExtension + "." + extension;

            BodyPart filePart = new MimeBodyPart();
            DataSource source = new FileAttachmentJavamailDataSource(leafNameWithoutExtension, extension);
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

        MimeBodyPart result = new MimeBodyPart();
        result.setContent(htmlMultiPart);
        return result;
    }

    public static BodyPart newAttachmentBodyPart(final Attachment attachment) throws MessagingException {
        DataSource dataSource = new DataSource() {
            @Override public String getContentType() { return attachment.getMimeType(); }
            @Override public InputStream getInputStream() { return attachment.newInputStream(); }
            @Override public String getName() { return attachment.getLeafNameInclExtension(); }
            @Override public OutputStream getOutputStream() { throw new RuntimeException("unreachable"); }
        };

        BodyPart filePart = new MimeBodyPart();
        filePart.setDataHandler(new DataHandler(dataSource));
        filePart.setFileName(attachment.getLeafNameInclExtension());
        filePart.setDisposition(Part.ATTACHMENT);
        
        return filePart;
    }

    // --------------------------------------------------------------------------------------------------------
    // Public methods / API
    // --------------------------------------------------------------------------------------------------------

    public static class FileNotFoundInEmailTemplateDirectoryException extends RuntimeException {
        public FileNotFoundInEmailTemplateDirectoryException(String msg) { super(msg); }
    }

    public interface Attachment {
        /** For example "invoice.pdf" */ public String getLeafNameInclExtension();
        /** For example "application/pdf" */ public String getMimeType();
        /** To provide the bytes of the file */ public InputStream newInputStream();
    }

    public static class ByteArrayAttachment implements Attachment {
        protected String leafNameInclExtension, mimeType;
        protected byte[] bytes;
        public ByteArrayAttachment(String leafNameInclExtension, String mimeType, byte[] bytes) {
            this.leafNameInclExtension = leafNameInclExtension; this.mimeType = mimeType; this.bytes = bytes; }
        @Override public String getLeafNameInclExtension() { return leafNameInclExtension; }
        @Override public String getMimeType() { return mimeType; }
        @Override public InputStream newInputStream() { return new ByteArrayInputStream(bytes); }
    }

    public EmailTemplate(Package pkg) { this.packageStr = pkg.getName(); }
    public EmailTemplate(String pkgStr) { this.packageStr = pkgStr; }

    /** Henceforth, no emails will be sent; instead the body will be recorded for inspection by {@link #getLastBodyForTesting()}. */
    static public void setLastBodyForTestingInsteadOfSendingEmails() { setLastBodyForTestingInsteadOfSendingEmails = true; }

    /** Return the plain text body of the last email which has been sent; or the empty string in case no emails have been sent. */
    static public String getLastBodyForTesting() { return lastBodyForTesting; }

    /** Send an email based on this email template. */
    public void send(
        EmailTransaction tx, Collection<InternetAddress> recipientEmailAddresses, Locale locale,
        Map<String, ? extends Object> parameters, Attachment... attachments
    ) {
        try {
            // Create the "message text" part which is the multipart/alternative of the text/plain and HTML versions
            Multipart messageText = new MimeMultipart("alternative");
            try { messageText.addBodyPart(parsePlainTextBodyPart(locale, parameters)); }
            catch (FileNotFoundInEmailTemplateDirectoryException e) { }
            try { messageText.addBodyPart(parseHtmlBodyPart(locale, parameters)); }
            catch (FileNotFoundInEmailTemplateDirectoryException e) { }
            if (messageText.getCount() < 1) throw new RuntimeException("No html nor text body found for email template: " + packageStr);
            if (setLastBodyForTestingInsteadOfSendingEmails) return;

            // Create the "main part" which is multipart/mixed of the message body and attachments
            MimeBodyPart messageTextPart = new MimeBodyPart();
            messageTextPart.setContent(messageText);
            Multipart mainPart = new MimeMultipart("mixed");
            mainPart.addBodyPart(messageTextPart);
            for (Attachment a : attachments) mainPart.addBodyPart(newAttachmentBodyPart(a));

            // Create the message from the subject and body
            Message msg = tx.newMimeMessage();
            msg.setHeader("X-SMTPAPI", getSendGridXSmtpApiHeader());
            msg.setFrom(new InternetAddress(expandVelocityTemplate("from", locale, ".txt", parameters)));
            msg.addRecipients(RecipientType.TO, recipientEmailAddresses.toArray(new InternetAddress[0]));
            msg.setSubject(expandVelocityTemplate("subject", locale, ".txt", parameters));
            msg.setContent(mainPart);
            msg.setSentDate(new Date());

            // Send the message
            tx.send(msg);
        }
        catch (MessagingException e) { throw new RuntimeException(e); }
    }

    public void send(
        EmailTransaction tx, InternetAddress recipientEmailAddress, Locale locale,
        Map<String, ? extends Object> parameters, Attachment... attachments
    ) {
        send(tx, Arrays.asList(recipientEmailAddress), locale, parameters, attachments);
    }
    
    protected String replaceImagesWithBaseURL(URL imageBaseUrl, String htmlContents) {
        StringBuffer result = new StringBuffer();
        Matcher fileMatcher = Pattern.compile("(['\"])([\\w\\-]+)\\.(\\w{3,4})['\"]").matcher(htmlContents);
        while (fileMatcher.find()) {
            String quote = fileMatcher.group(1);
            String leafNameWithoutExtension = fileMatcher.group(2);
            String extension = fileMatcher.group(3);
            String leafNameWithExtension = leafNameWithoutExtension + "." + extension;

            fileMatcher.appendReplacement(result, quote + imageBaseUrl + leafNameWithExtension + quote);
        }
        fileMatcher.appendTail(result);
        return result.toString();
    }
    
    /** 
     * @param imageBaseUrl images in the directory must be uploaded somewhere. They are then replaced
     *   in the HTML with a link to that place.
     * @param file where to write the HTML to
     */
    public void writeHtmlPartToFile(Locale locale, URL imageBaseUrl, Map<String, ? extends Object> parameters, File file) {
        try {
            String body = expandVelocityTemplate("body", locale, ".html", parameters);
            body = replaceImagesWithBaseURL(imageBaseUrl, body);
            FileUtils.writeStringToFile(file, body, "UTF-8");
            Logger.getLogger(getClass()).info("Successfully wrote email template to '"+file+"'");
        }
        catch (IOException e) { throw new RuntimeException(e); }
    }
    
    protected String getSendGridXSmtpApiHeader() {
        Map<String, Object> values = new HashMap<>();
        values.put("category", new String[] { getCampaignName() });
        return new Gson().toJson(values);
    }
    
    /**
     * Gets the "campaign name" (or "category" or "tag") which is sent along with this email when it's delivered.
     *    <p>
     * By default it is the package name of the email template.
     *    <p>
     * Sometimes it can be useful to have a tag which is sent along with an email,
     * for example SendGrid can then do reports based on that.
     * If the email is sent via default SMTP then this is ignored.
     * Currently only SendGridEmailTransaction makes use of this.
     */
    public String getCampaignName() {
        return packageStr;
    }
}
