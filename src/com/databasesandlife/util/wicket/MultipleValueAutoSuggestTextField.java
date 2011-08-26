package com.databasesandlife.util.wicket;

import java.io.Serializable;
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
<p>If new values should not be allowed, for example when selecting from a list of existing languages, see <a href="#">MultipleValueAutoCompleteTextField</a>.</p>
<p>The list of existing values to be suggested is, by necessity, a set of Strings. The model of the text field is an array of Strings which have been chosen. (It is not possible to display strings to the user, while maintaining a list of IDs corresponding to those strings internally, as the user may enter new strings, and those would have no corresponding IDs.)
<p>There are two ways the data for the suggestions may be fetched:</p>
<ul>
        <li>Either all entries are given to the object as a String[], in which case they will be inserted into the HTML page. Suggestions are fast, as a server round-trip is not necessary. This is not practical if there are 1M entries, as the generated HTML will be too large.</li>
        <li>Or a lookup function is provided which can take a substring and can return an ordered String[] of suggestions matching that substring, it is advised to return no more than 10 or 50 entries from this function, otherwise the HTTP communication will be too large. This requires a server round-trip each time the user types a character, so is less responsive.</li>
</ul>
<p>Usage:</p>
<pre>
  &lt;!-- in HTML --&gt;
  &lt;input type="text" wicket:id="tags" class="my-css-class"&gt;
  
  // In Java
  MultipleValueAutoSuggestTextFi<wbr>eld tagsField =
      new MultipleValueAutoSuggestTextFi<wbr>eld("tags");
  tagsField.setClientSideOptions(new String[] { "java", "php" }); // or..
  tagsField.setServerSideDataSource(new AutoSuggestDataSource() {
      public String[] suggest(String userEnteredPartialText) {
          return new String[] { "java", "php" };
      }
  });
  form.add(tagsField);
</pre>
The Javascript used by this software is based on the <a href="http://jqueryui.com/demos/autocomplete/#multiple" target="_blank">JQuery autocomplete multiple example</a>.

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
        
        add(new Label("callInitializerJS", new PropertyModel<String>(this, "callInitializerJS")).setEscapeModelStrings(false));
        
        ResourceLink<?> serverSideDataSourceUrl = new ResourceLink<Object>("serverSideDataSourceUrl", new DataSourceJsonWebResource());
        serverSideDataSourceUrl.add(new AttributeModifier("id", new Model<String>("serverSideDataSourceUrl" + wicketId)));
        add(serverSideDataSourceUrl);
        
        textField = new TextField<String>("text", new PropertyModel<String>(this, "text"));
        textField.add(new AttributeModifier("class", new Model<String>("xyz"))); // TODO
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
     * @param separatorForOutput                For example ", "
     * @param separatorCharacterClassRegexp     For example ",;\\s"
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
            String userEnteredPartialText = getParameters().getString("term");
            String[] results = serverSideDataSource.suggest(userEnteredPartialText);
            String jsonResult = new Gson().toJson(results);
            return new StringResourceStream(jsonResult, "application/json");
        }
    }
    
    @Override protected void onComponentTag(ComponentTag tag) {
        super.onComponentTag(tag);
        tag.setName("span");  // So that clients can write <input wicket:id="xx"> and we generate <script>, <input> etc.
    }
    
    /** For example takes <code>abc\def</code> and returns <code>"abc\\def"</code> */
    protected String escapeJsString(String str) {
        str = str.replace("\\", "\\\\"); // \ -> \\
        str = str.replace("\"", "\\\""); // " -> \"
        str = str.replace("'",  "\\'");  // ' -> \'
        return "\"" + str + "\"";
    }
    
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
