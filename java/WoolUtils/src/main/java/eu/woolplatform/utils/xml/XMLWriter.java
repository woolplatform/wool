package eu.woolplatform.utils.xml;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * An XML writer provides convenient methods to produce XML code.
 * 
 * @author Dennis Hofs
 */
public class XMLWriter {
	private String newline;
	private BufferedWriter writer;
	// elemStack contains open elements. The top of the stack is in front.
	private List<String> elemStack = new ArrayList<String>();
	private Position position = Position.END_ELEMENT;
	
	private enum Position {
		START_ELEMENT,
		CHARACTERS,
		END_ELEMENT
	}
	
	/**
	 * Constructs a new XML writer that will write XML code to the specified
	 * output stream in UTF-8 encoding. This constructor will immediately write
	 * the XML prolog.
	 * 
	 * @param out the output stream
	 * @throws IOException if an error occurs while initialising the XML writer
	 */
	public XMLWriter(OutputStream out) throws IOException {
		writer = new BufferedWriter(new OutputStreamWriter(out,
				Charset.forName("UTF-8")));
		init();
	}
	
	/**
	 * Constructs a new XML writer that will write XML code to the specified
	 * writer. If the code is encoded some time, it should be encoded in UTF-8.
	 * This constructor will immediately write the XML prolog.
	 * 
	 * @param writer the writer
	 * @throws IOException if an error occurs while initialising the XML writer
	 */
	public XMLWriter(Writer writer) throws IOException {
		this.writer = new BufferedWriter(writer);
		init();
	}
	
	/**
	 * Sets the variable "newline" and writes the XML prolog.
	 * 
	 * @throws IOException if an error occurs while writing the XML prolog
	 */
	private void init() throws IOException {
		newline = System.getProperty("line.separator");
		writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		writer.write(newline);
	}

	/**
	 * Closes this XML writer.
	 */
	public void close() {
		try {
			writer.close();
		} catch (IOException ex) {}
	}
	
	/**
	 * Writes the start of an element.
	 * 
	 * @param name the element name
	 * @throws IOException if a writing error occurs
	 */
	public void writeStartElement(String name) throws IOException {
		switch (position) {
		case START_ELEMENT:
			writer.write(">");
			writer.write(newline);
			writeIndent(elemStack.size());
			break;
		case CHARACTERS:
			break;
		case END_ELEMENT:
			writer.write(newline);
			writeIndent(elemStack.size());
			break;
		}
		writer.write("<");
		writer.write(name);
		position = Position.START_ELEMENT;
		elemStack.add(0, name);
	}
	
	/**
	 * Writes an attribute of an element. This can only be called after {@link
	 * #writeStartElement(String) writeStartElement()}. Any special characters
	 * in the attribute value will be escaped with XML entities.
	 * 
	 * @param name the attribute name
	 * @param value the attribute value
	 * @throws IOException if the writer is not in the start of an element, or
	 * if a writing error occurs
	 */
	public void writeAttribute(String name, String value) throws IOException {
		if (position != Position.START_ELEMENT) {
			throw new IOException(
					"Attributes are only allowed in the start tag of an " +
					"element");
		}
		writer.write(" ");
		writer.write(name);
		writer.write("=\"");
		writeEscape(value);
		writer.write("\"");
	}
	
	/**
	 * Writes the end of an element. This can only be called if {@link
	 * #writeStartElement(String) writeStartElement()} was called earlier.
	 * Eventually it should be called once for each call to {@link
	 * #writeStartElement(String) writeStartElement()}.
	 * 
	 * @throws IOException if no matching start was found, or if a writing
	 * error occurs
	 */
	public void writeEndElement() throws IOException {
		if (elemStack.size() == 0) {
			throw new IOException(
					"Can't write end of element, because no matching start " +
					"was found");
		}
		String name = elemStack.remove(0);
		switch (position) {
		case START_ELEMENT:
			writer.write(" />");
			break;
		case CHARACTERS:
			writeEndTag(name);
			break;
		case END_ELEMENT:
			writer.write(newline);
			writeIndent(elemStack.size());
			writeEndTag(name);
			break;
		}
		position = Position.END_ELEMENT;
	}
	
