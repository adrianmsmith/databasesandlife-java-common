package com.databasesandlife.util;

import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import static java.util.Arrays.asList;

/**
 * @author This source is copyright <a href="http://www.databasesandlife.com">Adrian Smith</a> and licensed under the LGPL 3.
 * @see <a href="https://github.com/adrianmsmith/databasesandlife-java-common">Project on GitHub</a>
 */
public class ThreadPoolTest extends TestCase {

    public void testException() {
        Runnable throwException = new Runnable() {
            @Override public void run() {
                throw new RuntimeException("foo");
            }
        };
        
        {
            ThreadPool pool = new ThreadPool();
            pool.addTask(throwException);
            try { 
                pool.execute();
                fail("No exception thrown");
            }
            catch (RuntimeException e) {
                assertEquals("foo", e.getMessage());
            }
        }

        {
            ThreadPool pool = new ThreadPool();
            pool.addTaskOffPool(throwException);
            try {
                pool.execute();
                fail("No exception thrown");
            }
            catch (RuntimeException e) {
                assertEquals("foo", e.getMessage());
            }
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

                class WriteToBufferTask implements Runnable {
                    String val;
                    WriteToBufferTask(String v) { val=v; }
                    @Override public String toString() { return val; } // For Debugger

                    @Override public void run() {
                        output.append(val);
                        sleep(0.1);
                    }
                }

                List<Runnable> writeATasks = new ArrayList<>();
                writeATasks.add(new WriteToBufferTask("a"));
                writeATasks.add(new WriteToBufferTask("a"));

                // Check that if a dependency task is already done at the point in time that the dependent task is added,
                // that the dependent task still runs (doesn't e.g. deadlock)
                List<Runnable> writeATasksAndFinishedTask = new ArrayList<>(writeATasks);
                writeATasksAndFinishedTask.add(new Runnable() { @Override public void run() { }} );

                // Check that A -> B -> C type multi-level dependencies work
                List<Runnable> writeBTasks = new ArrayList<>();
                writeBTasks.add(new WriteToBufferTask("b"));
                writeBTasks.add(new WriteToBufferTask("b"));

                ThreadPool pool = new ThreadPool();
                pool.setThreadCount(threadCount);
                pool.addTasks(writeATasks);
                for (Runnable b : writeBTasks) pool.addTaskWithDependencies(writeATasksAndFinishedTask, b);
                pool.addTaskWithDependencies(writeBTasks, new WriteToBufferTask("c"));

                pool.execute();

                // Don't use assertXxx as these throw Error not Exception
                if ( ! "aabbc".equals(output.toString())) throw new RuntimeException(output.toString());
            });
        }

        runTests.execute();
    }

    public void testAddTaskOffPool() {
        StringBuffer output = new StringBuffer();

        class SleepThenWrite implements Runnable {
            String val;
            SleepThenWrite(String v) { val=v; }

            @Override public void run() {
                sleep(0.1);
                output.append(val);
            }
        }

        ThreadPool pool = new ThreadPool();
        Runnable[] writeA = IntStream.range(0, 100).mapToObj(n -> new SleepThenWrite("a")).toArray(Runnable[]::new);
        Runnable[] writeB = IntStream.range(0, 100).mapToObj(n -> new SleepThenWrite("b")).toArray(Runnable[]::new);
        Runnable[] writeC = IntStream.range(0, 100).mapToObj(n -> new SleepThenWrite("c")).toArray(Runnable[]::new);
        pool.addTaskOffPool(writeA);
        pool.addTaskWithDependencies(asList(writeA), writeB);
        pool.addTaskWithDependenciesOffPool(asList(writeB), writeC);
        pool.execute();

        StringBuilder expectedResult = new StringBuilder();
        for (int i = 0; i < 100; i++) expectedResult.append("a");
        for (int i = 0; i < 100; i++) expectedResult.append("b");
        for (int i = 0; i < 100; i++) expectedResult.append("c");
        assertEquals(expectedResult.toString(), output.toString());
    }
}
