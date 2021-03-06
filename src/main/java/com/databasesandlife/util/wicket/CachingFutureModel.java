package com.databasesandlife.util.wicket;

import com.databasesandlife.util.Future;
import org.apache.wicket.model.IModel;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.io.Serializable;

/**
 * Can be used simple as a cache of a Future, or also as a Wicket model.
 * <p>
 * Futures have the problem that they are not serializable.
 * Wicket only needs to serialize things after they've been displayed, i.e. after the future has delivered its results.
 * </p>
 */
abstract public class CachingFutureModel<T extends Serializable> implements IModel<T> {

    protected transient @CheckForNull Future<T> future;
    protected @CheckForNull T contents;

    public CachingFutureModel() {
        refresh();
    }

    abstract protected @Nonnull T populate();

    @Override public @Nonnull T getObject() {
        if (future != null) contents = future.get();
        if (contents != null) return contents;
        throw new RuntimeException("Object serialized before future executed");
    }
    
    public void refresh() {
        future = new Future<T>() {
            @Override protected T populate() {
                return CachingFutureModel.this.populate();
            }
        };
    }
}
