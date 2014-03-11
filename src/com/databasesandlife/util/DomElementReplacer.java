package com.databasesandlife.util;

import org.w3c.dom.Element;

/**
 * Takes a DOM and an element name; 
 * each time that element name is found it is replaced with a specified other element (which may contain other elements).
 */
public class DomElementReplacer extends DomParser {
    
    public static void replace(Element doc, String elementName, Element replace) {
        for (Element e : getSubElements(doc, "*")) {
            if (e.getNodeName().equals(elementName)) {
                Element r = (Element) doc.getOwnerDocument().importNode(replace, true);
                doc.insertBefore(r, e);
                doc.removeChild(e);
            } 
            else replace(e, elementName, replace);
        }
    }
}
