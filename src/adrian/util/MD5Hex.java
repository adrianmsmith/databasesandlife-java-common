package adrian.util;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @author Adrian Smith
 */
public class MD5Hex {

    public static String md5(byte[] stuff) {
        try {
            MessageDigest algorithm = MessageDigest.getInstance("MD5");
            algorithm.reset();
            algorithm.update(stuff);
            byte messageDigest[] = algorithm.digest();

            StringBuffer hexString = new StringBuffer();
            for (int i=0;i<messageDigest.length;i++)
                hexString.append(Integer.toHexString(0xFF & messageDigest[i]));
            return hexString.toString();
        }
        catch (NoSuchAlgorithmException e) { throw new RuntimeException(e); }
    }

    public static String md5(String stuff) {
        try {
            return md5(stuff.getBytes("UTF-8"));
        }
        catch (UnsupportedEncodingException e) { throw new RuntimeException(e); }
    }
}
