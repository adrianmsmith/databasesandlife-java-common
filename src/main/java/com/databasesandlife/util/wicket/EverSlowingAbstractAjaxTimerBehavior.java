package com.databasesandlife.util.wicket;

import org.apache.log4j.Logger;
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
 * @author This source is copyright <a href="http://www.databasesandlife.com">Adrian Smith</a> and licensed under the LGPL 3.
 * @see <a href="https://github.com/adrianmsmith/databasesandlife-java-common">Project on GitHub</a>
 */
@SuppressWarnings("serial")
public abstract class EverSlowingAbstractAjaxTimerBehavior extends AbstractAjaxTimerBehavior {
    
    double durationSeconds = 1;
    final double durationMultipler = 1.25;
    final double maxSeconds = 60;

    public EverSlowingAbstractAjaxTimerBehavior() {
        super(Duration.seconds(1));
    }
    
    abstract protected void onEverSlowingTimer(AjaxRequestTarget target);

    @Override
    protected final void onTimer(AjaxRequestTarget target) {
        onEverSlowingTimer(target);
        durationSeconds = Math.min(durationSeconds * durationMultipler, maxSeconds);
        Logger.getLogger(getClass()).debug(String.format("%s: duration =%5.1f seconds", getClass().getSimpleName(), durationSeconds));
        setUpdateInterval(Duration.seconds(durationSeconds));
    }
}
