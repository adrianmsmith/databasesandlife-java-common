package com.databasesandlife.util;

import junit.framework.TestCase;

public class TimerTest extends TestCase {

    public void testFormatDurationNanos() {
        assertEquals("1.234 sec",                  Timer.formatDurationNanos(   1234 * 1000000L));
        assertEquals("1 min 1.234 sec",            Timer.formatDurationNanos(  61234 * 1000000L));
        assertEquals("1 hrs 1 min 1.234 sec",      Timer.formatDurationNanos(3661234 * 1000000L));
    }

}
