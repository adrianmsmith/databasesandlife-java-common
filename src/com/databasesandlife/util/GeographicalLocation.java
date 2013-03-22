package com.databasesandlife.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.esotericsoftware.yamlbeans.YamlReader;

//#
//#    Every location has:
//#       name:
//#          en: Austria
//#          ....
//#       children:
//#          123:    # gr node id:
//#             <location>
//#          ...
//#
//#    The file contains the root location, but without the "name" attr.
//#

/**
 * Represents a region / location / country.
 *     <p>
 * The ID of a location is e.g. "ABC" (but in the config file it's numerical, for example "012".)
 * Each location has a parent location.
 * The exception is the "world" location, which has the ID "" and has parent=null.
 */
public class GeographicalLocation implements Serializable {

    protected static GeographicalLocation world;
    protected static Map<String, GeographicalLocation> locationsForId;

    protected String id;
    protected transient GeographicalLocation parent;
    protected transient GeographicalLocation[] children;
    protected transient Map<String, String> displayNameForLanguage;

    // ----------------------------------------------------------------------------------------------------------------
    // Parsing
    // ----------------------------------------------------------------------------------------------------------------

    protected GeographicalLocation() { } // shouldn't be constructed by clients

    protected static GeographicalLocation newLocationForYaml(
        Map<String, GeographicalLocation> locationsForId,
        GeographicalLocation parent,
        String numericalId,
        Map<String,?> locationYaml
    ) {
        Map<String, String> displayNameForLanguage = new HashMap<String, String>();
        for (Entry<String,String> nameYaml : ((Map<String, String>) locationYaml.get("name")).entrySet()) {
            try {
                String language = String.valueOf(nameYaml.getKey());
                displayNameForLanguage.put(language, nameYaml.getValue());
            }
            catch (IllegalArgumentException e) { } // Language in YAML file isn't supported by our system 
        }

        // Alas some regions don't have texts in all system languages. Try English, else use whatever we have, else error
        String defaultName = displayNameForLanguage.get("en");
        if (defaultName == null) defaultName = displayNameForLanguage.values().iterator().next();
        for (String lang : new String[] { "en", "de" })
            if ( ! displayNameForLanguage.containsKey(lang))
                displayNameForLanguage.put(lang, defaultName);

        StringBuffer id = new StringBuffer();
        for (int c = 0; c < numericalId.length(); c++) {
            int digit = Integer.parseInt(numericalId.substring(c, c+1));
            id.append((char) ('A' + digit));
        }

        GeographicalLocation location = new GeographicalLocation();
        location.id = id.toString();
        location.parent = parent;
        location.displayNameForLanguage = displayNameForLanguage;

        locationsForId.put(location.id, location);
        setChildrenFromYaml(locationsForId, location, locationYaml);
        return location;
    }

    protected static void setChildrenFromYaml(
        Map<String, GeographicalLocation> locationsForId,
        GeographicalLocation parent,
        Map<String,?> locationYaml
    ) {
        Map<String,?> childrenYamlForId = (Map<String,?>) locationYaml.get("children");
        List<GeographicalLocation> children = new ArrayList<GeographicalLocation>();
        if (childrenYamlForId != null)   // if no children then "children" YAML node is not present
            for (Entry<String,?> idAndChildYaml : childrenYamlForId.entrySet()) {
                String numericalId = idAndChildYaml.getKey();
                Map<String,?> childYaml = (Map<String,?>) idAndChildYaml.getValue();
                GeographicalLocation c = newLocationForYaml(locationsForId, parent, numericalId, childYaml);
                children.add(c);
            }
        parent.children = children.toArray(new GeographicalLocation[0]);
    }

    protected static Map<String, GeographicalLocation> parseLocationsYaml() {
        Timer.start("parse-movement-locations-yaml");
        try {
            String name = GeographicalLocation.class.getName().replaceAll("\\.", "/"); // e.g. "com/mypkg/MyClass"
            InputStream csvStream = GeographicalLocation.class.getClassLoader().getResourceAsStream(name + ".yaml");
            if (csvStream == null) throw new IllegalArgumentException("No '.yaml' file for class '" + GeographicalLocation.class.getName() + "'");
            try {
                BufferedReader yamlCharacterReader = new BufferedReader(new InputStreamReader(csvStream, "UTF-8"));
                YamlReader yamlParser = new YamlReader(yamlCharacterReader);
                Map<String,?> rootLocationYaml = (Map<String,?>) yamlParser.read();
                Map<String, GeographicalLocation> result = new HashMap<String, GeographicalLocation>();
                world = new GeographicalLocation();
                world.id = "";
                world.parent = null;
                setChildrenFromYaml(result, world, rootLocationYaml);
                return result;
            }
            finally { csvStream.close(); }
        }
        catch (IOException e) { throw new RuntimeException(e); }
        finally { Timer.end("parse-movement-locations-yaml"); }
    }

