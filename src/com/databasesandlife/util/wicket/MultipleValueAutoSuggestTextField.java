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
        textField.add(new AttributeModifier("class", new Model<String>("xyz")));
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
