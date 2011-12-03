package com.databasesandlife.util.wicket;

import org.apache.wicket.feedback.IFeedbackMessageFilter;
import org.apache.wicket.markup.html.panel.FeedbackPanel;

/** 
 * Same as a FeedbackPanel but the whole component is deleted from the markup in case there are no messages to display. 
 */ 
public class DisappearingFeedbackPanel extends FeedbackPanel {
    public DisappearingFeedbackPanel(String id, IFeedbackMessageFilter filter) {
        super(id, filter);
    }
    
    public boolean isVisible() {
        if ( ! anyMessage()) return false;
        else return super.isVisible();
    }
}

