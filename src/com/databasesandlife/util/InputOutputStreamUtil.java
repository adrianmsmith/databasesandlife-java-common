package com.databasesandlife.util;

import java.io.*;
import java.net.*;

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
}