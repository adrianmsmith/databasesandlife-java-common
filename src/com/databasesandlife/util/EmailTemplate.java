package com.databasesandlife.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
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

import org.apache.commons.io.IOUtils;

/**

Represents a directory in the classpath, which contains text and potentially graphics, in multiple languages and multiple formats, for outgoing notification emails.

<p>A directory within the classpath should be created, and filled with the following files:</p>

<p><ul>
        <li><b>body.utf8.txt</b> - UTF-8 formatted body of the text/plain part of the email to be sent.</li>
        <li><b>body<b>.utf8</b>.html</b> - UTF-8 formatted HTML version of the email to be sent.</li>
        <li><b>subject<b>.utf8</b>.txt</b> - UTF-8 formatted subject of the email to be sent.</li>
        <li><b>from<b>.utf8</b>.txt </b>- An file containing an email address such as "John Smith &lt;bar@example.com&gt;"</li>
        <li><b>xyz.jpg</b> - Any resources required from the HTML version of the emails. They are referenced simply as &lt;img src="xyz.jpg"&gt; from the HTML versions, so the HTML version can be easily tested locally in a browser. This is replaced by &lt;img src="cid:xyz.jpg"&gt; by the software, as this is what is required in the email.</li>
        <li>Optionally <b>MyEmailTemplate.java</b> - Subclass of EmailTemplate, means that the whole template directory can be referenced via static typing, can be renamed with refactoring tools, and so on.</li>
</ul></p>

<p>Concerning <b>languages</b>, although e.g. "subject.utf8.txt" must be present, there may also be files with names such as "subject_de.utf8.txt" files for other Locales.</p>

<p>One or both of the <b>plain-text</b> and <b>HTML</b> versions of the email must be present. If they are both present then a "multipart/alternative" email is sent.</p>

<p>Bodies and subjects may have <b>variables </b>such <code>${XYZ}</code>. All variables are HTML-escaped in the HTML version of the email apart from variables with names such as <code>${XYZ_HTML}</code>.</p>

<p>For <b>unit testing</b>, use the static method {@link #setLastBodyForTestingInsteadOfSendingEmails()}.
After that method has been called, no emails will be sent,
instead the method {@link #getLastBodyForTesting} may be used to retrieve the last sent plain/text email body.
This allows one to assert that particular emails would be sent, and that they contain particular text.</p>

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
</ul></p>

<h3>Usage</h3>

<p>In the directory containing the template files:
<pre>
class RegistrationEmailTemplate extends EmailTemplate {
   public RegistrationEmailTemplate() {
     super(RegistrationEmailTemplate.class.getPackage());
   }
   // other methods can be added, specific to this email template
}
</pre></p>
<p>In client code:
<pre>
class RegistrationProcess {
  public registerNewUser(InternetAddress emailAddress, Locale language, String name, ... ) {
    String smtpServer = "localhost";

    Map&lt;String,String&gt; params = new HashMap&lt;String,String&gt;();
    params.put("USERNAME", name);

    RegistrationEmailTemplate tpl = new RegistrationEmailTemplate();
    tpl.send(smtpServer, recipientEmailAddress, language, params);
  }
}
</pre></p>
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
</pre></p>



 * @author The Java source is copyright <a href="http://www.databasesandlife.com">Adrian Smith</a> and licensed under the LGPL 3.
 * @version $Revision$
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
        public InputStream getInputStream() { return newInputStreamForBinaryFile(getName()); }
        public OutputStream getOutputStream() { throw new RuntimeException(); }
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
            return IOUtils.toString(i, "UTF-8");
        }
        catch (IOException e) { throw new RuntimeException(e); }
    }

    protected String readLocaleTextFile(String leafNameStem, Locale locale, String extension)
    throws FileNotFoundInEmailTemplateDirectoryException {
        try {
            return readTextFile(leafNameStem + "_" + locale.getLanguage() + ".utf8." + extension);
        }
        catch (FileNotFoundInEmailTemplateDirectoryException e) {
            return readTextFile(leafNameStem + ".utf8." + extension);
        }
    }

    protected BodyPart parseOptionalPlainTextBodyPart(Locale locale, Map<String,String> parameters) throws MessagingException {
        String textContents;
        try { textContents = readLocaleTextFile("body", locale, "txt"); }
        catch (FileNotFoundInEmailTemplateDirectoryException e) { return null; }

        textContents = replacePlainTextParameters(textContents, parameters);

        lastBodyForTesting = textContents;

        BodyPart result = new MimeBodyPart();
        result.setText(textContents);
        return result;
    }

    protected BodyPart parseOptionalHtmlBodyPart(Locale locale, Map<String,String> parameters) throws MessagingException {
        String htmlContents;
        try { htmlContents = readLocaleTextFile("body", locale, "html"); }
        catch (FileNotFoundInEmailTemplateDirectoryException e) { return null; }

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

    protected void addAttachment(Multipart container, final Attachment attachment) throws MessagingException {
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

        container.addBodyPart(filePart);
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

    /** Replaces variables such as ${XYZ} in the template */
    public static String replacePlainTextParameters(String template, Map<String,String> parameters) {
        for (Entry<String,String> paramEntry : parameters.entrySet())
            template = template.replace("${" + paramEntry.getKey() + "}", paramEntry.getValue());
        return template;
    }

    /** Send an email based on this email template. */
    public void send(
        String smtpServer, InternetAddress recipientEmailAddress, Locale locale,
        Map<String,String> parameters, Attachment... attachments
    ) {
        try {
            // Read the subject
            String subject = readLocaleTextFile("subject", locale, "txt");
            subject = replacePlainTextParameters(subject, parameters);

            // Read the bodies
            BodyPart plainTextBodyPart = parseOptionalPlainTextBodyPart(locale, parameters);
            BodyPart htmlBodyPart = parseOptionalHtmlBodyPart(locale, parameters);
            if (setLastBodyForTestingInsteadOfSendingEmails) return;

            // Create the "message text" part which is the multipart/alternative of the text/plain and HTML versions
            Multipart messageText = new MimeMultipart("alternative");
            if (plainTextBodyPart == null && htmlBodyPart == null)
                throw new RuntimeException("No bodies for email template: " + packageStr);
            if (plainTextBodyPart != null) messageText.addBodyPart(plainTextBodyPart);
            if (htmlBodyPart != null) messageText.addBodyPart(htmlBodyPart);

            // Create the "main part" which is multipart/mixed of the message body and attachments
            MimeBodyPart messageTextPart = new MimeBodyPart();
            messageTextPart.setContent(messageText);
            Multipart mainPart = new MimeMultipart("mixed");
            mainPart.addBodyPart(messageTextPart);
            for (Attachment a : attachments) addAttachment(mainPart, a);

            // Create the message from the subject and body
            Properties props = new Properties();
            props.put("mail.smtp.host", smtpServer);
            Session mailSession = Session.getDefaultInstance(props);
            Message msg = new MimeMessage(mailSession);
            msg.setFrom(new InternetAddress(readLocaleTextFile("from", locale, "txt")));
            msg.addRecipient(RecipientType.TO, recipientEmailAddress);
            msg.setSubject(subject);
            msg.setContent(mainPart);

            // Send the message
            Transport.send(msg);
        }
        catch (MessagingException e) { throw new RuntimeException(e); }
    }
}
