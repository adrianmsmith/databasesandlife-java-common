package com.databasesandlife.util;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @author Adrian Smith
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
}
