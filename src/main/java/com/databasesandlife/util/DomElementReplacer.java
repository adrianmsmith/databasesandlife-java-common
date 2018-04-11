package com.databasesandlife.util;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;

/**
 * Takes a DOM and an element name; 
 * each time that element name is found it is replaced with a specified other element (which may contain other elements).
 *    <p>
 * The element name, that is searched for, may exist in any namespace.
 * The replaced element is changed to be within the namespace of the found element, before insertion
 * in the destination document.
 *
 * @author This source is copyright <a href="http://www.databasesandlife.com">Adrian Smith</a> and licensed under the LGPL 3.
 * @see <a href="https://github.com/adrianmsmith/databasesandlife-java-common">Project on GitHub</a>
 */
public class DomElementReplacer extends DomParser {
    
    public static Element cloneElementAndSetNamespace(Document destDocument, Element source, String newNamespaceUri) {
        Element result = destDocument.createElementNS(newNamespaceUri, source.getNodeName());
        
        NamedNodeMap at = source.getAttributes();
        for (int a = 0; a < at.getLength(); a++) {
            Attr att = (Attr) at.item(a);
            result.setAttribute(att.getNodeName(), att.getValue());
        }
        
        for (Element child : getSubElements(source, "*"))
            result.appendChild(cloneElementAndSetNamespace(destDocument, child, newNamespaceUri));
        
        return result;
    }
    
    public static void replace(Element doc, String elementName, Element replace) {
        for (Element e : getSubElements(doc, "*")) {
            if (e.getNodeName().equals(elementName)) {
                Element r = cloneElementAndSetNamespace(doc.getOwnerDocument(), replace, e.getNamespaceURI());
                doc.insertBefore(r, e);
                doc.removeChild(e);
            } 
            else replace(e, elementName, replace);
        }
    }
    
    public static boolean contains(Element doc, String elementName) {
        for (Element e : getSubElements(doc, "*")) 
            if (e.getNodeName().equals(elementName)) return true;
            else if (contains(e, elementName)) return true;
        return false;
    }
}
