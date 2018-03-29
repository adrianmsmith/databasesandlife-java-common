package com.databasesandlife.util;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.CharArrayWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.w3c.dom.Element;

import com.esotericsoftware.yamlbeans.YamlReader;

/**
 * @author This source is copyright <a href="http://www.databasesandlife.com">Adrian Smith</a> and licensed under the LGPL 3.
 * @see <a href="https://github.com/adrianmsmith/databasesandlife-java-common">Project on GitHub</a>
 */
public class InputOutputStreamUtil {

    public static class Response {
        public String contentType;
        public byte[] byteArray;
    }

    /** @deprecated use Apache Commons, {@link IOUtils#copy(InputStream, OutputStream)} instead */
    public static void copyBytesFromInputToOutputStream(OutputStream oStream, InputStream iStream)
    throws IOException {
        byte[] buffer = new byte[10000];
        int bytesRead;
        while ((bytesRead = iStream.read(buffer)) >= 0)
           oStream.write(buffer, 0, bytesRead);
    }

    /** @deprecated use Apache Commons, {@link IOUtils#toByteArray(InputStream)} instead */
    public static byte[] readBytesFromInputStream(InputStream iStream)
    throws IOException {
        ByteArrayOutputStream oStream = new ByteArrayOutputStream();
        copyBytesFromInputToOutputStream(oStream, iStream);
        return oStream.toByteArray();
    }

    /** @deprecated use Apache Commons, {@link IOUtils#toString(Reader)} instead */
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

    /** @deprecated use Apache Commons, {@link FileUtils#writeStringToFile(File, String, String)} instead */
    public static void writeStringToFileUtf8(File f, String str)
    throws IOException {
        FileOutputStream o = new FileOutputStream(f);
        try {
            OutputStreamWriter w = new OutputStreamWriter(o, StandardCharsets.UTF_8);
            w.write(str, 0, str.length());
            w.close();
        }
        finally { o.close(); }
    }

    public static void writeBytesToOutputStream(OutputStream out, byte[] src) throws IOException {
        copyBytesFromInputToOutputStream(out, new ByteArrayInputStream(src));
    }

    public static void prettyPrintXml(Writer writer, Element xml) {
        try {
            Properties systemProperties = System.getProperties();
            systemProperties.remove("javax.xml.transform.TransformerFactory");
            System.setProperties(systemProperties);

            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            DOMSource source = new DOMSource(xml);
            StreamResult result = new StreamResult(writer);
            transformer.transform(source, result);
        }
        catch (TransformerException e) { throw new RuntimeException(e); }
    }

    public static String prettyPrintXml(Element xml) {
        StringWriter writer = new StringWriter();
        prettyPrintXml(writer, xml);
        return writer.toString();
    }

    /** If class is "X.java" then parse the "X.yaml" */
    public static Object parseYamlConfig(Class<?> c) {
        String name = c.getName().replaceAll("\\.", "/"); // e.g. "com/mypkg/MyClass"
        InputStream stream = c.getClassLoader().getResourceAsStream(name + ".yaml");
        if (stream == null) throw new IllegalArgumentException("No '.yaml' file for class '" + c.getName() + "'");
        try {
            BufferedReader yamlCharacterReader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8));
            YamlReader yamlParser = new YamlReader(yamlCharacterReader);
            return yamlParser.read();
        }
        catch (IOException e) { throw new RuntimeException(e); }
        finally {
            try { stream.close(); }
            catch (IOException e) { }
        }
    }

    public static void writeXmlToFile(File out, Element xml) {
        try (Writer writer = new OutputStreamWriter(new FileOutputStream(out), StandardCharsets.UTF_8)) {
            prettyPrintXml(writer, xml);
        }
        catch (IOException e) { throw new RuntimeException(e); }
    }
}
