package com.databasesandlife.util;

import junit.framework.TestCase;

/**
 * @author This source is copyright <a href="http://www.databasesandlife.com">Adrian Smith</a> and licensed under the LGPL 3.
 * @see <a href="https://github.com/adrianmsmith/databasesandlife-java-common">Project on GitHub</a>
 */
public class ThreadPoolTest extends TestCase {

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
