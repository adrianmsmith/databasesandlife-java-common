package com.databasesandlife.util;

import java.io.ByteArrayInputStream;
import java.io.CharArrayWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.Properties;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.wicket.util.io.ByteArrayOutputStream;
import org.w3c.dom.Element;

/**
 * @author This source is copyright <a href="http://www.databasesandlife.com">Adrian Smith</a> and licensed under the LGPL 3.
 * @version $Revision$
 */
public class InputOutputStreamUtil {
    
    public static class Response {
        public String contentType;
        public byte[] byteArray;
    }

    public static void copyBytesFromInputToOutputStream(OutputStream oStream, InputStream iStream)
    throws IOException {
        byte[] buffer = new byte[10000];
        int bytesRead;
        while ((bytesRead = iStream.read(buffer)) >= 0)
           oStream.write(buffer, 0, bytesRead);
    }
    
    public static byte[] readBytesFromInputStream(InputStream iStream)
    throws IOException {
        ByteArrayOutputStream oStream = new ByteArrayOutputStream();
        copyBytesFromInputToOutputStream(oStream, iStream);
        return oStream.toByteArray();
    }
    
    public static String readStringFromReader(Reader iStream)
    throws IOException {
        CharArrayWriter oStream = new CharArrayWriter();
        char[] buffer = new char[10000];
        int charsRead;
        while ((charsRead = iStream.read(buffer)) >= 0)
           oStream.write(buffer, 0, charsRead);
        return oStream.toString();
    }

    public static Response readBytesFromUrl(URL url) {
        try {
            Response r = new Response();
            URLConnection conn = url.openConnection();
            r.contentType = conn.getContentType();
            if (r.contentType == null) throw new RuntimeException(
                "cannot open input stream on URL '"+url.toExternalForm()+"'");
            r.byteArray = readBytesFromInputStream(conn.getInputStream());
            return r;
        }
        catch (IOException e) { throw new RuntimeException(e); }
    }
    
    public static void writeStringToFileUtf8(File f, String str)
    throws IOException {
        FileOutputStream o = new FileOutputStream(f);
        try {
            OutputStreamWriter w = new OutputStreamWriter(o, "UTF-8");
            w.write(str, 0, str.length());
            w.close();
        }
        finally { o.close(); }
    }

    public static void writeBytesToOutputStream(OutputStream out, byte[] src) throws IOException {
        copyBytesFromInputToOutputStream(out, new ByteArrayInputStream(src));
    }
    
    public static String prettyPrintXml(Element xml) {
        try {
            Properties systemProperties = System.getProperties();
            systemProperties.remove("javax.xml.transform.TransformerFactory");
            System.setProperties(systemProperties);
            
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            DOMSource source = new DOMSource(xml);
            StringWriter writer = new StringWriter();
            StreamResult result = new StreamResult(writer);
            transformer.transform(source, result);
            return writer.toString();
        }
        catch (TransformerException e) { throw new RuntimeException(e); }
    }
}
