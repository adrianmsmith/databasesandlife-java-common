package com.databasesandlife.util;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

/** Forwards all SAX events on to a destination ContentHandler. */
public class IdentityForwardingSaxHandler implements ContentHandler {
    
    protected ContentHandler destinationHandler;
    
    public IdentityForwardingSaxHandler(ContentHandler d) { destinationHandler = d; }

    @Override public void characters(char[] ch, int start, int length) throws SAXException {
        destinationHandler.characters(ch, start, length); }

    @Override public void endDocument() throws SAXException {
        destinationHandler.endDocument(); }

    @Override public void endElement(String uri, String localName, String qName) throws SAXException {
        destinationHandler.endElement(uri, localName, qName); }

    @Override public void endPrefixMapping(String prefix) throws SAXException {
        destinationHandler.endPrefixMapping(prefix); }

    @Override public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
        destinationHandler.ignorableWhitespace(ch, start, length); }

    @Override public void processingInstruction(String target, String data) throws SAXException {
        destinationHandler.processingInstruction(target, data); }

    @Override public void setDocumentLocator(Locator locator) {
        destinationHandler.setDocumentLocator(locator); }

    @Override public void skippedEntity(String name) throws SAXException {
        destinationHandler.skippedEntity(name); }

    @Override public void startDocument() throws SAXException {
        destinationHandler.startDocument(); }

    @Override public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
        destinationHandler.startElement(uri, localName, qName, atts); }

    @Override public void startPrefixMapping(String prefix, String uri) throws SAXException {
        destinationHandler.startPrefixMapping(prefix, uri); }

}
