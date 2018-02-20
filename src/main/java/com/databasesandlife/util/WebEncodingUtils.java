package com.databasesandlife.util;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

/**
 * @author This source is copyright <a href="http://www.databasesandlife.com">Adrian Smith</a> and licensed under the LGPL 3.
 * @see <a href="https://github.com/adrianmsmith/databasesandlife-java-common">Project on GitHub</a>
 */
public class WebEncodingUtils {

    public static String encodeHtml(String x) {
        x = x.replaceAll("&", "&amp;");
        x = x.replaceAll(">", "&gt;");
        x = x.replaceAll("<", "&lt;");
        x = x.replaceAll("\n", "<br/>");
        return x;
    }

    public static Map<String, String> getHttpRequestParameterMap(HttpServletRequest request) {
        Map<String, String> result = new HashMap<>();
        for (Entry<String,String[]> paramEntry : ((Map<String,String[]>) request.getParameterMap()).entrySet())
            result.put(paramEntry.getKey(), paramEntry.getValue()[0]);
        return result;
    }

    /** 
     * @param params values are either String or String[]
     * @return "a=b&amp;c=d" 
     */
    public static CharSequence encodeGetParameters(Map<String, ?> params) {
        try {
            StringBuilder result = new StringBuilder();
            for (Entry<String, ?> entry : params.entrySet()) {
                String[] values;
                if (entry.getValue() instanceof String) values = new String[] { (String) entry.getValue() };
                else if (entry.getValue() instanceof String[]) values = (String[]) entry.getValue();
                else throw new RuntimeException("Unexpected type of key '" + entry.getKey() + "': " + entry.getValue().getClass());
                
                for (String value : values) {
                    if (result.length() > 0) result.append("&");
                    result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
                    result.append("=");
                    result.append(URLEncoder.encode(value, "UTF-8"));
                }
            }
            return result;
        }
        catch (UnsupportedEncodingException e) { throw new RuntimeException(e); }
    }
    
    public static boolean isResponseSuccess(int responseCode) {
        return responseCode >= 200 && responseCode < 300; 
    }
    
    public static boolean isResponseSuccess(HttpURLConnection c) { 
        try { return isResponseSuccess(c.getResponseCode()); }
        catch (IOException e) { 
            Logger.getLogger(WebEncodingUtils.class).warn("HTTP request to '" + c.getURL() + "' unsuccessful", e);
            return false;
        }
    }
    
    // For GET parameters:
    // See http://java.sun.com/j2se/1.4.2/docs/api/java/net/URLEncoder.html
    
    /** Takes "New York" and returns "new-york" */
    public static String beautifyUrl(String x) {
        return x.replaceAll("[^\\p{L}\\p{N}]","-").replaceAll("-+", "-") .replaceAll("^-","").replaceAll("-$","").toLowerCase();
    }
    
    public static String dotdotdot(String x, int max) {
        if (x.length() <= max) return x;
        else return x.substring(0, max)+"...";
    }
}
