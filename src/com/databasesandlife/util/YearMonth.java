package com.databasesandlife.util;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents a year and a month. 
 *
 * @author The Java source is copyright <a href="http://www.databasesandlife.com">Adrian Smith</a> and licensed under the LGPL 3.
 * @version $Revision$
 */
public class YearMonth implements Serializable {

    public int year, month;

    public static class YearMonthParseException extends Exception {
        YearMonthParseException(String msg) { super(msg); }
    }

    YearMonth() { }
    public YearMonth(int y, int m) { year=y; month=m; }

    /** @return "YYYY-MM" form */
    public String toYYYYMM() {
        return String.format("%04d-%02d", year, month);
    }

    /** @param str "YYYY-MMxx" form where xx can be anything */
    public static YearMonth newForYYYYMM(String str) throws YearMonthParseException  {
        if (str == null) str = ""; // so that exception message is reasonable, and not NullPointerException
        Matcher m = Pattern.compile("^(\\d{4})-(\\d{2}).*$").matcher(str);
        if ( ! m.matches()) throw new YearMonthParseException("String '"+str+"' isn't 'YYYY-MMxx' date");
        YearMonth result = new YearMonth();
        result.year = Integer.parseInt(m.group(1));
        result.month = Integer.parseInt(m.group(2));
        return result;
    }

    public static YearMonth min(YearMonth a, YearMonth b) {
        if (a.lt(b)) return a;
        else return b;
    }

    public static YearMonth max(YearMonth a, YearMonth b) {
        if (a.gt(b)) return a;
        else return b;
    }

    public boolean lt(YearMonth other) {
        if (year < other.year) return true;
        if (other.year < year) return false;
        if (month < other.month) return true;
        return false;
    }

    public boolean le(YearMonth other) {
        return ! other.lt(this);
    }

    public boolean gt(YearMonth other) {
        return ! le(other);
    }

    public static YearMonth now() {
        Date now = new Date();
        YearMonth result = new YearMonth();
        result.year  = Integer.parseInt(new SimpleDateFormat("yyyy").format(now));
        result.month = Integer.parseInt(new SimpleDateFormat("MM").format(now));
        return result;
    }

    public YearMonth getPreviousMonth() {
        YearMonth result = new YearMonth();
        result.year = year;
        result.month = month - 1;
        if (result.month < 1) { result.year--; result.month=12; }
        return result;
    }

    public YearMonth getNextMonth() {
        YearMonth result = new YearMonth();
        result.year = year;
        result.month = month + 1;
        if (result.month > 12) { result.year++; result.month=1; }
        return result;
    }

    public static YearMonth[] rangeDownwardsIncl(YearMonth maxIncl, YearMonth minIncl) {
        List<YearMonth> result = new ArrayList<YearMonth>();
        YearMonth i = minIncl;
        while (i.le(maxIncl)) {
            result.add(0, i);
            i = i.getNextMonth();
        }
        return result.toArray(new YearMonth[0]);
    }

    public Date toDate() {
        try { return new SimpleDateFormat("yyyy-MM").parse(toYYYYMM()); }
        catch (ParseException e) { throw new RuntimeException("unreachable", e); }
    }
}
