package com.databasesandlife.util.wicket;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.FormComponentPanel;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;

//Represents a text-field in Wicket which supports auto-complete.
//
//There are two modes of operation:
// -A mode such as Gmail "to" field, or the old Stackoverflow tags field, where you can type in any number of entries, with a separator. 
//  Each time you type the separator and start typing a new entry, suggestions will appear. 
//  You can also type in entries which are not in the suggestions.
// - A mode such as the Facebook "to" field, where you can only select the suggestions presented.
//  Each suggestion appears in a box in the field with a "X" button to the right where that entry can be deleted.
//There are two ways the data for the suggestions may be fetched:
// - Either all entries are given to the object as a String[], in which case they will be inserted into the HTML page. 
//   Suggestions are fast, as a server round-trip is not necessary. 
//   This is not practical if there are 1M entries, as the generated HTML will be too large.
// - Or a lookup function is provided which can take a substring and can return an ordered String[] of suggestions 
//   matching that substring, it is advised to return no more than 10 or 50 entries from this function, 
//   otherwise the HTTP communication will be too large.
//   This requires a server round-trip each time the user types a character, so is less responsive.
//Usage:
//
//<!-- in HTML -->
//<input type="text" wicket:id="to">
//
//// In Java
//AutoCompleteTextField to = new AutoCompleteTextField("to");
//to.setAllowUnknownOptions(true);   // switches from FB to Gmail mode
//to.setClientSideOptions(new String[] { ... });
//to.setServerSideDataSource(..); 
//add(to);

public class MultipleValueAutoSuggestTextField extends FormComponentPanel<String[]> {
    
    // Configuration
    protected String[] clientSideOptions = null;
    protected String separatorForOutput = ", ";
    protected String separatorCharacterClassRegexp = ",;\\s";
    
    // Data currently in the field
    protected String text;
    
    // Wicket components
    protected TextField<String> textField;
    
    // ----------------------------------------------------------------------------------------------------------------
    // Configuring
    // ----------------------------------------------------------------------------------------------------------------
    
    public MultipleValueAutoSuggestTextField(String wicketId) {
        super(wicketId);
        
        add(new Label("callInitializerJS", new PropertyModel<String>(this, "callInitializerJS")).setEscapeModelStrings(false));
        
        textField = new TextField<String>("text", new PropertyModel<String>(this, "text"));
        textField.add(new AttributeModifier("class", new Model<String>("xyz")));
        textField.add(new AttributeModifier("id", new Model<String>(wicketId)));
        textField.setConvertEmptyInputStringToNull(false);
        add(textField);
    }

    /**
     * @return     the AutoCompleteTextField
     */
    public MultipleValueAutoSuggestTextField setClientSideOptions(String[] options) {
        clientSideOptions = options;
        return this; 
    }
    
    /**
     * @param out  For example ", "
     * @param ch   For example ",;\\s"
     * @return     the AutoCompleteTextField
     */
    public MultipleValueAutoSuggestTextField setSeparator(String out, String ch) {
        this.separatorForOutput = out;
        this.separatorCharacterClassRegexp = ch;
        return this; 
    }
    
    // ----------------------------------------------------------------------------------------------------------------
    // Implementation in Wicket
    // ----------------------------------------------------------------------------------------------------------------
    
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
        for (String tag : clientSideOptions) {
            if (optionsJS.length() > 0) optionsJS.append(",\n");
            optionsJS.append(escapeJsString(tag));
        }

        StringBuilder result = new StringBuilder();  
        result.append("var options" + getId() + " = [" + optionsJS + "];\n");
        result.append("var separatorForOutput" + getId() + " = " + escapeJsString(separatorForOutput) + ";\n");
        result.append("var separatorCharacterClassRegexp" + getId() + " = " + escapeJsString(separatorCharacterClassRegexp) + ";\n");
        result.append("autoCompleteTextFieldInit('" + getId() + "', " +
    		"options" + getId() + ", separatorForOutput" + getId() + ", separatorCharacterClassRegexp" + getId() + ");\n");
        
        return result.toString();
    }

    @Override protected void onBeforeRender() {
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
