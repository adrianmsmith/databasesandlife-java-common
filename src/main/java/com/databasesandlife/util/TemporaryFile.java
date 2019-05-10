package com.databasesandlife.util;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;

/**
 * Represents a temporary file.
 *   <p>
 * Difference to simply creating a temporary file is that this can be used in a try block and is deleted afterwards.
 *   <p>
 * Usage:
 * <pre>
 *     try (var f = new TemporaryFile("myfile", "jpg")) {
 *         ...
 *     } // deleted here
 * </pre>
 *
 * @author This source is copyright <a href="http://www.databasesandlife.com">Adrian Smith</a> and licensed under the LGPL 3.
 * @see <a href="https://github.com/adrianmsmith/databasesandlife-java-common">Project on GitHub</a>
 */
public class TemporaryFile implements AutoCloseable {
    
    public final @Nonnull File file;
    
    public TemporaryFile(@Nonnull String prefix, @Nonnull String extension) {
        try { file = File.createTempFile(prefix, "."+extension); }
        catch (IOException e) { throw new RuntimeException(e); }
    }
    
    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Override public void close() {
        file.delete();
    }
}
