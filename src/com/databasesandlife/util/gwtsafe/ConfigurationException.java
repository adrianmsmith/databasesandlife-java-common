package com.databasesandlife.util.gwtsafe;

/**
 * Indicates that in some way a configuration file loaded by DomParser is incorrect.
 *
 * @author This source is copyright <a href="http://www.databasesandlife.com">Adrian Smith</a> and licensed under the LGPL 3.
 * @version $Revision$
 */
@SuppressWarnings("serial")
public class ConfigurationException extends Exception {

    public ConfigurationException(String msg) {
        super(msg);
    }

    public ConfigurationException(String prefix, Throwable cause) {
        super((prefix == null ? "" : (prefix + ": ")) + ((cause.getMessage() == null) ? "Internal error" : cause.getMessage()), cause);
    }

    public ConfigurationException(Throwable cause) {
        this(null, cause);
    }
}
