package com.databasesandlife.util.wicket;

import org.apache.wicket.Page;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.request.mapper.parameter.PageParameters;

/**
 * Wraps a page class and its parameters into one object.
 *   <p>
 * Wicket should have been shipped with a class like this.
 * Passing two parameters to methods like "setResponsePage"
 * is inelegant; and returning two parameters from a method is impossible.
 *
 * @author This source is copyright <a href="http://www.databasesandlife.com">Adrian Smith</a> and licensed under the LGPL 3.
 * @version $Revision$
 */
public class PageClassAndParameters {
    public final Class<? extends Page> page;
    public final PageParameters params;
    public PageClassAndParameters(Class<? extends Page> c, PageParameters p) { page=c; params=p; }
    public void throwRestartResponseException() { throw new RestartResponseException(page, params); }
}

