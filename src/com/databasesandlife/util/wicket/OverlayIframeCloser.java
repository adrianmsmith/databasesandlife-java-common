package com.databasesandlife.util.wicket;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.link.Link;

/** 
 * Breaks out of an iframe JQuery overlay ("colorbox" or "fancybox"), and redirects the main browser window to a destination page.
 * <p>
 * If the iframe is on the same domain as the parent then specify {@link DomainSimilarity#SameDomain} and the overlay is closed first (it looks nice), 
 * otherwise specify {@link DomainSimilarity#DifferentDomain} and it isn't (as the browser won't allow it due to cross-domain concerns.)
 * <p>
 * Usage:
 * <pre>
 *     Link<?> linkToResult = new BookmarkablePageLink("link", ...);
 *     setResponsePage(new OverlayIframeCloser(
 *       DomainSimilarity.SameDomain, linkToResult)); </pre>
 * </p>
 *
 * @author The Java source is copyright <a href="http://www.databasesandlife.com">Adrian Smith</a> and licensed under the LGPL 3.
 * @version $Revision$
 */
public class OverlayIframeCloser extends WebPage {
    
    public enum DomainSimilarity { SameDomain, DifferentDomain }
    
    /** @param destinationPage wicket id of this link should be "link" */
    public OverlayIframeCloser(DomainSimilarity domainSimilarity, Link<?> destinationPage) {
        add(destinationPage);
        add(new WebMarkupContainer("closeOverlayScript").setVisible(domainSimilarity == DomainSimilarity.SameDomain));
    }
}
