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
import org.w3c.dom.Node;

import com.databasesandlife.util.DomVariableExpander.VariableNotFoundException;
import com.databasesandlife.util.DomVariableExpander.VariableSyntax;

/**
 * @author This source is copyright <a href="http://www.databasesandlife.com">Adrian Smith</a> and licensed under the LGPL 3.
 * @see <a href="https://github.com/adrianmsmith/databasesandlife-java-common">Project on GitHub</a>
 */
@SuppressWarnings("serial")
public class DomVariableExpanderTest extends TestCase {
    
    Map<String, String> variables = new HashMap<String, String>() {{
        put("var",  "VAR");
        put("var2", "VAR2");
    }};
    
    protected Element getXml(String xmlTemplate) throws Exception {
        InputStream xmlTemplateInputStream = new ReaderInputStream(new StringReader(xmlTemplate));
        return DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(xmlTemplateInputStream).getDocumentElement();
    }
    
    protected String printXml(Node expanded) throws Exception {
        StringWriter str = new StringWriter();
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty("omit-xml-declaration","yes");
        transformer.transform(new DOMSource(expanded), new StreamResult(str));
        return str.toString();
    }
    
    public void testExpand_dollarOrDollarThenBraces() throws Exception {
        Element template = getXml(
            "<foo attr='abc $var ${var2}'>" +
            " Some text $var ${var2}" +
            "</foo>");
        Document expanded = DomVariableExpander.expand(VariableSyntax.dollarOrDollarThenBraces, variables, template);
        assertEquals("<foo attr=\"abc VAR VAR2\"> Some text VAR VAR2</foo>", printXml(expanded));
    }
    
    public void testExpand_dollarThenBraces() throws Exception {
        Element template = getXml(
            "<foo attr='abc $var3 ${var2}'>" +
            " Some text $var3 ${var2}" +
            "</foo>");
        Document expanded = DomVariableExpander.expand(VariableSyntax.dollarThenBraces, variables, template);
        assertEquals("<foo attr=\"abc $var3 VAR2\"> Some text $var3 VAR2</foo>", printXml(expanded));
    }
    
    public void testExpand_variableMissing() throws Exception {
        for (VariableSyntax s : VariableSyntax.values()) {
            try {
                DomVariableExpander.expand(VariableSyntax.dollarOrDollarThenBraces, variables, getXml("<foo attr='abc ${var3}'/>"));
                fail(""+s);
            }
            catch (VariableNotFoundException e) { assertTrue(e.getMessage().contains("var3")); }
    
            try {
                DomVariableExpander.expand(VariableSyntax.dollarOrDollarThenBraces, variables, getXml("<foo>${var3}</foo>"));
                fail(""+s);
            }
            catch (VariableNotFoundException e) { assertTrue(e.getMessage().contains("var3")); }
        }
    }
}