    /** Can be called multiple times */
    public static synchronized void init() {
        if (locationsForId == null) locationsForId = parseLocationsYaml();
    }

    protected static synchronized void throwIfNotInit() {
        if (locationsForId == null)
            throw new RuntimeException("Location.init() must be called before first usage, e.g. from application startup code");
    }

    // ----------------------------------------------------------------------------------------------------------------
    // Public class API (over all locations)
    // ----------------------------------------------------------------------------------------------------------------

    /** Returns a map with keys such as "europe", "africa" etc. */
    public static Map<String, GeographicalLocation> getContinentMap() {
        throwIfNotInit();
        Map<String, GeographicalLocation> result = new HashMap<String, GeographicalLocation>();
        result.put("europe",   findForId("B"));
        result.put("africa",   findForId("C"));
        result.put("sAmerica", findForId("D"));
        result.put("nAmerica", findForId("E"));
        result.put("asia",     findForId("F"));
        result.put("cAmerica", findForId("G"));
        return result;
    }

    /** Find a location for its ID. If not found, throw RuntimeException */
    public static GeographicalLocation findForId(String id) {
        throwIfNotInit();
        GeographicalLocation result = locationsForId.get(id);
        if (result == null) throw new RuntimeException("Location not found for id='" + id + "'");
        return result;
    }

    /** @param lang two-letter lowercase language, for example "en" */
    protected static void findForNameAppend(
        List<GeographicalLocation> results,
        String lang,
        String namePartLower,
        GeographicalLocation location,
        int levelToGo
    ) {
        for (GeographicalLocation child : location.children) {
            if (levelToGo == 0) {
                String name = child.displayNameForLanguage.get(lang);
                if (name.toLowerCase().contains(namePartLower)) {
                    results.add(child);
                    if (results.size() > 20) return;
                }
            }

            if (levelToGo > 0) {
                findForNameAppend(results, lang, namePartLower, child, levelToGo-1);
                if (results.size() > 20) return;
            }
        }
    }

    /**
     * Matches namePart against all locations.
     * Returns higher-level locations before lower-level (e.g. Vienna before 2nd district).
     * Returns a maximum of 20 results.
     * @param lang two-letter lowercase language, for example "en"
     */
    public static GeographicalLocation[] findForName(String lang, String namePart) {
        Timer.start("Location.findForName name='" + namePart + "'");
        try {
            throwIfNotInit();
            List<GeographicalLocation> result = new ArrayList<GeographicalLocation>(20);
            for (int level = 0; level < 5; level++)
                findForNameAppend(result, lang, namePart.toLowerCase(), world, level);
            return result.toArray(new GeographicalLocation[0]);
        }
        finally { Timer.end("Location.findForName name='" + namePart + "'"); }
    }
    
    // ----------------------------------------------------------------------------------------------------------------
    // Public object API (of a location)
    // ----------------------------------------------------------------------------------------------------------------

    public String getId() { return id; }
    public String getDisplayNameForLanguage(String lang) { return displayNameForLanguage.get(lang);     }
    public String toString() { return id; }

    /**
     * If this is "2nd district vienna", return "Vienna, Austria" (but not "Europe, World" as that's too obvious).
     * @param lang two-letter lowercase language, for example "en"
     * @return null if there is no path, e.g. if the object is "Austria" itself.
     */
    public String getPathToCountry(String lang) {
        List<String> path = new ArrayList<String>();     // 2nd District, VIENNA, AUSTRIA, Europe  (capitals = result)
        GeographicalLocation r = this;
        while (r != world) {
            path.add(r.getDisplayNameForLanguage(lang));
            r = r.parent;
        }
        StringBuffer result = new StringBuffer();
        for (int i = 1; i < path.size()-1; i++) {
            if (result.length() > 0) result.append(", ");
            result.append(path.get(i));
        }
        if (result.length() == 0) return null;
        else return result.toString();
    }
    
    /** Returns if "candidateChildLocation" is underneath / contained by "this". A location is considered to contain itself. */
    public boolean contains(GeographicalLocation candidateChildLocation) {
        while (candidateChildLocation != null) {
            if (candidateChildLocation.id.equals(this.id)) return true;
            candidateChildLocation = candidateChildLocation.parent;
        }
        return false;
    }

    // ----------------------------------------------------------------------------------------------------------------
    // Serialization (we don't want to serialize the whole location tree every time we serialize a location)
    // ----------------------------------------------------------------------------------------------------------------

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();

        throwIfNotInit();

        GeographicalLocation original = locationsForId.get(id);
        parent = original.parent;
        children = original.children;
        displayNameForLanguage = original.displayNameForLanguage;
    }
}