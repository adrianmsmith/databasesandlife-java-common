package com.databasesandlife.util.gwtsafe;

import com.google.gwt.core.shared.GwtIncompatible;

public class DurationTimer {

    public static class TimerResult {
        private long millisPerIter;
        private double iterPerSecond;

        @GwtIncompatible
        public String toString() { return String.format("%.3f seconds (%.1f per second)", millisPerIter/1000.0, iterPerSecond); }
        public double getSecondsPerIter() { return millisPerIter / 1_000.0; }
        public double getItersPerSecond() { return iterPerSecond; }
    }

    /**
     * @param task runs this multiple times
     * @param minimumDurationMillis thousandths of a second
     * @see com.google.common.base.Stopwatch
     */
    public static TimerResult measureWallclockTime(Runnable task, long minimumDurationMillis, int warmupIterations) {
        // Ignore the first iterations, e.g. on one unit test it took 0.7 seconds one-time to do class loading of the class under test
        for (int i = 0; i < warmupIterations; i++) task.run();

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

    public static TimerResult measureWallclockTime(Runnable task, long minimumDurationMillis) {
        return measureWallclockTime(task, minimumDurationMillis, 0);
    }

}
