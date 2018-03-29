package com.databasesandlife.util;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.log4j.Priority;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * @author This source is copyright <a href="http://www.databasesandlife.com">Adrian Smith</a> and licensed under the LGPL 3.
 * @see <a href="https://github.com/adrianmsmith/databasesandlife-java-common">Project on GitHub</a>
 */
public class Executor {

    /** 
     * @param command can be String or String[]
     * @param currentDirectoryOrNull if not null, what should the 'current directory' of the new process be? 
     */
    @SuppressWarnings("deprecation")
    protected static void run(Object command, File currentDirectoryOrNull) {
        try {
            final String commandForLog;
            final Process p;
            if (command instanceof String) {
                commandForLog = (String)command;
                p = Runtime.getRuntime().exec((String)command, null, currentDirectoryOrNull);
            }
            else if (command instanceof String[]) {
                commandForLog = StringUtils.join((String[])command, " ");
                p = Runtime.getRuntime().exec((String[])command, null, currentDirectoryOrNull);
            }
            else throw new RuntimeException("comamnd must be String or String[], is " + command.getClass());
            
            Thread stdoutThread = spawnFor(new ProcessStreamReaderRunnable(p.getInputStream(), Priority.INFO));
            Thread stderrThread = spawnFor(new ProcessStreamReaderRunnable(p.getErrorStream(), Priority.ERROR));
            stdoutThread.start();
            stderrThread.start();
            int returnCode = p.waitFor();
            if (returnCode != 0) {
                throw new ReturnCodeNotZeroException("Return code for command '" + commandForLog + "' is '" + returnCode + "'");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
    
    public static void run(String[] command, File currentDirectoryOrNull) { run((Object)command, currentDirectoryOrNull); }
    public static void run(String command, File currentDirectoryOrNull) { run((Object)command, currentDirectoryOrNull); }
    public static void run(String[] command) { run(command, null); }
    public static void run(String command) { run(command, null); }
    
    private static Thread spawnFor(Runnable r) {
        Thread t = new Thread(r);
        t.setDaemon(true);
        return t;
    }

    public static class ProcessStreamReaderRunnable implements Runnable {

        private InputStream stream;
        private Priority priority;

        public ProcessStreamReaderRunnable(InputStream stream, Priority priority) {
            this.stream = stream;
            this.priority = priority;
        }

        @Override
        public void run() {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    Logger.getLogger(getClass()).log(priority, line);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        }
    }

    @SuppressWarnings("serial")
    public static class ReturnCodeNotZeroException extends RuntimeException {
        public ReturnCodeNotZeroException(String message) {
            super(message);
        }

        public ReturnCodeNotZeroException(String message, Throwable cause) {
            super(message, cause);
        }
    }

}
