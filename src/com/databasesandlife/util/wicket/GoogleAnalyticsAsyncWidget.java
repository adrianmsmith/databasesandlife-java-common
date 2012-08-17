package com.databasesandlife.util.wicket;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;

/**
 * Includes Google Analytics tracking into the page.
 *    <p>
 * Which google account is used for the tracking is probably deployment-specific. 
 * This is therefore passed as a parameter to the constructor of this object. 
 * It is advised that this parameter be set in the web.xml file of the application, to allow it to be different on different deployments.
 *    <p>
 * If the account is null or empty, then the analytics tracking code is not inserted into the page. 
 * (Perhaps some installations require the code, and other installations do not.)
 *
 * @author The Java source is copyright <a href="http://www.databasesandlife.com">Adrian Smith</a> and licensed under the LGPL 3.
 * @version $Revision$
 */
public class GoogleAnalyticsAsyncWidget extends Panel {
    
    public GoogleAnalyticsAsyncWidget(String wicketId, String googleAccountId) {
        super(wicketId);
        
        String setAccountJs = "var googleAnalyticsAsyncWidgetGoogleAccount = '" + googleAccountId + "'";
        Component setAccount = new Label("setAccount", setAccountJs).setEscapeModelStrings(false);
        
        WebMarkupContainer javascript = new WebMarkupContainer("javascript");
        javascript.add(setAccount);
        javascript.setVisible(googleAccountId != null && ! googleAccountId.isEmpty());
        
        add(javascript);
    }

}
