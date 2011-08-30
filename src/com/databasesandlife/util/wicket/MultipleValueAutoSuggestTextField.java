package com.databasesandlife.util.wicket;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebResource;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.FormComponentPanel;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.ResourceLink;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.StringResourceStream;

import com.google.gson.Gson;

/**
Represents a text-field in Wicket, which allows the user to enter multiple values, and values are suggested from a list of existing values. These values are only suggestions, however, the user may type in other new values, not in the list of existing values. This is ideal for entering tags on a newly created object, where you want the user to use existing tags if appropriate, but also allow the user to create new tags if none of the existing ones are appropriate.
<p>&nbsp;&nbsp;&nbsp;&nbsp;<img src="doc-files/MultipleValueAutoSuggestTextField.png" width=237 height=104></p>
<p>If new values should not be allowed, for example when selecting from a list of existing languages, see {@link MultipleValueAutoCompleteTextField}</a>.</p>
<p>The <strong>model</strong> of the field is an array of Strings which have been chosen. (It is not possible to display strings to the user, while maintaining a list of IDs corresponding to those strings internally, as the user may enter new strings, and those would have no corresponding IDs.)
<p>The list of values to be suggested is a collection of Strings. There are two ways the these may be fetched:</p>
<ul>
        <li><strong>Client-side</strong> - Either all entries are given to the object as a String[], in which case they will be inserted into the HTML page. Suggestions are fast, as a server round-trip is not necessary. This is not practical if there are 1M entries, as the generated HTML will be too large.</li>
        <li><strong>Server-side</strong> - Or a lookup function is provided which can take a substring and can return an ordered String[] of suggestions matching that substring, it is advised to return no more than 10 or 50 entries from this function, otherwise the HTTP communication will be too large. This requires a server round-trip each time the user types a character, so is less responsive.</li>
</ul>
<p>Usage:</p>
<pre>
  &lt;!-- in HTML --&gt;
  &lt;input type="text" wicket:id="tags" class="my-css-class"&gt;&lt;/input&gt;
  
  // In Java
  MultipleValueAutoSuggestTextField tagsField =
      new MultipleValueAutoSuggestTextField("tags");
  tagsField.<b>setClientSideOptions</b>(new String[] { "java", "php" }); <b>// or..</b>
  tagsField.<b>setServerSideDataSource</b>(new AutoSuggestDataSource() {
      public String[] suggest(String userEnteredPartialText) {
          return new String[] { "java", "php" };
      }
  });
  form.add(tagsField);
</pre>
<p>If there is an error about a missing <code>&lt;/span&gt;</code> tag, make sure the <code>&lt;input&gt;</code> tag is closed with a <code>&lt;/input&gt;</code>, even though HTML would not normally require it to be closed. This is a consequence of an implementation issue.
<p>The Javascript used by this software is based on the <a href="http://jqueryui.com/demos/autocomplete/#multiple" target="_blank">JQuery autocomplete multiple example</a>.</p>

 * @author The Java source is copyright <a href="http://www.databasesandlife.com">Adrian Smith</a> and licensed under the LGPL 3.
 * @version $Revision$
 */
public class MultipleValueAutoSuggestTextField extends FormComponentPanel<String[]> {
    
    // Configuration
    protected String[] clientSideOptions = null;
    protected AutoSuggestDataSource serverSideDataSource = null;
    protected String separatorForOutput = ", ";
    protected String separatorCharacterClassRegexp = ",;\\s";
    
    // Data currently in the field
    protected String cssClass;
    protected String text;
    
    // Wicket components
    protected TextField<String> textField;
    
    // Data source
    public interface AutoSuggestDataSource extends Serializable {
        public String[] suggest(String userEnteredPartialText);
    }
    
    // ----------------------------------------------------------------------------------------------------------------
    // Configuring
    // ----------------------------------------------------------------------------------------------------------------
    
    public MultipleValueAutoSuggestTextField(String wicketId) {
        super(wicketId);
        
        add(new JQueryIncluder("jQueryIncluder"));
        
        ResourceLink<?> serverSideDataSourceUrl = new ResourceLink<Object>("serverSideDataSourceUrl", new DataSourceJsonWebResource());
        serverSideDataSourceUrl.add(new AttributeModifier("id", new Model<String>("serverSideDataSourceUrl" + wicketId)));
        add(serverSideDataSourceUrl);
        
        add(new Label("callInitializerJS", new PropertyModel<String>(this, "callInitializerJS")).setEscapeModelStrings(false));
        
        textField = new TextField<String>("text", new PropertyModel<String>(this, "text"));
        textField.add(new AttributeModifier("class", true, new PropertyModel<String>(this, "cssClass")));
        textField.add(new AttributeModifier("id", new Model<String>(wicketId)));
        textField.setConvertEmptyInputStringToNull(false);
        add(textField);
    }

