package com.databasesandlife.util;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.w3c.dom.*;

import com.databasesandlife.util.gwtsafe.ConfigurationException;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.*;

/**
 * @author This source is copyright <a href="http://www.databasesandlife.com">Adrian Smith</a> and licensed under the LGPL 3.
 */
public class DomParser {

    /** @param elementNames can be "*" */
    protected static List<Element> getSubElements(Node container, String... elementNames) {
        boolean allElementsDesired = "*".equals(elementNames[0]);
        Set<String> elementNameSet = new HashSet<String>(Arrays.asList(elementNames));
        
        List<Element> result = new ArrayList<Element>();
        NodeList children = container.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child.getNodeType() == Node.ELEMENT_NODE) {
                if (allElementsDesired || elementNameSet.contains(child.getNodeName()))
                    result.add((Element) child);
            }
        }
        return result;
    }

    protected static void assertNoOtherElements(Node container, String... elements)
    throws ConfigurationException {
        Set<String> elementsSet = new HashSet<String>(Arrays.asList(elements));
        NodeList children = container.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child.getNodeType() == Node.ELEMENT_NODE)
                if ( ! elementsSet.contains(child.getNodeName()))
                    throw new ConfigurationException("Unexpected element <" + child.getNodeName() + ">");
        }
    }

    protected static String getMandatoryAttribute(Element node, String attributeName) throws ConfigurationException {
        Attr attributeNode = node.getAttributeNode(attributeName); 
        if (attributeNode == null)
            throw new ConfigurationException("<" + node.getNodeName() + "> expects mandatory attribute '" + attributeName + "'");
        return attributeNode.getValue();
    }

    /** @return null if the attribute is not defined */
    protected static String getOptionalAttribute(Element node, String attributeName) {
        Attr attributeNode = node.getAttributeNode(attributeName); 
        if (attributeNode == null) return null;
        return attributeNode.getValue();
    }
    
    protected static double parseMandatoryDoubleAttribute(Element node, String attributeName)
    throws ConfigurationException {
        String str = getMandatoryAttribute(node, attributeName);
        try { return Double.parseDouble(str); }
        catch (NumberFormatException e) { throw new ConfigurationException("<" + node.getNodeName() + " " + attributeName +
                "='" + str + "'>: couldn't parse decimal attribute"); }
    }

    /** @return null if attribute not found */
    protected static Integer parseOptionalIntegerAttribute(Element node, String attributeName)
    throws ConfigurationException {
        String str = node.getAttribute(attributeName);
        if (str.equals("")) return null;
        try { return Integer.parseInt(str); }
        catch (NumberFormatException e) { throw new ConfigurationException("<" + node.getNodeName() + " " + attributeName +
                "='" + str + "'>: couldn't parse integer attribute"); }
    }

    protected static int parseOptionalIntAttribute(Element node, String attributeName, int defaultValue)
    throws ConfigurationException {
        Integer result = parseOptionalIntegerAttribute(node, attributeName);
        if (result == null) return defaultValue;
        return result;
    }

    /** 
     * @param subNodeName can be "*" 
     * @throws ConfigurationException if more than one element found
     */
    protected static Element getMandatorySingleSubElement(Node node, String subNodeName) throws ConfigurationException {
        List<Element> resultList = getSubElements(node, subNodeName);
        if (resultList.size() != 1) throw new ConfigurationException("<" + node.getNodeName() + ">: found " +
            resultList.size() + ("*".equals(subNodeName) ? " sub-elements" : (" <" + subNodeName + "> sub-elements")));
        return resultList.get(0);
    }

    /** 
     * @param subNodeName can be "*"
     * @return null if element not found 
     * @throws ConfigurationException if more than one element found
     */
    protected static Element getOptionalSingleSubElement(Element node, String subNodeName) throws ConfigurationException {
        List<Element> resultList = getSubElements(node, subNodeName);
        if (resultList.size() == 0) return null;
        else if (resultList.size() == 1) return resultList.get(0);
        else throw new ConfigurationException("<" + node.getNodeName() + ">: found " +
                resultList.size() + ("*".equals(subNodeName) ? " sub-elements" : (" <" + subNodeName + "> sub-elements")));
    }
    
    /** 
     * @param subNodeName can be "*"
     * @return null if element not found 
     * @throws ConfigurationException if more than one element found
     */
    protected static String getOptionalSingleSubElementTextContent(Element node, String subNodeName) throws ConfigurationException {
        Element el = getOptionalSingleSubElement(node, subNodeName);
        if (el == null) return null;
        else return el.getTextContent();
    }

    protected static Set<String> parseSet(Element container, String elementName, String attribute)
    throws ConfigurationException {
        Set<String> result = new HashSet<String>();
        for (Element e : getSubElements(container, elementName)) result.add(getMandatoryAttribute(e, attribute));
        return result;
    }

    protected static Set<String> parseSet(Element container, String elementName)
    throws ConfigurationException {
        Set<String> result = new HashSet<String>();
        for (Element e : getSubElements(container, elementName)) result.add(e.getTextContent().trim());
        return result;
    }

    protected static Map<String, String> parseMap(Element container, String elementName, String keyAttribute, String valueAttribute)
    throws ConfigurationException {
        Map<String, String> result = new HashMap<String, String>();
        for (Element e : getSubElements(container, elementName))
            result.put(getMandatoryAttribute(e, keyAttribute), getMandatoryAttribute(e, valueAttribute));
        return result;
    }

    protected static Map<String, String> parseMap(Element container, String elementName, String keyAttribute)
    throws ConfigurationException {
        Map<String, String> result = new HashMap<String, String>();
        for (Element e : getSubElements(container, elementName))
            result.put(getMandatoryAttribute(e, keyAttribute), e.getTextContent());
        return result;
    }

    //========== XPath API ===========

    public static XPathExpression getExpression(String expression)
    throws XPathExpressionException {
        XPath xpath = XPathFactory.newInstance().newXPath();
        return xpath.compile(expression);
    }

    public static NodeList getNodesFromXPath(Document document, String expression)
    throws XPathExpressionException {
        return (NodeList) getExpression(expression).evaluate(document, XPathConstants.NODESET);
    }

    public static NodeList getNodesFromXPath(Element root, String expression)
    throws XPathExpressionException {
        return getNodesFromXPath(root.getOwnerDocument(), expression);
    }

    public static List<Element> getElementsFromXPath(Document document, String expression)
    throws XPathExpressionException {
        return toElementList(getNodesFromXPath(document, expression));
    }

    public static List<Element> getElementsFromXPath(Element root, String expression)
    throws XPathExpressionException {
        return getElementsFromXPath(root.getOwnerDocument(), expression);
    }

    public static List<Element> toElementList(NodeList nl) {
        List<Element> elements = new ArrayList<Element>(nl.getLength());
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n instanceof Element) {
                elements.add((Element) n);
            }
        }
        return elements;
    }

    public static Element getElementFromXPath(Document document, String expression)
    throws XPathExpressionException {
        return (Element) getExpression(expression).evaluate(document, XPathConstants.NODE);
    }

    public static Element getElementFromXPath(Element root, String expression)
    throws XPathExpressionException {
        return (Element) getExpression(expression).evaluate(root, XPathConstants.NODE);
    }

    public static Element from(File f) throws ConfigurationException {
        try {
            DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            return db.parse(f).getDocumentElement();
        }
        catch (ParserConfigurationException | IOException e) { throw new RuntimeException(e); }
        catch (SAXException e) { throw new ConfigurationException(e); }
    }

    public static Element from(InputStream f) throws ConfigurationException {
        try {
            DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            return db.parse(f).getDocumentElement();
        }
        catch (ParserConfigurationException | IOException e) { throw new RuntimeException(e); }
        catch (SAXException e) { throw new ConfigurationException(e); }
    }
}
