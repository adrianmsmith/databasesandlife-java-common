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
 * A strings file is a place where Strings can be stored out of the JVM.
 * If we are dealing with a 4GB XML file, we don't want all those strings hanging around core otherwise core will become full.
 * A strings file can also persist arbitrary blocks of XML as represented by DOM Elements outside the JVM.
 *    <p>
 * Usage:
 * <pre>
 *     StringsFile file = new StringsFile();            // file created in /tmp
 *     
 *     // Persisting and loading a string
 *     StringInFile str = file.newString("abc");        
 *     System.out.println("str is: " + str.toString()); // fetches string from file
 *     
 *     // Persisting and loading chunks of XML
 *     org.w3c.dom.Element anElement = ....;
 *     XmlInStringsFile x = file.newXml(anElement);
 *     org.w3c.dom.Element afterLoading = x.toXmlDomElement();
 * </pre>
 *
 * @author This source is copyright <a href="http://www.databasesandlife.com">Adrian Smith</a> and licensed under the LGPL 3.
 * @version $Revision$
 */
public class StringsFile {

    RandomAccessFile file;

    public class StringCannotBeAppendedException extends RuntimeException { StringCannotBeAppendedException() { } }

    public class StringInFile {
        protected long offset;
        protected int byteLength;

        public String toString() { return readString(this); }
        public void append(String x) { appendToString(this, x); }
    }
    
    public class XmlInStringsFile {
        protected StringInFile stringRepresentation;
        
        public Element toXmlDomElement() { return readXml(this); }
    }

    public StringsFile(File stringsFile) {
        try {
            file = new RandomAccessFile(stringsFile, "rw");
        }
        catch (IOException e) { throw new RuntimeException(e); }
    }

    public StringsFile() {
        try {
            File stringsFile = File.createTempFile("StringsFile-", ".dat");
            stringsFile.deleteOnExit();
            file = new RandomAccessFile(stringsFile, "rw");
        }
        catch (IOException e) { throw new RuntimeException(e); }
    }

    public synchronized StringInFile newString(String str) {
        try {
            StringInFile result = new StringInFile();
            result.offset = file.length();
            file.seek(result.offset);
            byte[] bytes = str.getBytes("UTF-8");
            file.write(bytes);
            result.byteLength = bytes.length;
            return result;
        }
        catch (IOException e) { throw new RuntimeException(e); }
    }

    /** @throws StringCannotBeAppendedException if string is not at end of file */
    public synchronized void appendToString(StringInFile s, String suffix) throws StringCannotBeAppendedException {
        try {
            if (s.offset + s.byteLength != file.length()) throw new StringCannotBeAppendedException();
            file.seek(file.length());
            byte[] suffixBytes = suffix.getBytes("UTF-8");
            file.write(suffixBytes);
            s.byteLength += suffixBytes.length;
        }
        catch (IOException e) { throw new RuntimeException(e); }
    }

    public synchronized String readString(StringInFile s) {
        try {
            file.seek(s.offset);
            byte[] resultBytes = new byte[s.byteLength];
            int bytesRead = file.read(resultBytes);
            if (bytesRead != s.byteLength) throw new RuntimeException("Could not read entire string");
            return new String(resultBytes, "UTF-8");
        }
        catch (IOException e) { throw new RuntimeException(e); }
    }
    
    public synchronized XmlInStringsFile newXml(Element element) {
        try {
            StringWriter str = new StringWriter();
            
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.transform(new DOMSource(element), new StreamResult(str));
            
            XmlInStringsFile result = new XmlInStringsFile();
            result.stringRepresentation = newString(str.toString());
            
            return result;
        }
        catch (TransformerConfigurationException e) { throw new RuntimeException(e); }
        catch (TransformerException e) { throw new RuntimeException(e); }
    }
    
    public synchronized Element readXml(XmlInStringsFile inFile) {
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
