package com.databasesandlife.util;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents a date (year, month, day).
 *
 * @author The Java source is copyright <a href="http://www.databasesandlife.com">Adrian Smith</a> and licensed under the LGPL 3.
 * @version $Revision$
 */
public class YearMonthDay implements Serializable, Comparable<YearMonthDay> {
    
    public int year, month, day;
    
    /** @param date "YYYY-MM-DD", not null */
    public static YearMonthDay newForYYYYMMDD(String date) {
        Matcher m = Pattern.compile("(\\d{4})-(\\d{2})-(\\d{2})").matcher(date);
        if (!m.matches()) throw new IllegalArgumentException("Date '" + date + "' is not 'YYYY-MM-DD' format");
        YearMonthDay result = new YearMonthDay();
        result.year = Integer.parseInt(m.group(1));
        result.month = Integer.parseInt(m.group(2));
        result.day = Integer.parseInt(m.group(3));
        return result;
    }
    
    /** @return "YYYY-MM-DD" */
    public String toYYYYMMDD() {
        return String.format("%04d-%02d-%02d", year, month, day);
    }
    
    @Override public String toString() {
        return toYYYYMMDD();
    }
    
    @Override public boolean equals(Object other) {
        if (other == null) return false;
        if ( ! (other instanceof YearMonthDay)) return false;
        YearMonthDay o = (YearMonthDay) other;
        if (year != o.year) return false;
        if (month != o.month) return false;
        if (day != o.day) return false;
        return true;
    }
    
    @Override public int hashCode() {
        return 384745 + toYYYYMMDD().hashCode();
    }
    
    public static YearMonthDay newToday() {
        return newForYYYYMMDD(new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
    }
    
    public Date getMidnightAtStart() {
        try {
            SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd");
            return f.parse(toYYYYMMDD());
        }
        catch (ParseException e) { throw new RuntimeException(e); }
    }
    
    protected YearMonthDay getDelta(long millisDelta) {
        Date result = new Date(getMidnightAtStart().getTime() + millisDelta);
        return newForYYYYMMDD(new SimpleDateFormat("yyyy-MM-dd").format(result));
    }
    
    public YearMonthDay getPrevious() {
        return getDelta(-24*60*60*1000L);
    }
    
    public YearMonthDay getNext() {
        return getDelta(+24*60*60*1000L);
    }

    public Date getMidnightAtEnd() {
        return getNext().getMidnightAtStart();
    }

    @Override public int compareTo(YearMonthDay other) {
        return getMidnightAtStart().compareTo(other.getMidnightAtStart());
//        if (this.year < other.year) return -1;
//        if (this.year > other.year) return +1;
//        if (this.month < other.month) return -1;
//        if (this.month > other.month) return +1;
//        if (this.day < other.day) return -1;
//        if (this.day > other.day) return +1;
//        return 0;
    }

    public YearMonthDay subtractDays(int dayCount) {
        return getDelta(-dayCount*24*60*60*1000L);
    }

    public static YearMonthDay newMinusInfinity() {
        return newForYYYYMMDD("1970-01-01");
    }
}
