package com.databasesandlife.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import com.google.gdata.util.io.base.UnicodeReader;

/**
 * Parses CSV files.
 *
 * <p>The CSV file is assumed to have a first line containing the column headings.
 * Does not handle quotes in fields (e.g. as generated by Excel).
 * Field names are case-sensitive.
 * Files have a default character set (by default UTF-8) which can be changed by calling {@link #setDefaultCharset},
 * however if the file has a Unicode BOM then this is accepted in preference to the default charset.
 *
 * <h3>Usage</h3>
 * <p>Create an object and set attributes such as the field-separator, list of acceptable columns, etc.
 * Then either call parseAndCallHandler or parseToListOfMaps.</p>
 * 
 * <pre>
 *    CsvLineHandler myHandler = new CsvLineHandler() {
 *        void processCsvLine(Map&lt;String,String> line) { .. }
 *    };
 *    CsvParser csvParser = new CsvParser();
 *    csvParser.setDesiredFields("abc","def"); // field set in file must be this set
 *    csvParser.setNonEmptyFields("abc");      // all of these fields must have non-empty values
 *    csvParser.parseAndCallHandler(myHandler, aFile);
 *    csvParser.parseAndCallHandler(myHandler, aReader);
 *    csvParser.parseAndCallHandler(myHandler, aClass);  // reads "aClass.csv" from classloader
 *    List&lt;Map&lt;String,String>> contents = csvParser.parseToListOfMaps(aFile);</pre>
 * <h3>Glossary</h3>
 * <ul>
 * <li><b>Field</b> - name of column
 * <li><b>Column index</b> - e.g. 0 is the left-most column
 * <li><b>Line</b> - a row of data or header
 * </ul>
 *
 * @author This source is copyright <a href="http://www.databasesandlife.com">Adrian Smith</a> and licensed under the LGPL 3.
 * @version $Revision$
 */
@SuppressWarnings("serial")
public class CsvParser {

    public interface CsvLineHandler {
        /** @param line this object can be re-used between calls to reduce GC; extract values from it but do not store the object anywhere */
        void processCsvLine(Map<String, String> line) throws MalformedCsvException;
    }

    public static class MalformedCsvException extends Exception {  // checked ex. because it's always possible CSV invalid, must handle it
        public MalformedCsvException(String msg) { super(msg); }
        public MalformedCsvException(Throwable e) { super(e); }
    }

    protected class ArrayOfMapsLineHandler implements CsvLineHandler {
        List<Map<String,String>> result = new ArrayList<Map<String,String>>();
        public void processCsvLine(Map<String, String> line) { result.add(new HashMap<String, String>(line)); }
    }

    protected Charset defaultCharset = Charset.forName("UTF-8");
    protected Pattern fieldSeparatorRegexp = Pattern.compile(Pattern.quote(","));
    protected String fieldSeparator = ",(?=([^\"]*\"[^\"]*\")*[^\"]*$)";
    protected Set<String> desiredFields = null;
    protected Set<String> nonEmptyFields = null;
    protected Pattern endOfDataRegex = null;
    protected boolean ignoreNotDesiredColumns = false;
    protected Pattern skipLinePattern = null;

    public void setEndOfDataRegex(Pattern p){ this.endOfDataRegex = p;}
    public void setSkipLinePattern(Pattern p){ this.skipLinePattern = p;}
    public void setDefaultCharset(Charset c) { defaultCharset = c; }
    public void setFieldSeparatorRegexp(Pattern p) { fieldSeparatorRegexp = Pattern.compile(fieldSeparator.replace(",", p.toString()));}
    public void setIgnoreNotDesiredColumns(boolean b){ this.ignoreNotDesiredColumns = b;}
    
    /** Any fields found outside of this list cause an error */ 
    public void setDesiredFields(String... f) { desiredFields = new HashSet<String>(Arrays.asList(f)); }
    
    /** Any fields here must be present and have non-empty values */ 
    public void setNonEmptyFields(String... f) { nonEmptyFields = new HashSet<String>(Arrays.asList(f)); }

