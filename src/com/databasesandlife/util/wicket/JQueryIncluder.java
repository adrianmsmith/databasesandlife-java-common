package com.databasesandlife.util.wicket;

import org.apache.wicket.markup.html.panel.Panel;

/**
 * Includes JQuery in a wicket page or component.
 *   <p>
 * JQuery doesn't like to get included twice, and if one has two components which both 
 * require JQuery, then they get different URLs (componentA/JQuery.js etc.) and then get
 * included twice. If the components both include the JQueryIncluder then all JQuery.js has
 * the same URL (JQueryIncluder/JQuery.js) and then gets included only once.
 *   <p>
 * Alas, as the JQuery specializations have to be included in the HTML <i>after</i> JQuery.js,
 * and as a wicket panel which includes JQueryIncluder includes its own header contributions
 * before that of its child JQueryIncluder, all the JS of all potentially required JQuery
 * has to be included in this component.
 *
 * @author The Java source is copyright <a href="http://www.databasesandlife.com">Adrian Smith</a> and licensed under the LGPL 3.
 * @version $Revision$
 */
public class JQueryIncluder extends Panel {

    public JQueryIncluder(String wicketId) {
        super(wicketId);
    }
}
