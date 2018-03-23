package com.databasesandlife.util;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

/**
 * Times a calculation.
 *    <p>
 * Usage:
 * <pre>try (Timer ignored = new Timer("doing something")) { ... }</pre>
 *
 * @author This source is copyright <a href="http://www.databasesandlife.com">Adrian Smith</a> and licensed under the LGPL 3.
 * @see <a href="https://github.com/adrianmsmith/databasesandlife-java-common">Project on GitHub</a>
 */
public class Timer implements AutoCloseable {

    static Logger logger = Logger.getLogger(Timer.class);
    static ThreadLocal<Map<String, Long>> start = new ThreadLocal<>();

    protected static String getPrefix() {
        int count = start.get().size();

        if (count >= 10) {
            return "I x"+count+" ";
        } else {
            StringBuilder prefix = new StringBuilder();
            for (int i = 0; i < count; i++) prefix.append("I ");
            return prefix.toString();
        }
    }

    /** @deprecated use <code>try (Timer ignored = new Timer("...")) { .. }</code> instead */
    public static void start(String name) {
        if (start.get() == null) start.set(new HashMap<String, Long>());

        if (start.get().containsKey(name)) {
            logger.warn("Timer start '"+name+"' but this name is already active");
            return;
        }

        logger.info(getPrefix() + "'" + name + "' start");
        start.get().put(name, System.nanoTime());
    }
    
    public static String formatDurationNanos(long durationNanoSeconds) {
        double seconds = durationNanoSeconds / (1000*1000*1000.0);
        
        int minutes = (int) Math.floor(seconds / 60);
        seconds -= 60 * minutes;

        int hours = (int) Math.floor(minutes / 60);       
        minutes -= 60 * hours;
        
        String hoursStr = (hours == 0) ? "" : (hours + " hrs ");
        String minutesStr = (hours == 0 && minutes == 0) ? "" : (minutes + " min ");

        return String.format("%s%s%.3f sec", hoursStr, minutesStr, seconds);
    }

    /** @deprecated use <code>try (Timer ignored = new Timer("...")) { .. }</code> instead */
    public static void end(String name) {
        if (start.get() == null) start.set(new HashMap<String, Long>());

        if ( ! start.get().containsKey(name)) {
            logger.warn("Timer end '" + name + "' but was never started");
            return;
        }

        long durationNanos = System.nanoTime() - start.get().get(name);
        start.get().remove(name);

        logger.info(String.format("%s'%s' end (%s)", getPrefix(), name, formatDurationNanos(durationNanos)));
    }

    // ------------------------------------------------------------------------------------------------------------------------

    public static class TimerResult {
        private long millisPerIter;
        private double iterPerSecond;

        public String toString() { return String.format("%.3f seconds (%.1f per second)", millisPerIter/1000.0, iterPerSecond); }
        public double getSecondsPerIter() { return millisPerIter / 1_000.0; }
        public double getItersPerSecond() { return iterPerSecond; }
    }

    /**
     * @param task runs this multiple times
     * @param minimumDurationMillis thousandths of a second
     * @see com.google.common.base.Stopwatch
     */
    public static TimerResult measureWallclockTime(Runnable task, long minimumDurationMillis) {
        // Ignore the first iteration, e.g. on one unit test it took 0.7 seconds one-time to do class loading of the class under test
        task.run();
        
        long startTime = System.currentTimeMillis();
        long endTime = startTime + minimumDurationMillis;
        int iterations = 0;
        while (System.currentTimeMillis() < endTime) {
            iterations++;
            task.run();
        }
        long durationMillis = System.currentTimeMillis() - startTime;

        TimerResult result = new TimerResult();
        result.millisPerIter = durationMillis / iterations;
        result.iterPerSecond = 1000.0 * iterations / durationMillis;
        return result;
    }

    // ------------------------------------------------------------------------------------------------------------------------
    
    protected String name;
    public Timer(String x) { Timer.start(name = x); }
    @Override public void close()  { Timer.end(name); }
}
