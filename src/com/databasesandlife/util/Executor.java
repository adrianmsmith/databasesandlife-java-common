package com.databasesandlife.util;

import org.apache.log4j.Logger;
import org.apache.log4j.Priority;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class Executor {

    public static void run(String command) {
        try {
            Process p = Runtime.getRuntime().exec(command);
            Thread stdoutThread = spawnFor(new ProcessStreamReaderRunnable(p.getInputStream(), Priority.INFO));
            Thread stderrThread = spawnFor(new ProcessStreamReaderRunnable(p.getErrorStream(), Priority.ERROR));
            stdoutThread.start();
            stderrThread.start();
            int returnCode = p.waitFor();
            if (returnCode != 0) {
                throw new ReturnCodeNotZeroException("Return code for command '" + command + "' is '" + returnCode + "'");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

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
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    Logger.getLogger(getClass()).log(priority, line);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        }
    }

    public static class ReturnCodeNotZeroException extends RuntimeException {
        public ReturnCodeNotZeroException(String message) {
            super(message);
        }

        public ReturnCodeNotZeroException(String message, Throwable cause) {
            super(message, cause);
        }
    }

}
