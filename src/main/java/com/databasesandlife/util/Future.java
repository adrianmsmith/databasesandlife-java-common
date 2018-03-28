package com.databasesandlife.util;

import java.util.Iterator;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import javax.annotation.Nonnull;

/**
 * A future is the response of a calculation which is done in the background (in a thread).
 *     <p>
 * A future has the method {@link #get()} which waits for the calculation to complete, and returns the result.
 * The client must create a subclass and implement the method {@link #populate()} which will be run in the thread.
 *     <p>
 * The reason for the creation of this class is the JVM-supplied {@link java.util.concurrent.Future} object seemed too complex.
 *
 * @author This source is copyright <a href="http://www.databasesandlife.com">Adrian Smith</a> and licensed under the LGPL 3.
 * @see <a href="https://github.com/adrianmsmith/databasesandlife-java-common">Project on GitHub</a>
 */
@SuppressWarnings("serial")
public abstract class Future<T> {

    T result = null;
    RuntimeException exception = null;
    Thread thread;
    
    /** Calculate the result and return it. Must not return null. */
    protected abstract T populate();
    
    public static class FutureComputationTimedOutException extends Exception { }

    /** An exception occurred during the population of this future */
    public static class FuturePopulationException extends RuntimeException {
        FuturePopulationException(@Nonnull Throwable t) { super(t); }
    }
    
    @SuppressFBWarnings("SC_START_IN_CTOR")
    public Future() {
        thread = new Thread(new Runnable() {
            @Override public void run() { 
                try { 
                    T localResult = populate();
                    synchronized (Future.this) { result = localResult; }
                }
                catch (RuntimeException e) {
                    synchronized (Future.this) { exception = e; } 
                }
            }
        }, getThreadName());
        
        thread.start();
    }
    
    protected String getThreadName() {
        return "Future-" + getClass().getSimpleName();
    }
    
    /** Same as {@link #get()} but times out after 'seconds' seconds. */
    public T getOrTimeoutAfterSeconds(float seconds) throws FutureComputationTimedOutException, FuturePopulationException {
        try { thread.join((int) (1000000 * seconds)); }
        catch (InterruptedException e) { throw new RuntimeException(e); }
        
        synchronized (this) {
            if (result == null && exception == null) throw new FutureComputationTimedOutException();

            if (exception != null) throw new FuturePopulationException(exception); // wrap exception to preserve its stack backtrace
            return result;
        }
    }
    
    /** Returns the object, waiting for its computation to be completed if necessary. */
    public T get() {
        try { return getOrTimeoutAfterSeconds(0); }
        catch (FutureComputationTimedOutException e) { throw new RuntimeException("impossible", e); }
    }
    
    /**
     * An iterable whose values are computed in the background.
     * The populate method must return an iterable.
     */
    public abstract static class IterableFuture<I> extends Future<Iterable<I>> implements Iterable<I> {
        @Override public Iterator<I> iterator() { return get().iterator(); }
    }
}
