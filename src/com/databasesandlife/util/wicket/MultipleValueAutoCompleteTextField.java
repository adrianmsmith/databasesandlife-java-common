package com.databasesandlife.util.wicket;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

import com.databasesandlife.util.WebEncodingUtils;
import com.google.gson.Gson;

/**
A text-field where the user may enter multiple values, but each value may only be one of a pre-defined list of values. Similar to Facebook's message compose screen, where users may only enter recipients who are in their address book.
<p>&nbsp;&nbsp;&nbsp;&nbsp;<img src="doc-files/MultipleValueAutoCompleteTextField.png" width=346 height=71></p>
<p>If the user should be allowed to enter values which are not in the pre-defined list of values (e.g. the "To:" field in Gmail, where email addresses are suggested, but any email address may be entered), see {@link MultipleValueAutoSuggestTextField}.
<p>The list of values that are allowed may either be <strong>client-side</strong> (meaning they are specified at the time the text field is created, and are placed in the generated HTML) or they may be <strong>server-side</strong> (meaning the application must specify a callback, which is called each time the user starts to enter something, and which must return a list of values matching what the user has entered). The former is appropriate for smaller lists, as it is faster (no server round-trip), the latter for larger lists (as otherwise the generated HTML would be too large.)</p>
<p>Each value may have an internal ID and an external string that the user sees. The <strong>model</strong> of the field is a list of the internal IDs. The data source for the suggestions (either client-side or server-side) is an ordered list of ID and displayable string pairs. IDs are strings (but that doesn't stop them being string representations of numbers, of course).</p>
<p>Usage:</p>
<pre>
  &lt;!-- In HTML --&gt;
  &lt;input type="text" wicket:id="languages"&gt;&lt;/input&gt;
  
  // In Java
  MultipleValueAutoCompleteTextField f =
      new MultipleValueAutoCompleteTextField("languages");
  f.<strong>setClientSideOptions</strong>(new AutoCompleteOption[] {
      new AutoCompleteOption("en", "English"),
      new AutoCompleteOption("de", "German"),
  });  // <b>or...</b>
  f.<strong>setServerSideDataSource</strong>(new AutoCompleteDataSource() {
      AutoCompleteOption[] suggest(String userEnteredText) {
          return ....;
      }
  });
  form.add(f);
</pre>
<p>By default the values (e.g. "English") are plain text strings. If you want to use HTML effects (e.g. have a few &lt;span&gt;s inside the value, with different styles), then subclass AutoCompleteOption and override {@link AutoCompleteOption#getHtmlDisplayText() getHtmlDisplayText}. Due to the way the JS is programmed, use <code>&lt;font class="x"&gt;</code> as opposed to <code>&lt;span class="x"&gt;</code>.</p>
<p>The text field is always on a separate line, this is a consequence of the Javascript library used, and seemed to be the case for all other Javascript libraries I saw as well. The solution, in case you want to have the text field on a line with a field name such as "To:", is either to use absolute positioning, or use tables.</p>
<p>If there is an error about a missing <code>&lt;/span&gt;</code> tag, make sure the <code>&lt;input&gt;</code> tag is closed with a <code>&lt;/input&gt;</code>, even though HTML would not normally require it to be closed. This is a consequence of an implementation issue.
<p>The Javascript software used is <a href="http://loopj.com/jquery-tokeninput/" target="_blank">jQuery Tokeninput</a>.</p>
<h3>Bugs</h3>
<ul>
        <li>Text such as "Loading..." should be localizable</li>
</ul>

 * @author The Java source is copyright <a href="http://www.databasesandlife.com">Adrian Smith</a> and licensed under the LGPL 3.
 * @version $Revision$
 */
public class MultipleValueAutoCompleteTextField extends FormComponentPanel<String[]> {
    
    protected AutoCompleteOption[] clientSideOptions = null;
    protected AutoCompleteDataSource serverSideDataSource = null;
    protected String theme = "facebook";
    protected String[] currentIdsToTextField = new String[0];  // JQuery JS ignores <input> value, this communictes to JS creation
    protected String   currentIdsFromTextField = "";           // JQuery JS puts "12,23" into <input> value, i.e. textfield's model
    protected TextField<String> textField;
    
    // ----------------------------------------------------------------------------------------------------------------
    // Inner classes & interfaces
    // ----------------------------------------------------------------------------------------------------------------
    
    public static class AutoCompleteOption implements Serializable {
        String id, displayText;
        public AutoCompleteOption(String id, String displayText) { this.id=id; this.displayText=displayText; }
        protected AutoCompleteOption() { } // e.g. for subclasses that override getId() and getDisplayText()
        public String getId() { return id; }
        public String getDisplayText() { return displayText; }
        /** If not overridden, this returns HTML encoded version of {@link #getDisplayText()} */
        public String getHtmlDisplayText() { return WebEncodingUtils.encodeHtml(displayText); }
    }
    
