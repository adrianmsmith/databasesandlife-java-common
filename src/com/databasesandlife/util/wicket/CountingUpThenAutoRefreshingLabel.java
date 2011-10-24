package com.databasesandlife.util.wicket;

import java.text.DecimalFormat;
import java.text.NumberFormat;

import org.apache.wicket.ajax.AbstractAjaxTimerBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.util.time.Duration;

/**
 * A &lt;span&gt; label which firstly counts up towards its model's value, and secondly auto-refreshes that value. 
 * The component is modeled on the "worldwide members" number which auto-refreshes on the avaaz homepage.
 * 
 * <p>Usage:
 * <pre>
 *    CountingUpThenAutoRefreshingLabel label =
 *       new CountingUpThenAutoRefreshingLabel("value");
 *    label.setModel(new PropertyModel(anObject, "anInteger"));
 *    add(label);
 *
 *    &lt;span class="xyz" wicket:id="value"&gt;
 * </pre>
 * 
 * <p>
 * The first phase of the component lasts two minutes, and the value counts up from zero to the target value. 
 * It "tends" towards the target value, i.e. moves fast at the beginning, then slows down as it approaches its target value.
 * 
 * <p>
 * The second phase, triggered after the first two minutes, simply refreshes the value periodically from the server. 
 * Initially this is refreshed every two seconds, but this refresh interval is gradually slowed down, 
 * so that if N users leave their browsers open for a long time, we will not get N requests every two seconds forever.
 * 
 * <p>
 * Various numerical values above may be altered with:
 * <pre>
 *    label.setCountingUpDurationSeconds(120);
 *    label.setCountingUpRefreshIntervalSeconds(0.1);
 *    label.setTendencyThreshold(0.001);
 *    label.setAutoRefreshingInitialIntervalSeconds(2.0);
 *    label.setAutoRefreshingIntervalMultiplier(1.2);
 * </pre>
 */

public class CountingUpThenAutoRefreshingLabel extends Panel {
    
    protected double countingUpDurationSeconds = 5;
    protected double countingUpRefreshIntervalSeconds = 0.1;
    protected double tendencyThreshold = 0.001;
    protected double autoRefreshingIntervalMultiplier = 1.2;
    
    protected Label label, js;
    protected AbstractAjaxTimerBehavior currentSelfUpdatingBehavior;
    protected double autoRefreshIntervalSeconds = 2.0;
    
    public CountingUpThenAutoRefreshingLabel(String wicketId) {
        super(wicketId);
        
        label = new Label("label", new PropertyModel<String>(this, "labelValue"));
        label.setOutputMarkupId(true);
        add(label);
        
        currentSelfUpdatingBehavior = new AbstractAjaxTimerBehavior(Duration.seconds(10)) {
            protected void onTimer(AjaxRequestTarget target) { autoRefreshCallback(target); };
        };
        add(currentSelfUpdatingBehavior);
        
        js = new Label("js", new PropertyModel<String>(this, "js"));
        js.setEscapeModelStrings(false);
        add(js);

        setOutputMarkupId(true);
    }
    
    /** How long should the initial phase of counting up be? Measured in seconds */
    public void setCountingUpDurationSeconds(double x) { countingUpDurationSeconds = x; } 
    
    /** What should the duration between the updates during the counting up phase be? Measured in seconds. Can be less than one. */
    public void setCountingUpRefreshIntervalSeconds(double x) { countingUpRefreshIntervalSeconds = x; }
    
    /** How close should the counting up phase get to the target value? A value of 0.01 means it gets within 1% of the target value
     * before switching to the second auto-refresh phase. */
    public void setTendencyThreshold(double x) { tendencyThreshold = x; }
    
    /** During the auto-refresh phase, what should the duration be between server-refreshes, initially? */
    public void setAutoRefreshingInitialIntervalSeconds(double x) { autoRefreshIntervalSeconds = x; }
    
    /** During the auto-refresh phase, by what multiplier should the auto-refresh interval durations increase? For example 2.0
     * means they double; so for example the first duration might be 10s, the next 20s, the next 40s, etc. */
    public void setAutoRefreshingIntervalMultiplier(double x) { autoRefreshingIntervalMultiplier = x; }
    
    /** INTERNAL USE */ public String getLabelValue() {
        NumberFormat f = NumberFormat.getInstance(getLocale());
        if (f instanceof DecimalFormat) {
            ((DecimalFormat) f).setGroupingUsed(true);
        }
        int val = (Integer) getDefaultModelObject();
        return f.format(val);
    }
    
    protected String getThousandSeparator() {
        NumberFormat f = NumberFormat.getInstance(getLocale());
        if (f instanceof DecimalFormat)
            return "" + ((DecimalFormat) f).getDecimalFormatSymbols().getGroupingSeparator();
        else
            return "";
    }
    
    /** INTERNAL USE */ public String getJs() {
        // CountingUpThenAutoRefreshLabel_continueCounting(",", document.getElementById("labelWICKETID"), 1000, 1000);
        
        String result = "";
        result += "var countingUpDurationSeconds = " + countingUpDurationSeconds + ";\n";
        result += "var countingUpRefreshIntervalSeconds = " + countingUpRefreshIntervalSeconds + ";\n";
        result += "var tendencyThreshold = " + tendencyThreshold + ";\n";
        result += "var thousandSeparator = \"" + getThousandSeparator() + "\";\n";
        result += "var element = document.getElementById(\"" + label.getMarkupId() + "\");\n";
        result += "var targetValue = " + (Integer) getDefaultModelObject() + ";\n";
        result += "CountingUpThenAutoRefreshLabel_continueCounting(countingUpDurationSeconds, countingUpRefreshIntervalSeconds, tendencyThreshold, " +
        		"thousandSeparator, element, targetValue, targetValue);\n";
        
        return result;
    }
    
    protected void autoRefreshCallback(AjaxRequestTarget target) {
        currentSelfUpdatingBehavior.stop();    // remove(..) doesn't seem to work; although I'm worried this could be a memory leak
        
        currentSelfUpdatingBehavior = new AbstractAjaxTimerBehavior(Duration.seconds(autoRefreshIntervalSeconds)) {
            protected void onTimer(AjaxRequestTarget target) { autoRefreshCallback(target); };
        };
        add(currentSelfUpdatingBehavior);
        
        js.setVisible(false);
        
        target.addComponent(this);
        
        autoRefreshIntervalSeconds *= autoRefreshingIntervalMultiplier;        
    }
}
