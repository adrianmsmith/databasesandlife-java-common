package com.databasesandlife.util.gwtsafe;

/**
 * Indicates that in some way a configuration file loaded by DomParser is incorrect.
 *
 * @author This source is copyright <a href="http://www.databasesandlife.com">Adrian Smith</a> and licensed under the LGPL 3.
 */
@SuppressWarnings("serial")
public class ConfigurationException extends Exception {

    public ConfigurationException(String msg) {
        super(msg);
    }

    public ConfigurationException(String prefix, Throwable cause) {
        super(prefixExceptionMessage(prefix, cause), cause);
    }

    public ConfigurationException(Throwable cause) {
        this(null, cause);
    }
    
    public static String prefixExceptionMessage(String prefix, Throwable cause) {
        return (prefix == null ? "" : (prefix + ": ")) + ((cause.getMessage() == null) ? "Internal error" : cause.getMessage());
    }
}
