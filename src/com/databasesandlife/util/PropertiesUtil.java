package com.databasesandlife.util;

import java.util.*;

/**
 * Utility class for manipulating Properties objects, as if they were
 * hierarchical objects. Lists can be created with .0. .1., etc, and key/value
 * pairs can also be constructed.
 *
 * @author This source is copyright <a href="http://www.databasesandlife.com">Adrian Smith</a> and licensed under the LGPL 3.
 * @see <a href="https://github.com/adrianmsmith/databasesandlife-java-common">Project on GitHub</a>
 */

public class PropertiesUtil {

    protected static Hashtable<String, Properties> propertiesForResouceName = new Hashtable<>();

    /** Same as splitToArray but returns a Vector.
      * @see #splitToArray
      * @deprecated use splitToArray instead, this provides type-safety */
    public static Vector<Properties> splitToVector(Properties p, String prefix) {
        if ( ! prefix.equals("")) prefix += ".";
        Vector<Properties> propertiesForIndex = new Vector<>();
        for (Enumeration<?> e = p.keys(); e.hasMoreElements();  ) {
            String key = (String) e.nextElement();
            if (! key.startsWith(prefix)) continue; // wrong prefix
            String indexAndSubKey = key.substring(prefix.length());
            int dotAfterIndex = indexAndSubKey.indexOf('.');
            if (dotAfterIndex == -1) continue; // seems to have no "."
            String indexStr = indexAndSubKey.substring(0, dotAfterIndex);
            int index = Integer.parseInt(indexStr);
            while (index >= propertiesForIndex.size())
                propertiesForIndex.add(new Properties());
            Properties propsThisIndex =
                propertiesForIndex.elementAt(index);
            String subKey = indexAndSubKey.substring(dotAfterIndex+1);
            String value = p.getProperty(key);
            propsThisIndex.setProperty(subKey, value);
        }
        return propertiesForIndex;
    }

    /** Takes a prefix like "rules" and then finds "rules.0.x" etc, puts them
      * into lots of Properties objects with keys like "x" and position 0 in
      * the array. If prefix is "" then finds "0.x" etc.
      * @see #splitToVector
      * @return a Vector of Properties objects */
    public static Properties[] splitToArray(Properties p, String prefix) {
        Vector<Properties> vec = splitToVector(p, prefix);
        return vec.toArray(new Properties[0]);
    }

    /** Takes a prefix like "rules" and then finds "rules.name.key", puts
      * them in a Hashtable of Property objects, where they keys are the
      * strings like "name", and the values are Properties objects, from
      * "key" to value. If prefix is "" then finds "name.key" etc.
      * @return a Hashtable from String objects to Properties objects
      * @deprecated use {@link #splitToMap(Properties,String)}
      */
    public static Hashtable<String, Properties> splitToHashtable(Properties p, String prefix) {
        if ( ! prefix.equals("")) prefix += ".";
        Hashtable<String, Properties> propertiesForName = new Hashtable<>();
        for (Enumeration<Object> e = p.keys(); e.hasMoreElements();  ) {
            String key = (String) e.nextElement();
            if (! key.startsWith(prefix)) continue; // wrong prefix
            String nameAndSubKey = key.substring(prefix.length());
            int dotAfterName = nameAndSubKey.indexOf('.');
            if (dotAfterName == -1) continue; // seems to have no "."
            String name = nameAndSubKey.substring(0, dotAfterName);
            Properties propsThisName = propertiesForName.get(name);
            if (propsThisName == null)
                propertiesForName.put(name, propsThisName = new Properties());
            String subKey = nameAndSubKey.substring(dotAfterName+1);
            String value = p.getProperty(key);
            propsThisName.setProperty(subKey, value);
        }
        return propertiesForName;
    }
    
    /** Takes a prefix like "rules" and then finds "rules.name.key", puts
      * them in a Map. The keys are
      * strings like "name", and the values are Properties objects, from
      * "key" to value. If prefix is "" then finds "name.key" etc.
      */
    public static Map<String, Properties> splitToMap(Properties p, String prefix) {
        return splitToHashtable(p, prefix);
    }

