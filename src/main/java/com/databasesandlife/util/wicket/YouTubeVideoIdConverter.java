package com.databasesandlife.util.wicket;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.wicket.util.convert.ConversionException;
import org.apache.wicket.util.convert.IConverter;

import com.databasesandlife.util.gwtsafe.YouTubeVideoId;

/**
 * Allows user to enter YouTube Video URLs.
 *
 * @author This source is copyright <a href="http://www.databasesandlife.com">Adrian Smith</a> and licensed under the LGPL 3.
 * @see <a href="https://github.com/adrianmsmith/databasesandlife-java-common">Project on GitHub</a>
 */
@SuppressWarnings("serial")
public class YouTubeVideoIdConverter implements IConverter<YouTubeVideoId> {

    @Override public YouTubeVideoId convertToObject(String value, Locale locale) {
        Matcher m;
        
        if (value == null || "".equals(value)) return null;
        
        m = Pattern.compile("youtube.*[\\?&]v=([\\w-]{11})([#&].*)?$", Pattern.CASE_INSENSITIVE).matcher(value);
        if (m.find()) return new YouTubeVideoId(m.group(1));
        
        m = Pattern.compile("youtu\\.be/(\\w{11})$", Pattern.CASE_INSENSITIVE).matcher(value);
        if (m.find()) return new YouTubeVideoId(m.group(1));
        
        throw new ConversionException("URL '" + value + "' doesn't look like a youtube URL");
    }

    @Override public String convertToString(YouTubeVideoId value, Locale locale) {
        YouTubeVideoId obj = (YouTubeVideoId) value;
        if (obj == null) return "";
        return "http://www.youtube.com/watch?v=" + obj.id;
    }
}
