package com.databasesandlife.util;

import junit.framework.TestCase;

public class MD5HexTest extends TestCase {

    public void testMd5String() {
        String in = "\u00e4"; // a umlaut
        String out = MD5Hex.md5(in);
        assertEquals("8419b71c87a225a2c70b50486fbee545", out);
    }
}
