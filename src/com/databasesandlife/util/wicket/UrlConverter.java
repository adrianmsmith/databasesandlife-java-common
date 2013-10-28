package com.databasesandlife.util.wicket;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;

import org.apache.wicket.util.convert.ConversionException;
import org.apache.wicket.util.convert.IConverter;

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
