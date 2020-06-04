package com.databasesandlife.util.spring;

import com.databasesandlife.util.gwtsafe.CleartextPassword;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.client.core.WebServiceMessageCallback;
import org.springframework.ws.soap.saaj.SaajSoapMessage;

import javax.annotation.Nonnull;
import javax.xml.namespace.QName;
import javax.xml.soap.*;

/**
 * This adds a security header to a SOAP message being sent to a SOAP server by Spring.
 * <p>Usage:
 * <pre>
 *     getWebServiceTemplate().marshalSendAndReceive(request, 
 *        new SoapClientSecurityHeaderWriter("user", "pw"));
 * </pre>
 * </p>
 * <p>
 * The header looks like:
 * <pre>
 *     &lt;wsse:Security xmlns:wsse="http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd"
 *         wsse:mustUnderstand="1"&gt;
 *       &lt;wsse:UsernameToken&gt;
 *         &lt;wsse:Username&gt;xx&lt;/wsse:Username&gt;
 *         &lt;wsse:Password&gt;yy&lt;/wsse:Password&gt;
 *       &lt;/wsse:UsernameToken&gt;
 *     &lt;/wsse:Security&gt;
 * </pre>
 *
 * @author This source is copyright <a href="http://www.databasesandlife.com">Adrian Smith</a> and licensed under the LGPL 3.
 * @see <a href="https://github.com/adrianmsmith/databasesandlife-java-common">Project on GitHub</a>
 */
public class SoapClientSecurityHeaderWriter implements WebServiceMessageCallback {
    
    protected String username;
    protected CleartextPassword password;

    public SoapClientSecurityHeaderWriter(@Nonnull String username, @Nonnull CleartextPassword password) {
        this.username = username;
        this.password = password;
    }
    
    public SoapClientSecurityHeaderWriter(@Nonnull String username, @Nonnull String password) {
        this(username, new CleartextPassword(password));
    }
    
    @Override
    public void doWithMessage(WebServiceMessage message) {
        try {
            SOAPMessage soapMessage = ((SaajSoapMessage) message).getSaajMessage();
            SOAPHeader header = soapMessage.getSOAPHeader();
            SOAPHeaderElement security = header.addHeaderElement(
                new QName("http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd", "Security", "wsse"));
            SOAPElement usernameToken = security.addChildElement("UsernameToken", "wsse");
            SOAPElement usernameElement = usernameToken.addChildElement("Username", "wsse");
            SOAPElement passwordElement = usernameToken.addChildElement("Password", "wsse");

            usernameElement.setTextContent(username);
            passwordElement.setTextContent(password.getCleartext());
        }
        catch (SOAPException e) { throw new RuntimeException(e); }
    }
}
