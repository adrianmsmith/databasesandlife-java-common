package com.databasesandlife.util.wicket;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;

import org.apache.wicket.util.convert.ConversionException;
import org.apache.wicket.util.convert.IConverter;

/**
 * Wicket converter to allow user to enter URLs and display errors if they are not valid.
 *
 * @author This source is copyright <a href="http://www.databasesandlife.com">Adrian Smith</a> and licensed under the LGPL 3.
 */
@SuppressWarnings("serial")
public class UrlConverter implements IConverter<URL> {

    @Override
    public URL convertToObject(String str, Locale arg1) {
        if (str == null) 
            return null;
        else try {
            String val = (str.toLowerCase().startsWith("https://") || str.toLowerCase().startsWith("http://")) ? str : "http://"+str;
            return new URL(val);
        } catch (MalformedURLException e) {
            throw new ConversionException(e).setResourceKey("invalidUrl").setVariable("url", str);
        }
    }

    @Override
    public String convertToString(URL val, Locale arg1) {
        if(val == null) return null;
        else return val.toString();
    }
}
