package com.databasesandlife.util.wicket;

import org.apache.wicket.ajax.AbstractAjaxTimerBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.util.time.Duration;

/**
 * The component will get updated, but ever less often.
 *   <p>
 * This means that initially the refresh rate is once a second, so the user will see the data updated quickly.
 * But after a while, it will gradually slow, until it reaches once a minute.
 *   <p>
 * This is a good balance between the user seeing the data update frequently (at the beginning),
 * and not spamming our server if lots of people leave their web browsers open.
 *
 * @author The Java source is copyright <a href="http://www.databasesandlife.com">Adrian Smith</a> and licensed under the LGPL 3.
 * @version $Revision$
 */
@SuppressWarnings("serial")
public abstract class EverSlowingAbstractAjaxTimerBehavior extends AbstractAjaxTimerBehavior {
    
    double durationSeconds = 1.0;
    final double durationMultipler = 1.25;

    public EverSlowingAbstractAjaxTimerBehavior() {
        super(Duration.seconds(1.0));
    }
    
    abstract protected void onEverSlowingTimer(AjaxRequestTarget target);

    @Override
    protected final void onTimer(AjaxRequestTarget target) {
        onEverSlowingTimer(target);
        setUpdateInterval(Duration.seconds(durationSeconds *= durationMultipler));
    }
}
