/*
 * Copyright 2019 Roessingh Research and Development.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 */

package eu.woolplatform.utils.xml;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import eu.woolplatform.utils.exception.ParseException;

/**
 * This class can parse an XML document using a SAX parser. At construction it
 * takes a {@link SimpleSAXHandler SimpleSAXHandler}, which can process the XML
 * events and return an object of a specified type.
 * 
 * @author Dennis Hofs
 * @param <T> the type of object to return after parsing
 */
public class SimpleSAXParser<T> {
	private SimpleSAXHandler<? extends T> simpleHandler;
	
	/**
	 * Constructs a new SAX handler. It will forward calls to the specified
	 * simple handler.
	 * 
	 * @param handler the simple SAX handler
	 */
	public SimpleSAXParser(SimpleSAXHandler<? extends T> handler) {
		this.simpleHandler = handler;
	}
	
	/**
	 * Parses the specified XML code and returns the corresponding object.
	 * 
	 * @param xml the XML code
	 * @return the object corresponding to the XML code
	 * @throws ParseException if a parsing error occurs. If the {@link
	 * SimpleSAXHandler SimpleSAXHandler} throws an exception, then this
	 * method will throw that same exception.
	 * @throws IOException if a reading error occurs
	 */
	public T parse(String xml) throws ParseException, IOException {
		InputSource input = new InputSource(new StringReader(xml));
		return parse(input);
	}
	
	/**
	 * Parses the specified XML file and returns the corresponding object.
	 * 
	 * @param f the XML file
	 * @return the object corresponding to the XML code
	 * @throws ParseException if a parsing error occurs. If the {@link
	 * SimpleSAXHandler SimpleSAXHandler} throws an exception, then this
	 * method will throw that same exception.
	 * @throws IOException if a reading error occurs
	 */
	public T parse(File f) throws ParseException, IOException {
		try (FileInputStream in = new FileInputStream(f)) {
			return parse(new InputSource(in));
		}
	}
	
	/**
	 * Parses the specified XML code and returns the corresponding object.
	 * 
	 * @param input the XML code
	 * @return the object corresponding to the XML code
	 * @throws ParseException if a parsing error occurs. If the {@link
	 * SimpleSAXHandler SimpleSAXHandler} throws an exception, then this
	 * method will throw that same exception.
	 * @throws IOException if a reading error occurs
	 */
	public T parse(InputSource input) throws ParseException, IOException {
		SAXParserFactory factory = SAXParserFactory.newInstance();
		SAXParser parser;
		try {
			parser = factory.newSAXParser();
		} catch (ParserConfigurationException ex) {
			throw new ParseException("Can't configure SAX parser: " +
					ex.getMessage(), ex);
		} catch (SAXException ex) {
			throw new ParseException("Can't create SAX parser: " +
					ex.getMessage(), ex);
		}
		SAXHandler handler = new SAXHandler();
		try {
			parser.parse(input, handler);
		} catch (SAXParseException ex) {
			throw new ParseException(String.format(
					"Parse error at line %s column %s", ex.getLineNumber(),
					ex.getColumnNumber()) + ": " + ex.getMessage(), ex);
		} catch (SAXException ex) {
			throw new ParseException("Parse error: " + ex.getMessage(), ex);
		}
		return simpleHandler.getObject();
	}
	
	/**
	 * This SAX handler forwards events to the "simpleHandler". It can be used
	 * for documents without namespaces (it only forwards qName rather than uri
	 * and localName). Errors and warnings are always thrown as exceptions.
	 * Consecutive characters including all white space are collected into one
	 * string.
	 */
	private class SAXHandler extends DefaultHandler {
		private List<String> elemStack = new ArrayList<>();
		private StringBuffer charBuf = new StringBuffer();
		private Locator locator = null;
		
		@Override
		public void characters(char[] ch, int start, int length)
		throws SAXException {
			charBuf.append(ch, start, length);
		}
	
		@Override
		public void ignorableWhitespace(char[] ch, int start, int length)
		throws SAXException {
			charBuf.append(ch, start, length);
		}
	
		@Override
		public void startElement(String uri, String localName, String qName,
				Attributes attributes) throws SAXException {
			String name = qName;
			if (name == null || name.length() == 0)
				name = localName;
			try {
				if (charBuf.length() > 0) {
					simpleHandler.characters(charBuf.toString(), elemStack);
					charBuf.setLength(0);
				}
				simpleHandler.startElement(name, attributes, elemStack);
				elemStack.add(name);
			} catch (ParseException ex) {
				throw new SAXParseException(ex.getMessage(), locator, ex);
			}
		}
	
		@Override
		public void endElement(String uri, String localName, String qName)
		throws SAXException {
			String name = qName;
			if (name == null || name.length() == 0)
				name = localName;
			try {
				if (charBuf.length() > 0) {
					simpleHandler.characters(charBuf.toString(), elemStack);
					charBuf.setLength(0);
				}
				elemStack.remove(elemStack.size() - 1);
				simpleHandler.endElement(name, elemStack);
			} catch (ParseException ex) {
				throw new SAXParseException(ex.getMessage(), locator, ex);
			}
		}
	
		@Override
		public void error(SAXParseException e) throws SAXException {
			throw e;
		}
	
		@Override
		public void fatalError(SAXParseException e) throws SAXException {
			throw e;
		}
	
		@Override
		public void warning(SAXParseException e) throws SAXException {
			throw e;
		}

		@Override
		public void setDocumentLocator(Locator locator) {
			super.setDocumentLocator(locator);
			this.locator = locator;
		}
	}
}
