package com.databasesandlife.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;

/**
 * For strings, UTF-8 bytes are used.
 * 
 * @author This source is copyright <a href="http://www.databasesandlife.com">Adrian Smith</a> and licensed under the LGPL 3.
 * @see <a href="https://github.com/adrianmsmith/databasesandlife-java-common">Project on GitHub</a>
 */
public class MD5Hex {

    protected static String bytesToHex(byte[] bytes) {
        StringBuffer hexString = new StringBuffer();
        for (int i=0;i<bytes.length;i++) {
            String x = "0" + Integer.toHexString(0xFF & bytes[i]);
            hexString.append(x.substring(x.length() - 2));
        }
        return hexString.toString();
    }

    public static String md5(byte[] stuff) {
        try {
            MessageDigest algorithm = MessageDigest.getInstance("MD5");
            algorithm.reset();
            algorithm.update(stuff);
            byte[] messageDigest = algorithm.digest();
            return bytesToHex(messageDigest);
        }
        catch (NoSuchAlgorithmException e) { throw new RuntimeException(e); }
    }

    public static String md5(String stuff) {
        try { return md5(stuff.getBytes("UTF-8")); }
        catch (UnsupportedEncodingException e) { throw new RuntimeException(e); }
    }

    public static String md5(CharSequence stuff) {
        return md5(stuff.toString());
    }

    /** @param stuff client must close this input stream */
    public static String md5(InputStream stuff) throws IOException {
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            byte[] buffer = new byte[8192];
            int read = 0;
            while( (read = stuff.read(buffer)) > 0) digest.update(buffer, 0, read);
            return bytesToHex(digest.digest());        
        }
        catch (NoSuchAlgorithmException e) { throw new RuntimeException(e); }
    }

    public static String md5(File stuff) {
       try {
            FileInputStream fileStr = new FileInputStream(stuff);
            try {
                BufferedInputStream str = new BufferedInputStream(fileStr);
                try {
                    return MD5Hex.md5(str);
                }
                finally { str.close(); }
            }
            finally { fileStr.close(); }
        }
        catch (IOException e) { throw new RuntimeException(e); }
    }

    public static String md5(Document xslt) {
        try {
            StringWriter str = new StringWriter();        
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty("omit-xml-declaration","yes");
            transformer.transform(new DOMSource(xslt), new StreamResult(str));
            return md5(str.toString());
        }
        catch (TransformerException e) { throw new RuntimeException(e); }
    }
}
