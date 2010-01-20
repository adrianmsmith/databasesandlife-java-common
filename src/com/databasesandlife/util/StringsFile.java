package com.databasesandlife.util;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * A strings file is a place where Strings can be stored out of the JVM.
 * If we are dealing with a 4GB XML file, we don't want all those strings hanging around core otherwise core will become full.
 *    <p>
 * Usage:
 * <pre>
 *     StringsFile file = new StringsFile();            // file created in /tmp
 *     StringInFile str = file.newString("abc");        
 *     System.out.println("str is: " + str.toString()); // fetches string from file
 * </pre>
 *
 * @author Adrian Smith
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
}
