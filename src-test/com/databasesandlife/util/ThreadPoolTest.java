package com.databasesandlife.util;

import java.util.concurrent.TimeUnit;

import com.google.common.base.Stopwatch;

import junit.framework.TestCase;

/**
 * @author This source is copyright <a href="http://www.databasesandlife.com">Adrian Smith</a> and licensed under the LGPL 3.
 * @see <a href="https://github.com/adrianmsmith/databasesandlife-java-common">Project on GitHub</a>
 */
public class ThreadPoolTest extends TestCase {
    
    public void test() {
        int threadCount = 2;
        int taskCount = 10;
        int durationMillis = 100;
        
        ThreadPool pool = new ThreadPool();
        pool.setThreadCount(threadCount);
        Runnable sleepTask = new Runnable() {
            @Override public void run() {
                try { Thread.sleep(durationMillis); }
                catch (InterruptedException e) { }
            }
        };
        Runnable seedTask = new Runnable() {
            @Override public void run() {
                for (int i = 0 ; i < taskCount; i++)
                    pool.addTask(sleepTask);
            };
        };        
        pool.addTask(seedTask);
        
        Stopwatch stopwatch = Stopwatch.createStarted();
        pool.execute();
        long elapsedMillis = stopwatch.elapsed(TimeUnit.MILLISECONDS);
        long expectedMillis = taskCount * durationMillis / threadCount;
        String msg = "expectedMillis="+expectedMillis+", elapsedMillis="+elapsedMillis;
        assertTrue(msg, elapsedMillis > 0.9*expectedMillis);
        assertTrue(msg, elapsedMillis < 1.1*expectedMillis);  // This sometimes fails, don't know why, just run it again then.
    }
    
    public void testException() {
        ThreadPool pool = new ThreadPool();
        pool.addTask(new Runnable() {
            @Override public void run() {
                throw new RuntimeException("foo");                
            }
        });
        try { 
            pool.execute();
            fail("No exception thrown");
        }
        catch (RuntimeException e) {
            assertEquals("foo", e.getMessage());
        }
    }
}
