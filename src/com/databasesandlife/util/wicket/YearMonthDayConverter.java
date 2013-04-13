package com.databasesandlife.util.wicket;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

import org.apache.wicket.util.convert.ConversionException;
import org.apache.wicket.util.convert.IConverter;

import com.databasesandlife.util.YearMonthDay;

/**
 * Allows Wicket fields to have models of type {@link YearMonthDay}.
 *    <p>
 * To use, for example add the following method to the Wicket application:
 * <pre>
 * protected IConverterLocator newConverterLocator() {
 *   ConverterLocator locator = 
 *     (ConverterLocator) super.newConverterLocator();
 *   locator.set(YearMonthDay.class, 
 *     new YearMonthDayConverter(new SimpleDateFormat("dd.MM.yyyy")));
 *   return locator;
 * }
 * </pre>
 */
@SuppressWarnings("serial")
public class YearMonthDayConverter implements IConverter<YearMonthDay>{

	private final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
	private SimpleDateFormat userFormat;
	
	public YearMonthDayConverter(SimpleDateFormat format){
		userFormat = format;
	}
	
	@Override
	public YearMonthDay convertToObject(String arg0, Locale arg1) {
		if(arg0 == null) return null;
		try {
			return YearMonthDay.newForYYYYMMDD(format.format(userFormat.parse(arg0)));
		} catch (ParseException e) {
			throw new ConversionException(e).setResourceKey("YearMonthDay not valid");
		}
	}

	@Override
	public String convertToString(YearMonthDay arg0, Locale arg1) {
		if(arg0 == null) return null;
 		try {
			return userFormat.format(format.parse(arg0.toYYYYMMDD()));
		} catch (ParseException e) {
			throw new ConversionException(e).setResourceKey("YearMonthDay not valid");
		}
	}


}
