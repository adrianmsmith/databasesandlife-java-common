package com.databasesandlife.util.wicket;

import org.apache.wicket.Component;
import org.apache.wicket.feedback.FeedbackMessage;
import org.apache.wicket.feedback.IFeedbackMessageFilter;
import org.apache.wicket.markup.html.basic.MultiLineLabel;
import org.apache.wicket.markup.html.panel.FeedbackPanel;

/** 
 * Same as a FeedbackPanel but:
 * <ul>
 * <li>The whole component is deleted from the markup in case there are no messages to display.
 * <li>If newlines are used in error messages, they are displayed as &lt;br&gt; characters in errors 
 * </ul>
 *
 * @author This source is copyright <a href="http://www.databasesandlife.com">Adrian Smith</a> and licensed under the LGPL 3.
 */ 
@SuppressWarnings("serial")
public class DisappearingFeedbackPanel extends FeedbackPanel {
    public DisappearingFeedbackPanel(String id, IFeedbackMessageFilter filter) {
        super(id, filter);
    }
    
    public DisappearingFeedbackPanel(String id) {
        super(id);
    }
    
    public boolean isVisible() {
        if ( ! anyMessage()) return false;
        else return super.isVisible();
    }

    @Override
    protected Component newMessageDisplayComponent(String id, FeedbackMessage message) {
        return new MultiLineLabel(id, message.getMessage().toString());
    }
}

