package com.databasesandlife.util.wicket;

import java.net.URL;

import org.apache.wicket.Page;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.request.mapper.parameter.PageParameters;

public interface UrlFromPageGenerator {

    public URL newAbsoluteUrl(Class<? extends WebPage> pageClass, PageParameters pageParameters);

    /**
     * @param page
     * Do NOT mount this page class in your application's "init" method.
     * This method is to generate a link to a page which has state (where {@link #newAbsoluteUrl(Class, PageParameters)}
     * cannot be used).
     * If this page is "mounted" then when calling the generated URL
     * after a session loss, Wicket will re-create the page (call default constructor).
     * Otherwise it will display "session expired".
     * So if you use this method, because your page has state you need to make sure the page isn't mounted.
     */
    public URL newAbsoluteUrl(Page page);
}
