package com.databasesandlife.util;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.util.Collections.emptyList;

/**
 * Runs a number of {@link Runnable} tasks in a number of threads (over a number of CPU cores).
 *    <p>
 * Usage:
 * <pre>
 *      ThreadPool pool = new ThreadPool();
 *
 *      // optional
 *      pool.setThreadNamePrefix("foo"); // for debugger output
 *      pool.setThreadCount(5);
 *
 *      // add one or more seed tasks
 *      pool.addTask(new Runnable() { ... });
 *
 *      // start threads, execute the seed tasks, and execute any tasks they create
 *      pool.execute();
 * </pre>
 *    <p>
 * In the case that any task throws an exception, this exception is thrown by the {@link #execute()} method.
 * If all tasks run to completion, the {@link #execute()} method returns with no value.
 *    <p>
 * Tasks can depend on other tasks. Use the {@link #addTaskWithDependencies(List, Runnable...)} method to add a new task,
 * which will only start in the first parameter after all the tasks in the second parameter have run to completion. All tasks in the List should have been
 * previously added using the normal {@link #addTask(Runnable...)} method, or themselves with {@link #addTaskWithDependencies(List, Runnable...)}.
 *    <p>
 * If task A must be executed before task B, normally task A is added first, and B (with dependency on A) is added afterwards.
 * Therefore, when B is added, if A cannot be found, it is assumed to be already finished and B is scheduled immediately.
 * However, if it is unknown in which order tasks will be added, then A can implement {@link ScheduleDependencyInAnyOrder}.
 *    <p>
 * Tasks can run "off pool". For example, in a thread pool doing CPU-intensive tasks, a long-running HTTP request should not 
 * block the threads from performing their CPU-intensive tasks. An "off pool" task runs in its own thread (not a thread that's
 * a member of the thread pool). The thread may still participate in dependency relationships, that is to say it's possible
 * to schedule a normal task to occur after an "off pool" task has completed. See {@link #addTaskOffPool(Runnable...)} 
 * and {@link #addTaskWithDependenciesOffPool(List, Runnable...)}.
 *    <p>    
 * The difference to an {@link ExecutorService} is:
 * <ul>
 * <li>The processing of tasks can
 * add additional tasks to the queue (whereas to an {@link ExecutorService} the client adds a fixed number of tasks,
 * and they are then executed, without the executing tasks being able to add more tasks).
 * Such functionality is mandatory for web crawlers, which, during the processing of pages, discover links which
 * point to additional pages which require processing.<br><br>
 * <li>It is impossible to forget to "shutdown" a ThreadPool and cause a leakage of threads, as is easily possible with {@link ExecutorService}.
 * If the {@link #execute()} method is never called then no threads are ever started and the object can be garbage collected normally.
 * If the {@link #execute()} method is called then that method makes sure all threads it creates are destroyed.
 * </ul>
 *
 * @author This source is copyright <a href="http://www.databasesandlife.com">Adrian Smith</a> and licensed under the LGPL 3.
 * @see <a href="https://github.com/adrianmsmith/databasesandlife-java-common">Project on GitHub</a>
 */
public class ThreadPool {

    /** A dependency which implements this can be added before or after the task that depends on it */
    public interface ScheduleDependencyInAnyOrder extends Runnable { }

    /** A task that performs no work, but upon which can be waited, and which can be added when some other work is finished. */
    public static class SynchronizationPoint implements ScheduleDependencyInAnyOrder {
        @Override public void run() { }
    }
    
    protected static class TaskWithDependencies {
        boolean offPool;
        @Nonnull Runnable task;
        @Nonnull IdentityHashSet<Runnable> dependencies;
    }
    
    protected @Nonnull String threadNamePrefix = getClass().getSimpleName();
    protected int threadCount = Runtime.getRuntime().availableProcessors();
    protected final IdentityHashSet<Runnable> readyTasks = new IdentityHashSet<>();
    protected final IdentityHashSet<Runnable> executingTasks = new IdentityHashSet<>();
    protected final Map<Runnable, List<TaskWithDependencies>> blockerTasks = new IdentityHashMap<>();
    protected final IdentityHashSet<Runnable> blockedTasks = new IdentityHashSet<>();
    protected final IdentityHashSet<ScheduleDependencyInAnyOrder> doneAnyOrderDependencies = new IdentityHashSet<>();
    protected @CheckForNull Throwable exceptionOrNull = null;
    
    protected synchronized void onTaskCompleted(Runnable task) {
        executingTasks.remove(task);
        
        if (exceptionOrNull != null) return;
        
        if (task instanceof ScheduleDependencyInAnyOrder) 
            doneAnyOrderDependencies.add((ScheduleDependencyInAnyOrder) task);
        
        for (TaskWithDependencies d : blockerTasks.getOrDefault(task, emptyList())) {
            d.dependencies.remove(task);
            if (d.dependencies.isEmpty()) {
                if (d.offPool) {
                    addTaskOffPool(d.task);
                } else {
                    readyTasks.add(d.task);
                }
                blockedTasks.remove(d.task);
            }
        }
        blockerTasks.remove(task);
    }
    