	/**
	 * Writes characters. Any special characters in the string will be escaped
	 * with XML entities.
	 * 
	 * @param s the characters
	 * @throws IOException if a writing error occurs
	 */
	public void writeCharacters(String s) throws IOException {
		if (position == Position.START_ELEMENT)
			writer.write(">");
		writeEscape(s);
		position = Position.CHARACTERS;
	}

	/**
	 * Writes a complete XML element. It parses the XML code into an element
	 * and then calls {@link #writeElement(Element) writeElement(Element)}.
	 *
	 * @param xml the XML code for the element
	 * @throws IOException if the XML code is not a valid element or a writing
	 * error occurs
	 */
	public void writeElement(String xml) throws IOException {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder;
		try {
			builder = factory.newDocumentBuilder();
		} catch (ParserConfigurationException ex) {
			throw new IOException("Can't create DocumentBuilder: " +
					ex.getMessage(), ex);
		}
		StringReader stringReader = new StringReader(xml);
		InputSource source = new InputSource(stringReader);
		Document document;
		try {
			document = builder.parse(source);
		} catch (SAXException ex) {
			throw new IOException("Invalid XML: " + ex.getMessage(), ex);
		}
		Element elem = document.getDocumentElement();
		writeElement(elem);
	}

	/**
	 * Writes a complete XML element.
	 *
	 * @param elem the element
	 * @throws IOException if a writing error occurs
	 */
	public void writeElement(Element elem) throws IOException {
		writeStartElement(elem.getNodeName());
		NamedNodeMap atts = elem.getAttributes();
		for (int i = 0; i < atts.getLength(); i++) {
			Node attr = atts.item(i);
			writeAttribute(attr.getNodeName(), attr.getNodeValue());
		}
		NodeList children = elem.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			Node node = children.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				writeElement((Element)node);
			} else if (node.getNodeType() == Node.TEXT_NODE) {
				writeCharacters(node.getNodeValue());
			}
		}
		writeEndElement();
	}
	
	/**
	 * Writes a number of indentations. Each indentation consists of a tab
	 * character.
	 * 
	 * @param n the number of indentations to write
	 * @throws IOException if a writing error occurs
	 */
	private void writeIndent(int n) throws IOException {
		for (int i = 0; i < n; i++) {
			writer.write("\t");
		}
	}
	
	/**
	 * Writes the end tag of an element.
	 * 
	 * @param name the element name
	 * @throws IOException if a writing error occurs
	 */
	private void writeEndTag(String name) throws IOException {
		writer.write("</");
		writer.write(name);
		writer.write(">");
	}

	/**
	 * Writes the specified string. Any special characters will be escaped with
	 * XML entities.
	 * 
	 * @param s the string to write
	 * @throws IOException if a writing error occurs
	 */
	private void writeEscape(String s) throws IOException {
		writer.write(escapeString(s));
	}

	/**
	 * Escapes any special XML characters with XML entities.
	 *
	 * @param s the string
	 * @return the escaped string
	 */
	public static String escapeString(String s) {
		StringBuilder builder = new StringBuilder();
		int start = 0;
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			String repl = null;
			switch (c) {
			case '&':
				repl = "&amp;";
				break;
			case '<':
				repl = "&lt;";
				break;
			case '>':
				repl = "&gt;";
				break;
			case '"':
				repl = "&quot;";
				break;
			case '\'':
				repl = "&apos;";
				break;
			}
			if (repl != null) {
				builder.append(s, start, i);
				builder.append(repl);
				start = i + 1;
			}
		}
		if (start < s.length())
			builder.append(s, start, s.length());
		return builder.toString();
	}
}
