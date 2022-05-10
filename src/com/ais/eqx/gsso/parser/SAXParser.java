package com.ais.eqx.gsso.parser;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;

import javax.xml.parsers.SAXParserFactory;

import org.w3c.dom.Document;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;


public class SAXParser extends DefaultHandler {

	private String token;
	private String path;

	public Object parse(Document arg0, URL arg1){
		parseXML(arg1);
		return onGetParsedObject();
	}

	public Object parse(Document arg0, Document arg1, URL arg2, URL arg3){
		parseXML(arg2);
		return onGetParsedObject();
	}

	protected Object onGetParsedObject() {
		return null;
	}

	protected Boolean onStartParsingURI(String uri) {
		return true;
	}

	protected Boolean onStartParsingXML(String xml) {
		return true;
	}

	public void parseXML(URI uri) {
		parseXML(uri.toString());
	}

	public void parseXML(URL url) {
		parseXML(url.toString());
	}

	public void parseXML(String uri) {
		if (onStartParsingURI(uri)) {
			SAXParserFactory spf = SAXParserFactory.newInstance();
			try {
				javax.xml.parsers.SAXParser sp = spf.newSAXParser();
				path = "";
				sp.parse(uri, this);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void parseXMLText(String xml) {
		if (onStartParsingXML(xml)) {
			SAXParserFactory spf = SAXParserFactory.newInstance();
			try {
				xml = xml.replaceFirst("<!DOCTYPE[^>]*>", "");
				javax.xml.parsers.SAXParser sp = spf.newSAXParser();
				path = "";
				InputStream in = new ByteArrayInputStream(xml.getBytes("UTF-8"));
				sp.parse(in, this);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		super.characters(ch, start, length);
		String s = new String(ch, start, length);
		token = (token == null) ? s : token + s;
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		super.startElement(uri, localName, qName, attributes);
		token = null;
		path += "/" + qName;
		onElementStarted(path, qName, attributes);
	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		super.endElement(uri, localName, qName);
		token = (token == null) ? "" : token;
		onElementCaptured(path, qName, token);
		token = null;
		path = path.substring(0, path.length() - qName.length() - 1);
	}

	protected void onElementStarted(String path, String qName, Attributes attr) {
	}

	protected void onElementCaptured(String path, String qName, String token) {
	}

	protected int S2I(String s) {
		if (s == null || s.trim().length() == 0)
			return -1;
		return Integer.parseInt(s);
	}

	protected Boolean S2B(String s) {
		if (s == null || s.trim().length() == 0)
			return false;
		return Boolean.parseBoolean(s);
	}
}
