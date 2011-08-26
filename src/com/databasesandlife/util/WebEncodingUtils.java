package com.databasesandlife.util;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;

/**
 * @author This source is copyright <a href="http://www.databasesandlife.com">Adrian Smith</a> and licensed under the LGPL 3.
 * @version $Revision$
 */
public class WebEncodingUtils {

    public static String encodeHtml(String x) {
        x = x.replaceAll("&", "&amp;");
        x = x.replaceAll(">", "&gt;");
        x = x.replaceAll("<", "&lt;");
        return x;
    }

    @SuppressWarnings("unchecked")
    public static Map<String, String> getHttpRequestParameterMap(HttpServletRequest request) {
        Map<String, String> result = new HashMap<String, String>();
        for (Entry<String,String[]> paramEntry : ((Map<String,String[]>) request.getParameterMap()).entrySet())
            result.put(paramEntry.getKey(), paramEntry.getValue()[0]);
        return result;
    }

    /** @return "a=b&c=d" */
    public static CharSequence encodeGetParameters(Map<String, String> params) {
        try {
            StringBuilder result = new StringBuilder();
            for (Entry<String, String> entry : params.entrySet()) {
                if (result.length() > 0) result.append("&");
                result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
                result.append("=");
                result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
            }
            return result;
        }
        catch (UnsupportedEncodingException e) { throw new RuntimeException(e); }
    }
    
    // For GET parameters:
    // See http://java.sun.com/j2se/1.4.2/docs/api/java/net/URLEncoder.html
}