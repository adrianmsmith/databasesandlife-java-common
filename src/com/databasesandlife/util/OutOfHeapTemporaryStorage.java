package com.databasesandlife.util;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * A temporary file which can store arbitrary data for the duration of a process.
 * If we are dealing with a 4GB XML file, we don't want lots of seldom accessed strings hanging around the heap.
 * Currently strings and XML documents may be stored.
 *    <p>
 * Usage:
 * For each object to be stored, firstly a "container" is created and then data is appended/written to the container.
 * In this way, it's possible for an XML SAX parser, finding an XML [myObject type="x"] tag, to create the container and place
 * it in the appropriate place in the destination data structure (e.g. if its place dependent on the element's attribute), 
 * but for the container to be actually filled with data
 * while processing further tags, e.g. child elements of the original tag.  
 * <pre>
 *     OutOfHeapTemporaryFileStorage file = new OutOfHeapTemporaryFileStorage();            // file created in /tmp
 *     
 *     // Persisting and loading a string
 *     OutOfHeapString str = file.newStringContainer();
 *     str.append("xyz");        
 *     System.out.println("str is: " + str.toString()); // fetches string from file
 *     
 *     // Persisting and loading chunks of XML
 *     org.w3c.dom.Element anElement = ....;
 *     OutOfHeapXml xml = file.newXmlContainer();
 *     xml.setXml(anElement); // call only once
 *     org.w3c.dom.Element afterLoading = x.toXmlDomElement();
 * </pre>
 *
 * @author This source is copyright <a href="http://www.databasesandlife.com">Adrian Smith</a> and licensed under the LGPL 3.
 * @version $Revision$
 */
public class OutOfHeapTemporaryStorage {

    RandomAccessFile file;

    public OutOfHeapTemporaryStorage() {
        try {
            File stringsFile = File.createTempFile("OutOfHeapTemporaryFileStorage-", ".dat");
            stringsFile.deleteOnExit();
            file = new RandomAccessFile(stringsFile, "rw");
        }
        catch (IOException e) { throw new RuntimeException(e); }
    }
    
    // ------------------------------------------------------------------------------------
    // String storage
    // ------------------------------------------------------------------------------------

    public class OutOfHeapString {
        protected long offset;
        protected int byteLength;

        public String toString() { return readString(this); }
        public void append(String x) { appendToString(this, x); }
        public boolean isEmpty() { return byteLength==0; }
    }
    
    public synchronized OutOfHeapString newStringContainer() {
        try {
            OutOfHeapString result = new OutOfHeapString();
            result.offset = file.length();
            result.byteLength = 0;
            return result;
        }
        catch (IOException e) { throw new RuntimeException(e); }
    }
    
    public synchronized OutOfHeapString newStringContainer(String initialValue) {
        OutOfHeapString result = newStringContainer();
        result.append(initialValue);
        return result;
    }

    protected synchronized void appendToString(OutOfHeapString s, String suffix) {
        try {
            if (s.offset + s.byteLength != file.length()) throw new IllegalStateException("String is not at end of file");
            file.seek(file.length());
            byte[] suffixBytes = suffix.getBytes("UTF-8");
            file.write(suffixBytes);
            s.byteLength += suffixBytes.length;
        }
        catch (IOException e) { throw new RuntimeException(e); }
    }

    protected synchronized String readString(OutOfHeapString s) {
        try {
            file.seek(s.offset);
            byte[] resultBytes = new byte[s.byteLength];
            int bytesRead = file.read(resultBytes);
            if (bytesRead != s.byteLength) throw new RuntimeException("Could not read entire string");
            return new String(resultBytes, "UTF-8");
        }
        catch (IOException e) { throw new RuntimeException(e); }
    }
    
    // ------------------------------------------------------------------------------------
    // XML storage
    // ------------------------------------------------------------------------------------

    public class OutOfHeapXml {
        protected OutOfHeapString stringRepresentation;
        
        public void setXml(Element x) { writeXml(this, x); }
        
        /** You will need to import this into your DOM document */
        public Element toXmlDomElement() { return readXml(this); }
    }
    
    public synchronized OutOfHeapXml newXmlContainer() {
        OutOfHeapString underlyingString = newStringContainer();
        
        OutOfHeapXml result = new OutOfHeapXml();
        result.stringRepresentation = underlyingString;
        return result;
    }

    protected synchronized void writeXml(OutOfHeapXml xml, Element element) {
        try {
            if ( ! xml.stringRepresentation.isEmpty()) throw new IllegalStateException("XML container can only be written once");
            
            StringWriter str = new StringWriter();
            
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty("omit-xml-declaration","yes");
            transformer.transform(new DOMSource(element), new StreamResult(str));
            
            xml.stringRepresentation.append(str.toString());
        }
        catch (TransformerConfigurationException e) { throw new RuntimeException(e); }
        catch (TransformerException e) { throw new RuntimeException(e); }
    }
    
    protected synchronized Element readXml(OutOfHeapXml inFile) {
        try {
            DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
            dbfac.setNamespaceAware(true);
            DocumentBuilder docBuilder = dbfac.newDocumentBuilder();
            InputSource inputSource = new InputSource(new StringReader(inFile.stringRepresentation.toString()));
            Document doc = docBuilder.parse(inputSource);
            return doc.getDocumentElement();
        }
        catch (ParserConfigurationException e) { throw new RuntimeException(e); }
        catch (IOException e) { throw new RuntimeException(e); }
        catch (SAXException e) { throw new RuntimeException(e); }
    }
}
