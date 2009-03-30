package com.databasesandlife;

/**
 * Times a calculation.
 *
 * @author Adrian Smith
 */
public class Timer {

    public static class TimerResult {
        long millis;
        int iterPerSecond;

        public String toString() { return String.format("%.3f seconds (%,d per second)", millis/1000.0, iterPerSecond); }
    }

    public static TimerResult measureWallclockMilliseconds(Runnable task, long minimumDurationMillis) {
        long startTime = System.currentTimeMillis();
        long endTime = startTime + minimumDurationMillis;
        int iterations = 0;
        while (System.currentTimeMillis() < endTime) {
            iterations++;
            task.run();
        }
        long durationMillis = System.currentTimeMillis() - endTime;
        
        TimerResult result = new TimerResult();
        result.millis = durationMillis / iterations;
        result.iterPerSecond = (int) (1000L * iterations / durationMillis);
        return result;
    }
}
