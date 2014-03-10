package com.databasesandlife.util.wicket;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.apache.wicket.util.convert.ConversionException;
import org.apache.wicket.util.convert.IConverter;

/**
 * Converts user input to/from a {@link List} of {@link InternetAddress} email address.
 *     <p>
 * Because of the <b>fail</b> that is <b>Java Erasure</b>, this cannot be added to a converter locator (at run-time, all the
 * converter locator sees is <code>List</code> not <code>List&lt;InternetAddress&gt;</code>, therefore cannot match
 * the converter.) You need to subclass your TextFields like this:
 * <pre>
 * add(new TextField&lt;List&lt;InternetAddress&gt;&gt;("emailAddressList") {
 *   public IConverter getConverter() {
 *     return new InternetAddressListConverter();
 *   }
 * }.setConvertEmptyInputStringToNull(false));
 * </pre>
 * 
 * @deprecated instead of using this, create a InteretAddressArrayConverter which wicket could use based on the run-time type;
 *  (X[] is distinct from Y[] at runtime; but List&lt;X&gt; and List&lt;Y&gt; are not.
 *
 * @author This source is copyright <a href="http://www.databasesandlife.com">Adrian Smith</a> and licensed under the LGPL 3.
 * @version $Revision$
 */
@SuppressWarnings("serial")
public class InternetAddressListConverter implements IConverter<List<InternetAddress>> {

    @Override
    public List<InternetAddress> convertToObject(String str, Locale arg1) {
        ArrayList<InternetAddress> result = new ArrayList<InternetAddress>();
        if (str != null)
            for (String x : str.split("[ ,;]+"))
                if ( ! x.isEmpty())
                    try { result.add(new InternetAddress(x, true)); }
                    catch (AddressException e) { 
                        throw new ConversionException(e).setResourceKey("invalidEmailAddress").setVariable("email-address", x); 
                    }
        return result;
    }

    @Override
    public String convertToString(List<InternetAddress> val, Locale arg1) {
        StringBuilder result = new StringBuilder();
        if (val != null)
            for (InternetAddress a : val) {
                if (result.length() > 0) result.append(", ");
                result.append(a.toString());
            }
        return result.toString();
    }
}
