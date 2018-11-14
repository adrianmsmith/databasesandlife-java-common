package com.databasesandlife.util;

import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;
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
import org.w3c.dom.Node;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 * Takes a XML element (containing text and sub-nodes), and expands variables like ${xyz}. 
 *    <p>
 * Usage:
 * <pre>
 * try {
 *     Map&lt;String, String&gt; variables = new Map&lt;&gt;() {{ put("foo", "value"); }};
 *
 *     // for example &lt;element attr="${foo}"&gt;${foo}&lt;/element&gt;
 *     Element elementWithVariables = ...
 *
 *     Document elementExpanded = DomVariableExpander.expand(
 *       variableStyle, variables, elementWithVariables);
 * }
 * catch (VariableNotFoundException e) { .. }
 * </pre>
 *   <p>
 * For the syntax of variables, see the {@link VariableSyntax} enum.
 *   <p>
 * Variables in the Map passed to the expand method should not have the dollar prefix.
 *   <p>
 * Variable names may contain a-z, A-Z, 0-9, hypen and underscore and are case sensitive.
 *   <p>
 * This class is XML namespace aware.
 *
 * @author This source is copyright <a href="http://www.databasesandlife.com">Adrian Smith</a> and licensed under the LGPL 3.
 * @see <a href="https://github.com/adrianmsmith/databasesandlife-java-common">Project on GitHub</a>
 */

@SuppressWarnings("serial")
public class DomVariableExpander extends IdentityForwardingSaxHandler {
    
    public static class VariableNotFoundException extends RuntimeException {
        public VariableNotFoundException(String var) { super(var); }
    }
    
    public enum VariableSyntax {
        /** Accepts variables like ${foo} but NOT $foo. */
        dollarThenBraces {
            protected Pattern variablePattern = Pattern.compile("\\$\\{([\\w-]+)\\}");
            @Override public CharSequence expand(Map<String, String> variables, CharSequence template) throws VariableNotFoundException {
                Matcher matcher = variablePattern.matcher(template);
                StringBuffer result = new StringBuffer();
                while (matcher.find()) {
                    String variable = matcher.group(1);                   // ${xyz}
                    String expansion = variables.get(variable);
                    if (expansion == null) throw new VariableNotFoundException(
                        "Variable '${" + variable + "}' is used in XML template, but is missing from map of variables");
                    matcher.appendReplacement(result, Matcher.quoteReplacement(expansion));
                }
                matcher.appendTail(result);
                return result;
            }
        },
        
        /** Accepts variables like ${foo} or $foo */ 
        dollarOrDollarThenBraces {
            protected Pattern variablePattern = Pattern.compile("\\$(([\\w-]+)|\\{([\\w-]+)\\})");
            @Override public CharSequence expand(Map<String, String> variables, CharSequence template) throws VariableNotFoundException {
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
        };
        
        public abstract CharSequence expand(Map<String, String> variables, CharSequence template) throws VariableNotFoundException;
    }
    
    protected final VariableSyntax syntax;
    protected final Map<String, String> variables;
    
    public DomVariableExpander(VariableSyntax syntax, Map<String, String> variables, TransformerHandler outputHandler) {
        super(outputHandler);
        this.syntax = syntax;
        this.variables = variables;
    }
    
    @Override public void startElement(String uri, String localName, String el, Attributes templateAttributes) throws SAXException {
        AttributesImpl expandedAttributes = new AttributesImpl(templateAttributes);
        for (int a = 0; a < templateAttributes.getLength(); a++) {
            CharSequence valueTemplate = templateAttributes.getValue(a);
            CharSequence replacement = syntax.expand(variables, valueTemplate);
            expandedAttributes.setValue(a, replacement.toString());
        }
        
        super.startElement(uri, localName, el, expandedAttributes);
    }
    
    @Override public void characters(char[] ch, int start, int length) throws SAXException {
        CharSequence templateCharacters = new String(ch, start, length);
        CharSequence expandedCharacters = syntax.expand(variables, templateCharacters);
        super.characters(expandedCharacters.toString().toCharArray(), 0, expandedCharacters.length());
    }
    
    public static Document expand(VariableSyntax syntax, Map<String, String> variables, Node prototypeElement) throws VariableNotFoundException {
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
            DomVariableExpander expander = new DomVariableExpander(syntax, variables, toResult);
            SAXResult intoExpander = new SAXResult(expander);
            
            // Perform the chain of transformations, and populate "result"
            DOMSource source = new DOMSource(prototypeElement);
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.transform(source, intoExpander);
            
            return (Document) result.getNode();
        }
        catch (TransformerConfigurationException e) { throw new RuntimeException(e); }
        catch (TransformerException e) {
            if (e.getCause() instanceof VariableNotFoundException) throw (VariableNotFoundException) e.getCause();
            throw new RuntimeException(e); 
        }
    }
}
