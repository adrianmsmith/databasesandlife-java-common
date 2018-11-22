package com.databasesandlife.util;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import com.databasesandlife.util.CsvParser.MalformedCsvException;

/**
 * @author This source is copyright <a href="http://www.databasesandlife.com">Adrian Smith</a> and licensed under the LGPL 3.
 * @see <a href="https://github.com/adrianmsmith/databasesandlife-java-common">Project on GitHub</a>
 */
public class CsvParserTest extends TestCase {

    public void testParse() throws MalformedCsvException {
        // Test normal case
        List<Map<String,String>> x = new CsvParser().parseToListOfMaps(new BufferedReader(new StringReader(
            "A,B,C\n" +
            "a,b,c\n" +
            "x,y,z\n")));
        assertEquals(2, x.size());
        assertEquals("a", x.get(0).get("A"));

        // Wrong number of fields
        try { new CsvParser().parseToListOfMaps(new BufferedReader(new StringReader("A,B,C\na,b"))); fail(); }
        catch (MalformedCsvException e) { assertTrue(e.getMessage().contains("Expected")); }
    }

    public void testSetMandatoryFields() {
        try {
            CsvParser parser = new CsvParser();
            parser.setMandatoryFields("C");
            parser.parseToListOfMaps(new BufferedReader(new StringReader("A,B\na,b")));
            fail();
        }
        catch (MalformedCsvException e) { assertTrue(e.getMessage().contains("C")); }
    }

    public void testSetAllowedFields() {
        try {
            CsvParser parser = new CsvParser();
            parser.setAllowedFields("A", "B", "C");
            parser.parseToListOfMaps(new BufferedReader(new StringReader("A,B,C,WRONG\na,b,c,d")));
            fail();
        }
        catch (MalformedCsvException e) { assertTrue(e.getMessage().contains("WRONG")); }
    }

    public void testSetNonEmptyFields() {
        try {
            CsvParser parser = new CsvParser();
            parser.setNonEmptyFields("A");
            parser.parseToListOfMaps(new BufferedReader(new StringReader("A,B,C\n,b,c"))); 
            fail(); 
        }
        catch (MalformedCsvException e) { assertTrue(e.getMessage().contains("A")); }
    }
}
