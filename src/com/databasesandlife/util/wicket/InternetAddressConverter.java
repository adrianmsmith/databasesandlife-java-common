package com.databasesandlife.util.wicket;

import java.util.Locale;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.apache.wicket.util.convert.ConversionException;
import org.apache.wicket.util.convert.IConverter;

/**
 * Converts user input into {@link InternetAddress} email address.
 */
@SuppressWarnings("serial")
public class InternetAddressConverter implements IConverter<InternetAddress> {

    @Override
    public InternetAddress convertToObject(String str, Locale arg1) {
        if (str == null) 
            return null;
        else try {
            return new InternetAddress(str, true);
        } catch (AddressException e) {
            throw new ConversionException(e).setResourceKey("invalid");
        }
    }

    @Override
    public String convertToString(InternetAddress val, Locale arg1) {
        if(val == null) return null;
        else return val.toString();
    }
}
