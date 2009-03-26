package com.databasesandlife;

/**
 * Times a calculation.
 *
 * @author Adrian Smith
 */
public class Timer {

    public static long measureWallclockMilliseconds(Runnable task, long minimumDurationMillis) {
        long startTime = System.currentTimeMillis();
        long endTime = startTime + minimumDurationMillis;
        int iterations = 0;
        while (System.currentTimeMillis() < endTime) {
            iterations++;
            task.run();
        }
        long duration = System.currentTimeMillis() - endTime;
        return duration / iterations;
    }
}
