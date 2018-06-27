package com.databasesandlife.util;

import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

    protected void sleep(double seconds) {
        try { Thread.sleep((long) (1000 * seconds)); }
        catch (InterruptedException e) { throw new RuntimeException(e); }
    }

    public void testAddTaskWithDependencies() {
        ThreadPool runTests = new ThreadPool();
        runTests.setThreadCount(10);

        for (int i = 1; i < 10; i++) {
            int threadCount = i;
            runTests.addTask(() -> {
                StringBuffer output = new StringBuffer();

                List<Runnable> writeATasks = new ArrayList<>();
                writeATasks.add(() -> { output.append("a"); sleep(0.1); });
                writeATasks.add(() -> { output.append("a"); sleep(0.1); });

                // Check that if a dependency task is already done at the point in time that the dependent task is added,
                // that the dependent task still runs (doesn't e.g. deadlock)
                List<Runnable> dependencies = new ArrayList<>(writeATasks);
                dependencies.add(new Runnable() { @Override public void run() { }} );

                ThreadPool pool = new ThreadPool();
                pool.setThreadCount(threadCount);
                pool.addTasks(writeATasks);
                pool.addTaskWithDependencies(dependencies, () -> output.append("b"));
                pool.addTaskWithDependencies(dependencies, () -> output.append("b"));

                pool.execute();

                assertEquals("aabb", output.toString());
            });
        }

        runTests.execute();
    }
}
