package com.databasesandlife.util;

import junit.framework.TestCase;

public class WebEncodingUtilsTest extends TestCase {
    
    public void testEncodeHtml() {
        assertEquals("foo &amp; &lt; &gt; bar", WebEncodingUtils.encodeHtml("foo & < > bar"));
    }

}
