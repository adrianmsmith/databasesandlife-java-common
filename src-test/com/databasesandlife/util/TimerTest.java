package com.databasesandlife.util;

import junit.framework.TestCase;

/**
 * @author This source is copyright <a href="http://www.databasesandlife.com">Adrian Smith</a> and licensed under the LGPL 3.
 * @see <a href="https://github.com/adrianmsmith/databasesandlife-java-common">Project on GitHub</a>
 */
public class TimerTest extends TestCase {

    public void testFormatDurationNanos() {
        assertEquals("1.234 sec",                  Timer.formatDurationNanos(   1234 * 1000000L));
        assertEquals("1 min 1.234 sec",            Timer.formatDurationNanos(  61234 * 1000000L));
        assertEquals("1 hrs 1 min 1.234 sec",      Timer.formatDurationNanos(3661234 * 1000000L));
    }

}
