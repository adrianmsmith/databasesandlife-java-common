package com.databasesandlife.util;

import java.io.File;
import java.io.FilenameFilter;

public class FilenameExtensionFilter implements FilenameFilter {
    
    protected final String extension;
    
    public FilenameExtensionFilter(String extension) { 
        this.extension = extension;
    }

    @Override
    public boolean accept(File dir, String name) {
        return name.toLowerCase().endsWith("."+this.extension.toLowerCase());
    }
}