    protected class RunnerRunnable implements Runnable {
        @Override public void run() {
            while (true) {
                final @CheckForNull Runnable nextTaskOrNull;
                synchronized (ThreadPool.this) {
                    if (executingTasks.isEmpty() && readyTasks.isEmpty()) break; // It's finished successfully
                    if (exceptionOrNull != null) break;  // It's failed, no point continuing
                    nextTaskOrNull = readyTasks.isEmpty() ? null : readyTasks.iterator().next();
                    if (nextTaskOrNull != null) { readyTasks.remove(nextTaskOrNull); executingTasks.add(nextTaskOrNull); }
                }
                
                if (nextTaskOrNull != null) {
                    try {
                        nextTaskOrNull.run(); 
                    }
                    // Also catch e.g. StackOverflowExceptions here, 
                    // otherwise ThreadPool.execute appears to "succeed" but stuff that should have happened has not happened.
                    catch (Throwable e) {
                        synchronized (ThreadPool.this) {
                            exceptionOrNull = e;
                        }
                    }
                    finally {
                        onTaskCompleted(nextTaskOrNull);
                    }
                } else {
                    // it might be that other tasks are running, and they will produce lots more tasks
                    // so keep the thread alive and polling until all work is done.
                    try { Thread.sleep(10); }
                    catch (InterruptedException ignored) { }
                }
            }
        }
    }
    
    public void setThreadCount(int count) { threadCount = count; }
    public void setThreadNamePrefix(String prefix) { threadNamePrefix = prefix; }
    
    public synchronized void addTaskWithDependencies(List<? extends Runnable> dependencies, Runnable... after) {
        List<Runnable> stillScheduledDependencies = dependencies.stream()
            .filter(dep -> dep instanceof ScheduleDependencyInAnyOrder ||
                executingTasks.contains(dep) || readyTasks.contains(dep) || blockedTasks.contains(dep))
            .filter(dep -> ! (dep instanceof ScheduleDependencyInAnyOrder && doneAnyOrderDependencies.contains((ScheduleDependencyInAnyOrder) dep)))
            .collect(Collectors.toList());

        if (stillScheduledDependencies.isEmpty()) {
            readyTasks.addAll(after);
        } else {
            for (Runnable job : after) {
                blockedTasks.add(job);

                TaskWithDependencies d = new TaskWithDependencies();
                d.task = job;
                d.dependencies = new IdentityHashSet<>(stillScheduledDependencies);

                for (Runnable dep : stillScheduledDependencies) {
                    blockerTasks.putIfAbsent(dep, new ArrayList<>());
                    blockerTasks.get(dep).add(d);
                }
            }
        }
    }

    public void addTask(Runnable... tasks) {
        for (Runnable r : tasks)
            addTaskWithDependencies(emptyList(), r);
    }

    public void addTasks(Collection<Runnable> tasks) {
        addTask(tasks.toArray(new Runnable[0]));
    }

    /**
     * Add a task which runs in its own thread. 
     * Intended for CPU-bound thread pools which require a task to be completed which does not 
     * consume CPU for example an HTTP request.
     */
    public synchronized void addTaskOffPool(Runnable... tasks) {
        for (Runnable t : tasks) {
            executingTasks.add(t);
            new Thread(() -> {
                try {
                    t.run();
                }
                catch (Exception e) {
                    synchronized (ThreadPool.this) {
                        exceptionOrNull = e;
                    }
                }
                finally {
                    onTaskCompleted(t);
                }
            }).start();
        }
    }

    public synchronized void addTaskWithDependenciesOffPool(List<? extends Runnable> dependencies, Runnable... after) {
        List<Runnable> stillScheduledDependencies = dependencies.stream()
            .filter(dep -> dep instanceof ScheduleDependencyInAnyOrder || 
                executingTasks.contains(dep) || readyTasks.contains(dep) || blockedTasks.contains(dep))
            .filter(dep -> ! (dep instanceof ScheduleDependencyInAnyOrder && doneAnyOrderDependencies.contains((ScheduleDependencyInAnyOrder) dep)))
            .collect(Collectors.toList());

        if (stillScheduledDependencies.isEmpty()) {
            addTaskOffPool(after);
        } else {
            for (Runnable job : after) {
                blockedTasks.add(job);

                TaskWithDependencies d = new TaskWithDependencies();
                d.offPool = true;
                d.task = job;
                d.dependencies = new IdentityHashSet<>(stillScheduledDependencies);

                for (Runnable dep : stillScheduledDependencies) {
                    blockerTasks.putIfAbsent(dep, new ArrayList<>());
                    blockerTasks.get(dep).add(d);
                }
            }
        }
    }

    /** See {@link #unwrapException(RuntimeException, Class)} to how to handle checked exceptions */
    public void execute() {
        List<Thread> threads = IntStream.range(0, threadCount)
            .mapToObj(i -> new Thread(new RunnerRunnable(), threadNamePrefix+"-thread"+i))
            .collect(Collectors.toList());
        for (Thread t : threads) t.start();
        for (Thread t : threads) try { t.join(); } catch (InterruptedException e) { exceptionOrNull = e; }
        if (exceptionOrNull != null) throw new RuntimeException(exceptionOrNull);
    }
    
    /** 
     * After {@link #execute()} runs, use this method, once per checked exception that your Runnables might throw.
     * This can handle the case that you wrap your exception in a RuntimeException, or the case that you use @SneakyThrows.
     * <p>
     * For example:
     * <pre>
     * catch (RuntimeException e) {
     *     unwrapException(e, RequestInvalidException.class);
     *     unwrapException(e, TransformationFailedException.class);
     *     throw e;
     * }
     * </pre>
     * </p>
     */
    @SuppressWarnings("unchecked")
    public static <E extends Exception> void unwrapException(@Nonnull RuntimeException e, @Nonnull Class<E> exceptionClass) throws E {
        // runnable must wrap checked exception, then threads.execute wraps it again
        if (e.getCause() != null
            && e.getCause().getCause() != null
            && exceptionClass.isAssignableFrom(e.getCause().getCause().getClass()))
            throw (E) e.getCause().getCause();

        // @SneakyThrows doesn't wrap, then threads.execute wraps it
        if (e.getCause() != null
            && exceptionClass.isAssignableFrom(e.getCause().getClass()))
            throw (E) e.getCause();
    }
}
