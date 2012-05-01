package com.databasesandlife.util;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.databasesandlife.util.gwtsafe.ConfigurationException;

/**
 * @author This source is copyright <a href="http://www.databasesandlife.com">Adrian Smith</a> and licensed under the LGPL 3.
 * @version $Revision$
 */
public class DomParser {

    /** @param elementName can be "*" */
    protected static List<Element> getSubElements(Node container, String elementName) {
        Vector<Element> result = new Vector<Element>();
        NodeList children = container.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child.getNodeType() == Node.ELEMENT_NODE) {
                if ("*".equals(elementName) || child.getNodeName().equals(elementName))
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

    protected static double parseMandatoryValue(Element container) throws ConfigurationException {
        String val = getMandatorySingleSubElement(container, "value").getTextContent();
        try { return Double.parseDouble(val); }
        catch (NumberFormatException e) { throw new ConfigurationException("<value> of '" + val + "' is not a number"); }
    }

    protected static String getMandatoryAttribute(Element node, String attributeName) throws ConfigurationException {
        String result = node.getAttribute(attributeName);
        if (result.equals(""))
            throw new ConfigurationException("<" + node.getNodeName() + "> expects mandatory attribute '" + attributeName + "'");
        return result;
    }

    /** @return null if the attribute is not defined */
    protected static String getOptionalAttribute(Element node, String attributeName) {
        String result = node.getAttribute(attributeName);
        if (result.equals("")) return null;
        return result;
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

    /** @param subNodeName can be "*" */
    protected static Element getMandatorySingleSubElement(Node node, String subNodeName) throws ConfigurationException {
        List<Element> resultList = getSubElements(node, subNodeName);
        if (resultList.size() != 1) throw new ConfigurationException("<" + node.getNodeName() + ">: found " +
                resultList.size() + ("*".equals(subNodeName) ? " sub-elements" : (" <" + subNodeName + "> sub-elements")));
        return resultList.get(0);
    }

    /** 
     * @param subNodeName can be "*"
     * @return null if element not found 
     */
    protected static Element getOptionalSingleSubElement(Element node, String subNodeName) throws ConfigurationException {
        List<Element> resultList = getSubElements(node, subNodeName);
        if (resultList.size() == 0) return null;
        else if (resultList.size() == 1) return resultList.get(0);
        else throw new ConfigurationException("<" + node.getNodeName() + ">: found " +
                resultList.size() + ("*".equals(subNodeName) ? " sub-elements" : (" <" + subNodeName + "> sub-elements")));
    }
}
