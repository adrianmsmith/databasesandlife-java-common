package com.databasesandlife.util;

/**
 * @author Adrian Smith
 */
public class WebEncodingUtils {

    public static String encodeHtml(String x) {
        x = x.replaceAll("&", "&amp;");
        x = x.replaceAll(">", "&gt;");
        x = x.replaceAll("<", "&lt;");
        return x;
    }

}
