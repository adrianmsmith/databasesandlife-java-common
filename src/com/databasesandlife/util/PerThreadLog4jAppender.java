package com.databasesandlife.util;

import java.util.Map;
import java.util.Map.Entry;
import java.util.WeakHashMap;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggingEvent;

/**
 * Allows each thread to log to its own logger.
 *     <p>
 * Usage:
 * <pre>
 *    final String threadName = ...;
 *    Layout layout = new PatternLayout("%d{yyyy-MM-dd HH:mm:ss} %p: %m%n");
 *    FileAppender toFile = new FileAppender(layout, "C:/" + threadName);
 *    Runnable runnable = new Runnable() {
 *        public void run() {
 *            NDC.push(threadName);
 *            try {
 *                Logger.getLogger(getClass()).info("Some text!");
 *            }
 *            finally { NDC.remove(); }
 *        }
 *    };
 *    Thread thread = new Thread(runnable, threadName); 
 *    PerThreadLog4jAppender
 *        .getSharedInstanceAndAddToLog4jIfNecessary()
 *        .addThreadAppender(thread, toFile);
 * </pre>
 * There is no need to remove appenders from this object once threads exit.
 *
 * @author This source is copyright <a href="http://www.databasesandlife.com">Adrian Smith</a> and licensed under the LGPL 3.
 * @see <a href="https://github.com/adrianmsmith/databasesandlife-java-common">Project on GitHub</a>
 */
public class PerThreadLog4jAppender extends AppenderSkeleton {
    
    protected static PerThreadLog4jAppender sharedInstance = null;
    
    protected Map<Thread, AppenderSkeleton> appenderForThread = new WeakHashMap<>();
    
    protected PerThreadLog4jAppender() { }
    
    public synchronized static PerThreadLog4jAppender getSharedInstanceAndAddToLog4jIfNecessary() {
        if (sharedInstance == null) {
            sharedInstance = new PerThreadLog4jAppender();
            Logger.getRootLogger().addAppender(sharedInstance);
        }
        return sharedInstance;
    }
    
    public void addThreadAppender(Thread t, AppenderSkeleton a) {
        synchronized (appenderForThread) {
            appenderForThread.put(t, a);
        }
    }
    
    /** Makes logs from subThread go to the same appender as the calling thread */
    public void registerSubThread(Thread mainThread, Thread subThread) {
        synchronized (appenderForThread) {
            if (appenderForThread.get(mainThread) != null)
                appenderForThread.put(subThread, appenderForThread.get(mainThread));
        }
    }
    
    @Override public void close() {
        for (AppenderSkeleton a : appenderForThread.values()) a.close();
    }

    @Override public boolean requiresLayout() { return false; }

    @Override protected void append(LoggingEvent event) {
        synchronized (appenderForThread) {
            for (Entry<Thread, AppenderSkeleton> mapEntry : appenderForThread.entrySet()) {
                if (mapEntry.getKey() == Thread.currentThread())
                    mapEntry.getValue().doAppend(event);
            }
        }
    }
}