    public interface AutoCompleteDataSource extends Serializable {
        public AutoCompleteOption[] suggest(String userEnteredPartialText);
        public AutoCompleteOption getOptionForId(String id);
    }

    // ----------------------------------------------------------------------------------------------------------------
    // Public API
    // ----------------------------------------------------------------------------------------------------------------

    public MultipleValueAutoCompleteTextField(String wicketId) {
        super(wicketId);
        
        add(new JQueryIncluder("jQueryIncluder"));
        
        ResourceLink<?> serverSideDataSourceUrl = new ResourceLink<Object>("serverSideDataSourceUrl", new DataSourceJsonWebResource());
        serverSideDataSourceUrl.add(new AttributeModifier("id", new Model<String>("serverSideDataSourceUrl" + wicketId)));
        add(serverSideDataSourceUrl);
        
        add(new Label("javascript", new PropertyModel<String>(this, "javascript")).setEscapeModelStrings(false));
        
        textField = new TextField<String>("textField", new PropertyModel<String>(this, "currentIdsFromTextField"));
        textField.add(new AttributeModifier("id", new Model<String>(wicketId)));
        textField.setConvertEmptyInputStringToNull(false);
        add(textField);
    }
    
    /** @return this */
    public MultipleValueAutoCompleteTextField setClientSideOptions(AutoCompleteOption[] options) {
        this.clientSideOptions = options;
        return this;
    }

    /** @return this */
    public MultipleValueAutoCompleteTextField setServerSideDataSource(AutoCompleteDataSource dataSource) {
        this.serverSideDataSource = dataSource;
        return this;
    }
    
    /**
     * If the default style is not acceptable, copy "token-input-facebook.css" (from databases and life source) into your
     * CSS directory as "token-input-XXX.css", include it from your HTML, and call <code>setTheme("XXX")</code>.
     * @return this
     */
    public MultipleValueAutoCompleteTextField setTheme(String theme) {
        this.theme = theme;
        return this;
    }

    // ----------------------------------------------------------------------------------------------------------------
    // Implementation / Wicket
    // ----------------------------------------------------------------------------------------------------------------

    protected class DataSourceJsonWebResource extends WebResource {
        public IResourceStream getResourceStream() {
            try {
                String userEnteredPartialText = getParameters().getString("q");
                AutoCompleteOption[] options = serverSideDataSource.suggest(userEnteredPartialText);
                String jsonResult = jsonForOptions(options);
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
    }
    
    protected String jsonForOptions(AutoCompleteOption[] options) {
        List<Object> optionList = new ArrayList<Object>(options.length);
        for (AutoCompleteOption o : options) {
            Map<String, String> optionMap = new HashMap<String, String>();
            optionMap.put("id", o.getId());
            optionMap.put("name", o.getHtmlDisplayText());
            optionList.add(optionMap);
        }
        
        Gson json = new Gson();
        return json.toJson(optionList);
    }
    
    /** Internal method - Do not use */
    public String getJavascript() {
        String source;
        if (clientSideOptions != null) 
            source = jsonForOptions(clientSideOptions);
        else if (serverSideDataSource != null) 
            source = "document.getElementById('serverSideDataSourceUrl" + getId() + "').getAttribute('href')";
        else throw new RuntimeException("Either clientSideOptions or serverSideDataSource must be set for wicket:id='" + getId() + "'");
        
        List<AutoCompleteOption> currentValues = new ArrayList<AutoCompleteOption>(currentIdsToTextField.length);
        for (String id : currentIdsToTextField) {
            if (clientSideOptions != null) 
                for (AutoCompleteOption candidate : clientSideOptions)
                    if (candidate.getId().equals(id))
                        currentValues.add(candidate);
            if (serverSideDataSource != null)
                currentValues.add(serverSideDataSource.getOptionForId(id));
        }
        
        StringBuilder result = new StringBuilder();
        result.append("$(document).ready(function () { \n");
        result.append("  $('#"+getId()+"').tokenInput(" + source + ", {\n");
        result.append("    prePopulate: " + jsonForOptions(currentValues.toArray(new AutoCompleteOption[0])) + ",\n");
        result.append("    theme: '"+theme+"',\n");
        result.append("  });\n");
        result.append("});\n");
        
        return result.toString();
    }

    @Override protected void onBeforeRender() {
        currentIdsToTextField = getModelObject();
        super.onBeforeRender();
    }
    
    @Override protected void convertInput() {
        String newIdsStr = textField.getConvertedInput();
        List<String> newEntryList = new ArrayList<String>();
        Matcher m = Pattern.compile("[^,]+").matcher(newIdsStr);
        while (m.find()) newEntryList.add(m.group());
        setConvertedInput(newEntryList.toArray(new String[0]));
    }
}
