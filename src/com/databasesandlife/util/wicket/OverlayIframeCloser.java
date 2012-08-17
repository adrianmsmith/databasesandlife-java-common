package com.databasesandlife.util.wicket;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.link.Link;

/** 
 * Breaks out of an iframe JQuery overlay ("colorbox"), and redirects the main browser window to a destination page.
 * 
 * <p>
 * Usage:
 * <pre>
 *     Link<?> linkToResult = new BookmarkablePageLink("link", ...);
 *     setResponsePage(new OverlayIframeCloser(linkToResult));
 * </pre>
 * </p>
 *
 * @author The Java source is copyright <a href="http://www.databasesandlife.com">Adrian Smith</a> and licensed under the LGPL 3.
 * @version $Revision$
 */
public class OverlayIframeCloser extends WebPage {
    
    /** @param destinationPage wicket id of this link should be "link" */
    public OverlayIframeCloser(Link<?> destinationPage) {
        add(destinationPage);
    }
}
