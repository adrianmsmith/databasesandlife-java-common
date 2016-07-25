package com.databasesandlife.util;

import junit.framework.TestCase;

/**
 * @author This source is copyright <a href="http://www.databasesandlife.com">Adrian Smith</a> and licensed under the LGPL 3.
 */
public class WebEncodingUtilsTest extends TestCase {
    
    public void testEncodeHtml() {
        assertEquals("foo &amp; &lt; &gt; bar", WebEncodingUtils.encodeHtml("foo & < > bar"));
    }

    public void testBeautifyUrl() {
        assertEquals("new-york", WebEncodingUtils.beautifyUrl("----New/& ?York--"));
    }

}
