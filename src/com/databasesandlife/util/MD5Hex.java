package com.databasesandlife.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @author This source is copyright <a href="http://www.databasesandlife.com">Adrian Smith</a> and licensed under the LGPL 3.
 * @version $Revision$
 */
public class MD5Hex {

    /** Try and indicate to garbage collector that intermediate string is no longer needed after byte[] produced */
    public static byte[] bytesFromStringBuilder(StringBuilder inputString) {
        try {
            int length = inputString.length();
            byte[] result = new byte[length];
            int minIncl = 0;
            while (minIncl < length) {
                int maxExcl = minIncl + 1000000;
                if (maxExcl > length) maxExcl = length;
                int blockLength = maxExcl - minIncl;
                String srcStr = inputString.substring(minIncl, maxExcl);
                System.arraycopy(srcStr.getBytes("US-ASCII"), 0, result, minIncl, blockLength);
                minIncl = maxExcl;
            }
            return result;
        }
        catch (UnsupportedEncodingException e) { throw new RuntimeException(e); }
    }

    public static String bytesToHex(byte[] bytes) {
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

    /** @param stuff UTF-8 bytes are used */
    public static String md5(String stuff) {
        try {
            return md5(stuff.getBytes("UTF-8"));
        }
        catch (UnsupportedEncodingException e) { throw new RuntimeException(e); }
    }

    /** @param stuff ASCII bytes are used */
    public static String md5(StringBuilder stuff) {
        return md5(bytesFromStringBuilder(stuff));
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
}
