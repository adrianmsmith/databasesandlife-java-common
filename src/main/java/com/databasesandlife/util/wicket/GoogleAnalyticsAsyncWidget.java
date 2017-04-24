package com.databasesandlife.util.wicket;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;

/**
 * Includes Google Analytics tracking into the page.
 * <p>
 * Which google account is used for the tracking is probably
 * deployment-specific. This is therefore passed as a parameter to the
 * constructor of this object. It is advised that this parameter be set in the
 * web.xml file of the application, to allow it to be different on different
 * deployments.
 * <p>
 * If the account is null or empty, then the analytics tracking code is not
 * inserted into the page. (Perhaps some installations require the code, and
 * other installations do not.)
 *
 * @author This source is copyright
 *         <a href="http://www.databasesandlife.com">Adrian Smith</a> and
 *         licensed under the LGPL 3.
 * @see <a href="https://github.com/adrianmsmith/databasesandlife-java-common">Project on GitHub</a>
 */
@SuppressWarnings("serial")
public class GoogleAnalyticsAsyncWidget extends Panel {

    public GoogleAnalyticsAsyncWidget(String wicketId, String trackingId) {
        this(wicketId, trackingId, true);
    }

    public GoogleAnalyticsAsyncWidget(String wicketId, String trackingId, boolean useTheOldGoogleAnalyticsApi) {
        super(wicketId);

        String setAccountJs = "var googleAnalyticsAsyncWidgetGoogleAccount = '" + trackingId + "';";
        Component setAccount = new Label("setAccount", setAccountJs).setEscapeModelStrings(false);
        add(setAccount);
        WebMarkupContainer oldGoogleAnalyticsBlock = new WebMarkupContainer("javascript");
        oldGoogleAnalyticsBlock
                .setVisible(useTheOldGoogleAnalyticsApi && trackingId != null && !trackingId.isEmpty());
        add(oldGoogleAnalyticsBlock);
        WebMarkupContainer newGoogleAnalyticsBolock = new WebMarkupContainer("new-javascript");
        newGoogleAnalyticsBolock
                .setVisible(!useTheOldGoogleAnalyticsApi && trackingId != null && !trackingId.isEmpty());
        add(newGoogleAnalyticsBolock);
    }

}