    /** @return this */
    public MultipleValueAutoSuggestTextField setClientSideOptions(String[] options) {
        clientSideOptions = options;
        return this; 
    }
    
    /** @return this */
    public MultipleValueAutoSuggestTextField setServerSideDataSource(AutoSuggestDataSource dataSource) {
        serverSideDataSource = dataSource;
        return this; 
    }
    
    /**
     * When the user types multiple options, they will be separated by, for example, ",".
     * This method sets that separator.
     *    <p>
     * There are two places the separator is used: when generating the text field from a list 
     * of values (<code>separatorForOutput</code>), and when the text field is being parsed into
     * a list of values (<code>separatorCharacterClassRegexp</code>, which is a regular expression).
     * @param separatorForOutput                For example <code>", "</code> - plain text
     * @param separatorCharacterClassRegexp     For example <code>",;\\s"</code> - regular expression
     * @return                                  this
     */
    public MultipleValueAutoSuggestTextField setSeparator(String separatorForOutput, String separatorCharacterClassRegexp) {
        this.separatorForOutput = separatorForOutput;
        this.separatorCharacterClassRegexp = separatorCharacterClassRegexp;
        return this; 
    }
    
    // ----------------------------------------------------------------------------------------------------------------
    // Implementation in Wicket
    // ----------------------------------------------------------------------------------------------------------------
    
    /** Is a wicket web resource; can call the data source and return the results in the JSON format needed by JQuery autocomplete */
    protected class DataSourceJsonWebResource extends WebResource {
        public IResourceStream getResourceStream() {
            try {
                String userEnteredPartialText = getParameters().getString("term");
                String[] results = serverSideDataSource.suggest(userEnteredPartialText);
                String jsonResult = new Gson().toJson(results);
                // Wicket always delivers strings as Latin1, yet client always JSON is always UTF-8 therefore client expects UTF-8
                jsonResult = new String(jsonResult.getBytes("UTF-8"), "ISO-8859-1");
                return new StringResourceStream(jsonResult, "application/json");
            }
            catch (UnsupportedEncodingException e) { throw new RuntimeException(e); }
        }
    }
    
    @Override protected void onComponentTag(ComponentTag tag) {
        super.onComponentTag(tag);
        tag.setName("span");  // So that clients can write <input wicket:id="xx"> and we generate <script>, <input> etc.
        cssClass = tag.getAttribute("class");
    }
    
    /** For example takes <code>abc\def</code> and returns <code>"abc\\def"</code> */
    protected String escapeJsString(String str) {
        str = str.replace("\\", "\\\\"); // \ -> \\
        str = str.replace("\"", "\\\""); // " -> \"
        str = str.replace("'",  "\\'");  // ' -> \'
        return "\"" + str + "\"";
    }
    
    /** Internal method - Do not use */
    public String getCallInitializerJS() {
        StringBuilder optionsJS = new StringBuilder();
        if (clientSideOptions != null) {
            for (String tag : clientSideOptions) {
                if (optionsJS.length() > 0) optionsJS.append(",\n");
                optionsJS.append(escapeJsString(tag));
            }
            optionsJS.insert(0, "[");
            optionsJS.append("]");
        } else {
            optionsJS.append("null");
        }
        
        StringBuilder result = new StringBuilder();  
        result.append("var clientSideOptions" + getId() + " = " + optionsJS + ";\n");
        result.append("var separatorForOutput" + getId() + " = " + escapeJsString(separatorForOutput) + ";\n");
        result.append("var separatorCharacterClassRegexp" + getId() + " = " + escapeJsString(separatorCharacterClassRegexp) + ";\n");
        result.append("autoCompleteTextFieldInit('" + getId() + "', " +
    		"clientSideOptions" + getId() + ", separatorForOutput" + getId() + ", separatorCharacterClassRegexp" + getId() + ");\n");

        return result.toString();
    }

    @Override protected void onBeforeRender() {
        int dataSourceCount = 0;
        if (clientSideOptions != null) dataSourceCount ++;
        if (serverSideDataSource != null) dataSourceCount ++;
        if (dataSourceCount != 1) throw new RuntimeException("Exactly one of clientSideOptions or serverSideDataSource should be set");
        
        String[] currentEntryArray = getModelObject();
        StringBuilder result = new StringBuilder();
        for (String currentEntry : currentEntryArray) {
            if (result.length() > 0) result.append(separatorForOutput);
            result.append(currentEntry);
        }
        text = result.toString();

        super.onBeforeRender();
    }
    
    @Override protected void convertInput() {
        String newEntriesStr = textField.getConvertedInput();
        List<String> newEntryList = new ArrayList<String>();
        Matcher m = Pattern.compile("[^" + separatorCharacterClassRegexp + "]+").matcher(newEntriesStr);
        while (m.find()) newEntryList.add(m.group());
        setConvertedInput(newEntryList.toArray(new String[0]));
    }
}
