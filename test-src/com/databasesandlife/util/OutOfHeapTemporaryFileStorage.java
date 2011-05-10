package com.databasesandlife.util;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import junit.framework.TestCase;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.databasesandlife.util.OutOfHeapTemporaryStorage.OutOfHeapString;
import com.databasesandlife.util.OutOfHeapTemporaryStorage.OutOfHeapXml;

/**
 * @author This source is copyright <a href="http://www.databasesandlife.com">Adrian Smith</a> and licensed under the LGPL 3.
 * @version $Revision$
 */
public class OutOfHeapTemporaryFileStorage extends TestCase {
    
    public OutOfHeapTemporaryFileStorage(String testName) {
        super(testName);
    }

    public void testNewStringContainer() {
        OutOfHeapTemporaryStorage str = new OutOfHeapTemporaryStorage();
        OutOfHeapString a = str.newStringContainer("abc");
        OutOfHeapString b = str.newStringContainer("foo\u20E0bar");
        assertEquals("abc",    a.toString());
        assertEquals("foo\u20E0bar", b.toString());
        try { a.append("x"); fail(); } catch (IllegalStateException e) { }
        b.append("joe");
        assertEquals("foo\u20E0barjoe", b.toString());
    }
    
    public void testNewXmlContainer() throws Exception {
        DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = dbfac.newDocumentBuilder();
        Document doc = docBuilder.newDocument();
        Element root = doc.createElementNS("my-namespace", "my-test-element");
        
        OutOfHeapTemporaryStorage str = new OutOfHeapTemporaryStorage();
        OutOfHeapXml x = str.newXmlContainer();
        x.setXml(root);
        
        Element fromFile = x.toXmlDomElement();
        assertEquals("my-test-element", fromFile.getNodeName());
        assertEquals("my-namespace", fromFile.getNamespaceURI());
    }
}