    /** Takes a Properties with keys such as "rules.x" and "y", and a prefix
      * string such as "rules" and returns a new Properties with keys such
      * as "x". */
    public static Properties getSubProperties(Properties p, String prefix) {
        prefix += ".";
        Properties result = new Properties();
        for (Enumeration<Object> e = p.keys(); e.hasMoreElements();  ) {
            String key = (String) e.nextElement();
            if (! key.startsWith(prefix)) continue; // wrong prefix
            String subKey = key.substring(prefix.length());
            result.setProperty(subKey, p.getProperty(key));
        }
        return result;
    }

    /** Determines if getSubProperties would return an empty Properties
      * or not. (But it's faster than calling getSubProperties)
      * @see #getSubProperties */
    public static boolean existsSubProperties(Properties p, String prefix) {
        prefix += ".";
        for (Enumeration<Object> e = p.keys(); e.hasMoreElements();  ) {
            String key = (String) e.nextElement();
            if (key.startsWith(prefix)) return true;
        }
        return false;
    }

    /** Takes a properties and prefixes all entries with prefix and a dot */
    public static Properties prefixAllProperties(Properties p, String prefix) {
        prefix += ".";
        Properties result = new Properties();
        for (Enumeration<Object> e = p.keys(); e.hasMoreElements();  ) {
            String key = (String) e.nextElement();
            result.setProperty(prefix + key, p.getProperty(key));
        }
        return result;
    }

    /** Creates a new Properties which is the combination of all the keys in
      * p1 and p2. If the same key exists in both preoperties, the behaviour
      * is undefined. */
    public static Properties union(Properties p1, Properties p2) {
        Properties result = new Properties();
        for (Enumeration<Object> e1 = p1.keys(); e1.hasMoreElements();  ) {
            String key = (String) e1.nextElement();
            result.setProperty(key, p1.getProperty(key));
        }
        for (Enumeration<Object> e2 = p2.keys(); e2.hasMoreElements();  ) {
            String key = (String) e2.nextElement();
            result.setProperty(key, p2.getProperty(key));
        }
        return result;
    }

    /** Takes a ResourceBundle (describing keys/values) and converts the object
      * into a Properties object. */
    public static Properties newPropertiesForResourceBundle(ResourceBundle b) {
        Properties properties = new Properties();
        for (Enumeration<?> e = b.getKeys(); e.hasMoreElements(); ) {
            String key = (String) e.nextElement(); // ResourceBundle key : String
            String value = b.getString(key);
            properties.setProperty(key, value);
        }
        return properties;
    }

    /**
     * @param resourceBundleName name for example "com.ucpag.components.x" where there
     * is a properties file called "x.properties"
     * in the appropriate directory, and returns a Properties object for that file.
     * @throws MissingResourceException if file cannot be found
     */
    public static Properties newPropertiesForResourceName(String resourceBundleName)
    throws MissingResourceException {
        ResourceBundle configRes = ResourceBundle.getBundle(resourceBundleName);
        return PropertiesUtil.newPropertiesForResourceBundle(configRes);
    }
    
    public static Properties newPropertiesForClass(Class<?> c) {
        return newPropertiesForResourceName(c.getName());
    }
    
    /**
     * @param name name for example "com.ucpag.components.x" where there
     * is a properties file called "x.properties"
     * in the appropriate directory, and returns a Properties object for that file.
     * @throws MissingResourceException if file cannot be found
     */
    public synchronized static Properties getCachedPropertiesForResourceName(String name)
    throws MissingResourceException {
        Properties result = propertiesForResouceName.get(name);
        if (result == null) propertiesForResouceName.put(name, result = newPropertiesForResourceName(name));
        return result;
    }
    
    public static String getPropertyOrThrow(Properties p, String key) {
        String result = p.getProperty(key);
        if (result == null) throw new RuntimeException("Mandatory key '" + key + "' not found");
        return result;
    }
    
    /** @param resourceBundleName name for example "com.databasesandlife.x" where there is a properties file called "x.properties"
      * in the appropriate directory, and returns a Properties object for that file. */
    public static String getPropertyOrThrow(String resourceBundleName, String key) {
        Properties p = getCachedPropertiesForResourceName(resourceBundleName);
        try { return getPropertyOrThrow(p, key); }
        catch (RuntimeException e) { throw new RuntimeException("Exception occured while reading '" +
                resourceBundleName + "' properties file"); }
    }
}