    public void parseAndCallHandler(CsvLineHandler lineHandler, BufferedReader r) throws MalformedCsvException {
        try {
            String headerLine = r.readLine();
            if (headerLine == null) throw new MalformedCsvException("File was empty (header line is mandatory)");
            String[] fieldForColIdx = fieldSeparatorRegexp.split(headerLine);
            if (desiredFields != null) {
                for (String desiredField : desiredFields)
                    if ( ! containsField(Arrays.asList(fieldForColIdx),desiredField))
                        throw new MalformedCsvException("Column '" + desiredField + "' is missing");
                if(!ignoreNotDesiredColumns)
                        for (String foundField : fieldForColIdx)
                            if ( ! desiredFields.contains(foundField))
                                throw new MalformedCsvException("Column '" + foundField + "' unexpected");
            }

            int lineNumber = 2;
            Map<String, String> valueForField = new HashMap<String, String>();
            while (true) {
                try {
                    String line = r.readLine();
                    if (line == null || (endOfDataRegex != null && endOfDataRegex.matcher(line).matches())) break; // end of data
                    if(skipLinePattern != null && skipLinePattern.matcher(line).matches()) continue;
                    String[] valueForColIdx = fieldSeparatorRegexp.split(line,-1);
                    if (valueForColIdx.length != fieldForColIdx.length) throw new MalformedCsvException("Expected " +
                        fieldForColIdx.length + " fields but found " + valueForColIdx.length + " fields");
                    valueForField.clear();
                    for (int c = 0; c < valueForColIdx.length; c++) {
                        String field = fieldForColIdx[c].replaceAll("\"", "");
                        String val = valueForColIdx[c].replaceAll("\"", "");
                        if (nonEmptyFields != null && nonEmptyFields.contains(field))
                            if (val.length() == 0) throw new MalformedCsvException("Column " + c + ", field '" + field + "': value may not be empty");
                        valueForField.put(field, val);
                    }
                    lineHandler.processCsvLine(valueForField);

                    lineNumber++;
                }
                catch (MalformedCsvException e) { throw new MalformedCsvException("Line " + lineNumber + ": " + e.getMessage()); }
            }
        }
        catch (IOException e) { throw new RuntimeException(e); }
    }

    public void parseAndCallHandler(CsvLineHandler lineHandler, File f) throws MalformedCsvException {
        try {
            FileInputStream is = new FileInputStream(f);
            try {
                Reader r = new UnicodeReader(is, defaultCharset.name());
                BufferedReader br = new BufferedReader(r);
                parseAndCallHandler(lineHandler, br);
            }
            finally { is.close(); }
        }
        catch (FileNotFoundException e) { throw new MalformedCsvException("CSV file '"+f+"' doesn't exist"); }
        catch (IOException e) { throw new RuntimeException("CSV file '" + f + "': " + e.getMessage(), e); }
        catch (MalformedCsvException e) { throw new MalformedCsvException("CSV file '" + f + "': " + e.getMessage()); }
    }
    
    public void parseAndCallHandler(CsvLineHandler lineHandler, Class<?> cl) throws MalformedCsvException {
        try {
            String name = cl.getName().replaceAll("\\.", "/"); // e.g. "com/offerready/MyClass"
            InputStream csvStream = cl.getClassLoader().getResourceAsStream(name + ".csv");
            if (csvStream == null) throw new IllegalArgumentException("No CSV file for class '" + cl.getName() + "'");
            try { parseAndCallHandler(lineHandler, new BufferedReader(new InputStreamReader(csvStream, defaultCharset))); }
            finally { csvStream.close(); }
        }
        catch (IOException e) { throw new RuntimeException("CSV file for class " + cl + ": " + e.getMessage(), e); }
        catch (MalformedCsvException e) { throw new MalformedCsvException("CSV file for class " + cl + ": " + e.getMessage()); }
    }

    public List<Map<String, String>> parseToListOfMaps(BufferedReader r) throws MalformedCsvException {
        ArrayOfMapsLineHandler lineHandler = new ArrayOfMapsLineHandler();
        parseAndCallHandler(lineHandler, r);
        return lineHandler.result;
    }

    public List<Map<String, String>> parseToListOfMaps(File f) throws MalformedCsvException {
        ArrayOfMapsLineHandler lineHandler = new ArrayOfMapsLineHandler();
        parseAndCallHandler(lineHandler, f);
        return lineHandler.result;
    }

    public List<Map<String, String>> parseToListOfMaps(Class<?> cl) throws MalformedCsvException {
        ArrayOfMapsLineHandler lineHandler = new ArrayOfMapsLineHandler();
        parseAndCallHandler(lineHandler, cl);
        return lineHandler.result;
    }
    
    private boolean containsField(List<String> desired,String field){
        for(String s : desired){
                if(s.replaceAll("\"", "").equals(field)) return true;
        }
        return false;
    }
    
}
