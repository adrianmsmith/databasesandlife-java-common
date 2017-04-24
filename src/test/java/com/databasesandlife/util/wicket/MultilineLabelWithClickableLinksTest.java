package com.databasesandlife.util.wicket;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import junit.framework.TestCase;

/**
 * @author This source is copyright <a href="http://www.databasesandlife.com">Adrian Smith</a> and licensed under the LGPL 3.
 * @see <a href="https://github.com/adrianmsmith/databasesandlife-java-common">Project on GitHub</a>
 */
public class MultilineLabelWithClickableLinksTest extends TestCase {
    
    public void testLinkPattern() {
        Pattern p = MultilineLabelWithClickableLinks.linkPattern;
        Matcher m;
        assertEquals("foo.com", (m = p.matcher("a foo.com. b")).find() ? m.group() : "");
        assertEquals("foo-at-end.com", (m = p.matcher("a foo-at-end.com")).find() ? m.group() : "");
        assertEquals("hyphen-hyphen.com", (m = p.matcher("a hyphen-hyphen.com. b")).find() ? m.group() : "");
        assertEquals("numbers123.com", (m = p.matcher("a numbers123.com. b")).find() ? m.group() : "");
        assertEquals("UPPERCASE.com", (m = p.matcher("a UPPERCASE.com. b")).find() ? m.group() : "");
        assertEquals("foo.foo.com", (m = p.matcher("a foo.foo.com. b")).find() ? m.group() : "");
        assertEquals("foo://foo.com", (m = p.matcher("a foo://foo.com. b")).find() ? m.group() : "");
        assertEquals("foo://foo.com/", (m = p.matcher("a foo://foo.com/. b")).find() ? m.group() : "");
        assertEquals("foo://foo.com/1", (m = p.matcher("a foo://foo.com/1. b")).find() ? m.group() : "");
        assertEquals("foo://foo.com/dot.dot", (m = p.matcher("a foo://foo.com/dot.dot. b")).find() ? m.group() : "");
    }

    public void testEncodeLinksToHtml() {
        String subject = "< foo.com/foo&bar >";
        String expected = "&lt; <a target=_blank href='foo.com/foo&bar'>foo.com/foo&amp;bar</a> &gt;";
        assertEquals(expected, MultilineLabelWithClickableLinks.encodeLinksToHtml(subject).toString());
    }
}
