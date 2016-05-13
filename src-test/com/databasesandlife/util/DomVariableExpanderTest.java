package com.databasesandlife.util;

import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import junit.framework.TestCase;

import org.hibernate.lob.ReaderInputStream;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.databasesandlife.util.DomVariableExpander.VariableNotFoundException;

@SuppressWarnings("serial")
public class DomVariableExpanderTest extends TestCase {
    
    public void testExpand() throws Exception {
        
        // Parse XML template
        String xmlTemplate = 
            "<foo attr='abc $var ${var2}'>" +
            " Some text $var ${var2}" +
            "</foo>";
        InputStream xmlTemplateInputStream = new ReaderInputStream(new StringReader(xmlTemplate));
        Element template = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(xmlTemplateInputStream).getDocumentElement();
        
        // Transform
        Map<String, String> variables = new HashMap<String, String>() {{
            put("var",  "VAR");
            put("var2", "VAR2");
        }};
        Document expanded = DomVariableExpander.expand(template, variables);

        // Check the result of the transformation
        StringWriter str = new StringWriter();
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty("omit-xml-declaration","yes");
        transformer.transform(new DOMSource(expanded), new StreamResult(str));
        assertEquals("<foo attr=\"abc VAR VAR2\"> Some text VAR VAR2</foo>", str.toString());
        
        // Test: Variable missing leads to exception
        try {
            variables.remove("var2");
            DomVariableExpander.expand(template, variables);
            fail();
        }
        catch (VariableNotFoundException e) {
            assertTrue(e.getMessage().contains("var2"));
        }
    }
}
