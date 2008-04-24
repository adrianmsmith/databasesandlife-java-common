package com.databasesandlife.mailtemplate;

/**
 * Represents a bundle of resources needed to send an email (for example registration or password reminder email).
 * <p>
 * The objective of this system is that all necessary resources to send such an email can be stored together in one directory.
 * This includes texts in different langauges; HTML and text versions; associated images and CSS files for the HTML version, etc.
 * In addition, the system should be testable, i.e. it should be possible to test parts of the system which send such an email,
 *      to see if they've done so: this can be done by setting the MailTemplate not to actually deliver mail, but just store the
 *      mail it would have sent in some internal variables.
 * 
 * @author Adrian Smith &lt;adrian.m.smith@gmail.com&gt;
 */
public class MailTemplate {

}
