package com.databasesandlife.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;

import junit.framework.TestCase;

import org.apache.log4j.FileAppender;
import org.apache.log4j.Layout;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

/**
 * @author This source is copyright <a href="http://www.databasesandlife.com">Adrian Smith</a> and licensed under the LGPL 3.
 */
public class PerThreadLog4jAppenderTest extends TestCase {
    
    private static String readFile(File path) throws IOException {
        FileInputStream stream = new FileInputStream(path);
        try {
            FileChannel fc = stream.getChannel();
            MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
            /* Instead of using default, pass in a decoder. */
            return Charset.defaultCharset().decode(bb).toString();
        } 
        finally { stream.close(); }
    }

    public void test() throws Exception {
        Layout layout = new PatternLayout("%m");    // Just the message; no date, no class, no newline
        
        File fileA = File.createTempFile("PerThreadLog4jAppenderTest-", ".log");
        FileAppender appenderA = new FileAppender(layout, fileA.getAbsolutePath());
        Runnable runnableA = new Runnable() { public void run() { Logger.getLogger(getClass()).info("a"); } };
        Thread threadA = new Thread(runnableA);
        PerThreadLog4jAppender.getSharedInstanceAndAddToLog4jIfNecessary().addThreadAppender(threadA, appenderA);
        threadA.start();

        File fileB = File.createTempFile("PerThreadLog4jAppenderTest-", ".log");
        FileAppender appenderB = new FileAppender(layout, fileB.getAbsolutePath());
        Runnable runnableB = new Runnable() { public void run() { Logger.getLogger(getClass()).info("b"); } };
        Thread threadB = new Thread(runnableB);
        PerThreadLog4jAppender.getSharedInstanceAndAddToLog4jIfNecessary().addThreadAppender(threadB, appenderB);
        threadB.start();
        
        threadA.join();
        threadB.join();
        
        assertEquals("a", readFile(fileA));
        assertEquals("b", readFile(fileB));
    }
}
