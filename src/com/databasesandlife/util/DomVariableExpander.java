package com.databasesandlife.util;

import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 * Takes a DOM XML tree which can have variables such as $xyz, and a Map of variable values, and creates a new DOM 
 * with those variables expanded.
 *    <p>
 * Usage:
 * <pre>
 *    Map&lt;String, String&gt; variables = ...
 *    Element elementWithVariables = ...
 *    Element elementExpanded = DomVariableExpander.expand(
 *       elementWithVariables, variables);          </pre>
 *   <p>
 * Variables may be written in attribute values and in text contents, and may be written in the XML as $xyz or ${xyz}.
 * Variables in the Map passed to the expand method should not have the dollar prefix.
 * Variable names may contain a-z, A-Z, 0-9, hypen and underscore and are case sensitive.
 *   <p>
 * This class is namespace aware.
 *
 * @author This source is copyright <a href="http://www.databasesandlife.com">Adrian Smith</a> and licensed under the LGPL 3.
 * @version $Revision$
 */

public class DomVariableExpander extends IdentityForwardingSaxHandler {
    
    public static class VariableNotFoundException extends RuntimeException {
        public VariableNotFoundException(String var) { super(var); }
    }
    
    protected Pattern variablePattern = Pattern.compile("\\$(([\\w-]+)|\\{([\\w-]+)\\})");
    protected Map<String, String> variables;
    
    public DomVariableExpander(TransformerHandler outputHandler, Map<String, String> variables) {
        super(outputHandler);
        this.variables = variables;
    }
    
    protected CharSequence expand(CharSequence template) throws VariableNotFoundException {
        Matcher matcher = variablePattern.matcher(template);
        StringBuffer result = new StringBuffer();
        while (matcher.find()) {
            String variable = matcher.group(2);                   // $xyz 
            if (variable == null) variable = matcher.group(3);    // ${xyz}
            String expansion = variables.get(variable);
            if (expansion == null) throw new VariableNotFoundException(
                "Variable '$" + variable + "' is used in XML template, but is missing from map of variables");
            matcher.appendReplacement(result, Matcher.quoteReplacement(expansion));
        }
        matcher.appendTail(result);
        return result;
    }
    
    @Override public void startElement(String uri, String localName, String el, Attributes templateAttributes) throws SAXException {
        AttributesImpl expandedAttributes = new AttributesImpl(templateAttributes);
        for (int a = 0; a < templateAttributes.getLength(); a++) {
            CharSequence valueTemplate = templateAttributes.getValue(a);
            CharSequence replacement = expand(valueTemplate);
            expandedAttributes.setValue(a, replacement.toString());
        }
        
        super.startElement(uri, localName, el, expandedAttributes);
    }
    
    @Override public void characters(char[] ch, int start, int length) throws SAXException {
        CharSequence templateCharacters = new String(ch, start, length);
        CharSequence expandedCharacters = expand(templateCharacters);
        super.characters(expandedCharacters.toString().toCharArray(), 0, expandedCharacters.length());
    }
    
    public static Element expand(Element prototypeElement, Map<String, String> variables) throws VariableNotFoundException {
        try {
            Properties systemProperties = System.getProperties();
            systemProperties.remove("javax.xml.transform.TransformerFactory");

            // The resulting DOM
            DOMResult result = new DOMResult();
            
            // SAX identity transformer to populate the resulting DOM
            SAXTransformerFactory writerFactory = (SAXTransformerFactory) SAXTransformerFactory.newInstance();
            TransformerHandler toResult = writerFactory.newTransformerHandler(); // Identity transform
            toResult.setResult(result);
            
            // Our transformer which expands variables into the above identity transformer
            DomVariableExpander expander = new DomVariableExpander(toResult, variables);
            SAXResult intoExpander = new SAXResult(expander);
            
            // Perform the chain of transformations, and populate "result"
            DOMSource source = new DOMSource(prototypeElement);
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.transform(source, intoExpander);
            
            return ((Document) result.getNode()).getDocumentElement();
        }
        catch (TransformerConfigurationException e) { throw new RuntimeException(e); }
        catch (TransformerException e) {
            if (e.getCause() instanceof VariableNotFoundException) throw (VariableNotFoundException) e.getCause();
            throw new RuntimeException(e); 
        }
    }
}
