package com.databasesandlife.util;

import java.io.File;

/**
 * @author Adrian Smith <adrian.m.smith@gmail.com>
 */
public class RecursiveDirectoryDeleter {

    public static class DeletionException extends Exception {
        public DeletionException(String msg) { super(msg); }
    }
            
    /** @throws RuntimeException if delete fails */
    public static void deleteRecursively(File dir)
    throws DeletionException {
        if ( ! dir.exists()) return;
        
        if (dir.isDirectory()) {
            File[] files = dir.listFiles();
            for(int i = 0; i < files.length; i++) {
                deleteRecursively(files[i]);
            }
        }

        if ( ! dir.delete())
            throw new DeletionException("Cannot delete '" + dir + "' " +
                    "(Java runtime provides no information about why)");
    }
}
