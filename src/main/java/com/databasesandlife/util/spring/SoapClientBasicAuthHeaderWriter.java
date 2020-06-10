package com.databasesandlife.util.spring;

import com.databasesandlife.util.gwtsafe.CleartextPassword;
import org.springframework.ws.transport.http.HttpUrlConnectionMessageSender;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.Base64;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Allows communication with a SOAP server which demands basic authentication.
 * <p>
 * Usage:
 * <pre>
 * var client = getWebServiceTemplate();
 * client.setMessageSender(new SoapClientBasicAuthHeaderWriter(username, password));
 * </pre>
 * </p>
 */
public class SoapClientBasicAuthHeaderWriter extends HttpUrlConnectionMessageSender {
    
    protected @Nonnull String username;
    protected @Nonnull CleartextPassword password;
    
    public SoapClientBasicAuthHeaderWriter(@Nonnull String username, @Nonnull CleartextPassword password) {
        this.username = username;
        this.password = password;
    }

    @Override
    protected void prepareConnection(@Nonnull HttpURLConnection connection) throws IOException {
        String userpassword = username + ":" + password.getCleartext();
        String encodedAuthorization = new String(Base64.getEncoder().encode(userpassword.getBytes(UTF_8)), UTF_8);
        connection.setRequestProperty("Authorization", "Basic " + encodedAuthorization);

        super.prepareConnection(connection);
    }
}

