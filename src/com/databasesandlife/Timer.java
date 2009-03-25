package com.databasesandlife;

/**
 *
 *
 * @author Adrian Smith
 */
public class Timer {

    public static long measureWallclockMilliseconds(Runnable task, long minimumMillis) {
        long start = System.currentTimeMillis();
        int iterations = 0;
        do {
            task.run();
            iterations++;
        } while (System.currentTimeMillis() < start + minimumMillis);
        return (System.currentTimeMillis() - start) / iterations;
    }
}
