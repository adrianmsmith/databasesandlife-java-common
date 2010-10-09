package com.databasesandlife.util;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import com.databasesandlife.util.CsvParser.MalformedCsvException;

/**
 * @author This source is copyright <a href="http://www.databasesandlife.com">Adrian Smith</a> and licensed under the LGPL 3.
 * @version $Revision: 1960 $
 */
public class CsvParserTest extends TestCase {

    public void testParse() throws MalformedCsvException {
        CsvParser parser = new CsvParser();
        parser.setDesiredFields("A", "B", "C");
        parser.setNonEmptyFields("A");

        // Test normal case
        List<Map<String,String>> x = parser.parseToListOfMaps(new BufferedReader(new StringReader("A,B,C\na,b,c\nx,y,z\n")));
        assertEquals(2, x.size());
        assertEquals("a", x.get(0).get("A"));

        // Wrong number of fields
        try { parser.parseToListOfMaps(new BufferedReader(new StringReader("A,B,C\na,b"))); fail(); }
        catch (MalformedCsvException e) { assertTrue(e.getMessage().contains("Expected")); }

        // Too many columns
        try { parser.parseToListOfMaps(new BufferedReader(new StringReader("A,B,C,WRONG\na,b,c,d"))); fail(); }
        catch (MalformedCsvException e) { assertTrue(e.getMessage().contains("WRONG")); }

        // Too few columns
        try { parser.parseToListOfMaps(new BufferedReader(new StringReader("A,B\na,b"))); fail(); }
        catch (MalformedCsvException e) { assertTrue(e.getMessage().contains("C")); }

        // Empty field
        try { parser.parseToListOfMaps(new BufferedReader(new StringReader("A,B,C\n,b,c"))); fail(); }
        catch (MalformedCsvException e) { assertTrue(e.getMessage().contains("A")); }
    }
}
