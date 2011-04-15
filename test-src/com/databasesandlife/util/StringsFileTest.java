package com.databasesandlife.util;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import junit.framework.TestCase;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.databasesandlife.util.StringsFile.StringInFile;
import com.databasesandlife.util.StringsFile.XmlInStringsFile;

/**
 * @author This source is copyright <a href="http://www.databasesandlife.com">Adrian Smith</a> and licensed under the LGPL 3.
 * @version $Revision$
 */
public class StringsFileTest extends TestCase {
    
    public StringsFileTest(String testName) {
        super(testName);
    }

    public void testString() {
        StringsFile str = new StringsFile();
        StringInFile a = str.newString("abc");
        StringInFile b = str.newString("foo\u20E0bar");
        assertEquals("abc",    a.toString());
        assertEquals("foo\u20E0bar", b.toString());
        try { a.append("x"); fail(); } catch (StringsFile.StringCannotBeAppendedException e) { }
        b.append("joe");
        assertEquals("foo\u20E0barjoe", b.toString());
    }
    
    public void testXml() throws Exception {
        DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = dbfac.newDocumentBuilder();
        Document doc = docBuilder.newDocument();
        Element root = doc.createElementNS("my-namespace", "my-test-element");
        
        StringsFile str = new StringsFile();
        XmlInStringsFile x = str.newXml(root);
        
        Element fromFile = x.toXmlDomElement();
        assertEquals("my-test-element", fromFile.getNodeName());
        assertEquals("my-namespace", fromFile.getNamespaceURI());
    }
}
