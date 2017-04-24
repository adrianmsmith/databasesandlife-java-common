package com.databasesandlife.util;

import java.io.File;
import java.io.FilenameFilter;

/**
 * @author This source is copyright <a href="http://www.databasesandlife.com">Adrian Smith</a> and licensed under the LGPL 3.
 * @see <a href="https://github.com/adrianmsmith/databasesandlife-java-common">Project on GitHub</a>
 */
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