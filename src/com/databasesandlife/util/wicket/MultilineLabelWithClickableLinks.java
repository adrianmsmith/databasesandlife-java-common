package com.databasesandlife.util.wicket;

import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.apache.wicket.util.string.Strings;

/** 
 * Displays the text in the model over multiple HTML lines, and makes links clickable. 
 *
 * @author This source is copyright <a href="http://www.databasesandlife.com">Adrian Smith</a> and licensed under the LGPL 3.
 * @version $Revision$
 */
@SuppressWarnings("serial")
public class MultilineLabelWithClickableLinks extends Label {

    public MultilineLabelWithClickableLinks(final String id, Serializable label) { super(id, label); }
    public MultilineLabelWithClickableLinks(final String id, IModel<?> model) { super(id, model); }

    /**
     * Matches "foo://foo.foo/foo".
     * <ul>
     * <li>Protocol is optional
     * <li>Domain must contain at least one dot (TLD list is too long for whitelist)
     * <li>Last part is optional and can contain anything apart from space and trailing punctuation
     *  (=&nbsp;part of the sentence in which the link is embedded)
     * </ul>
     * <p>Quotes are not allowed because we don't want &lt;a href="foo"&gt; to have foo containing quotes (XSS).
     */
    protected static Pattern linkPattern = Pattern.compile(
        "(\\w{2,7}:/+)?([\\w-]+\\.)+[\\w-]+(/([^\\s'\"]*[\\w])?)?", Pattern.CASE_INSENSITIVE);

    /**
     * Takes some plain text and returns HTML with URLs marked up as clickable links.
     *    
     * <p>Making links clickable is not as easy as it seems.
     * <ul>
     * <li>Conversion from plain text to HTML requires that entities 
     *   such as "&amp;" get replaced by "&amp;amp;".
     * <li>Links such as "foo.com/a&amp;b" need to get replaced by
     *   "&lt;a href='foo.com/a&amp;b'&gt;foo.com/a&amp;amp;b&lt;/a&gt;".
     * </ul>
     * 
     * <p>Therefore,
     * <ul>
     * <li>One cannot firstly replace entities and then markup links, as the links should contain 
     *   unescaped "&amp;" as opposed to "&amp;amp;".
     * <li>One cannot firstly encode links and then replace entities as the angle brackets in the link's
     *   "&lt;a href.." would get replaced by "&amp;lt;a href..." which the 
     *   browser would not understand.
     * </ul>
     * 
     * <p>Therefore, the replacement of HTML entities, and the replacement of links, must be done 
     *   in a single pass, not one after another.
     *   
     * @param plainText Plain text, no HTML.
     * @return Safe HTML with entities encoded and links marked up.
     */
    public static CharSequence encodeLinksToHtml(CharSequence plainText) {
        Matcher m = linkPattern.matcher(plainText);
        StringBuffer result = new StringBuffer();
        while (m.find()) {
            String replacement = "<a target=_blank href='" + m.group() + "'>" + Strings.escapeMarkup(m.group()) + "</a>";
            int backgroundStartInclIdx = result.length();
            m.appendReplacement(result, replacement);
            int backgroundEndExclIdx = result.length() - replacement.length();
            String background = result.substring(backgroundStartInclIdx, backgroundEndExclIdx);
            result.replace(backgroundStartInclIdx, backgroundEndExclIdx, Strings.escapeMarkup(background).toString());
        }
        int backgroundStartInclIdx = result.length();
        m.appendTail(result);
        int backgroundEndExclIdx = result.length();
        String background = result.substring(backgroundStartInclIdx, backgroundEndExclIdx);
        result.replace(backgroundStartInclIdx, backgroundEndExclIdx, Strings.escapeMarkup(background).toString());
        return result;
    }

    @Override public void onComponentTagBody(MarkupStream markupStream, ComponentTag openTag) {
        String plainText = getDefaultModelObjectAsString();
        CharSequence htmlWithLinksMarkedup = encodeLinksToHtml(plainText); 
        CharSequence htmlWIthLinksAndNewlines = Strings.toMultilineMarkup(htmlWithLinksMarkedup);
        replaceComponentTagBody(markupStream, openTag, htmlWIthLinksAndNewlines);
    }
}
