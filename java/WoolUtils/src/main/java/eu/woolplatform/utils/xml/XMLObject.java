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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import eu.woolplatform.utils.exception.ParseException;
import eu.woolplatform.utils.json.JsonObject;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * An XML object contains methods for XML serialisation. It extends {@link
 * JsonObject JsonObject} for a meaningful toString().
 * 
 * @author Dennis Hofs (RRD)
 */
public abstract class XMLObject extends JsonObject {
	
	/**
	 * Writes this object as XML code to the specified file with UTF-8
	 * encoding.
	 * 
	 * @param file the file
	 * @throws IOException if a writing error occurs
	 */
	public void writeXml(File file) throws IOException {
		FileOutputStream output = new FileOutputStream(file);
		try {
			writeXml(output);
		} finally {
			output.close();
		}
	}

	/**
	 * Writes this object as XML code to the specified output stream with UTF-8
	 * encoding.
	 * 
	 * @param output the output stream
	 * @throws IOException if a writing error occurs
	 */
	public void writeXml(OutputStream output) throws IOException {
		writeXml(new OutputStreamWriter(output, "UTF-8"));
	}
	
	/**
	 * Writes this object as XML code to the specified writer, which should use
	 * UTF-8 encoding.
	 * 
	 * @param writer the writer
	 * @throws IOException if a writing error occurs
	 */
	public void writeXml(Writer writer) throws IOException {
		writeXml(new XMLWriter(writer));
	}
	
	/**
	 * Writes this object to the specified XML writer.
	 * 
	 * @param writer the XML writer
	 * @throws IOException if a writing error occurs
	 */
	public abstract void writeXml(XMLWriter writer) throws IOException;
	
	/**
	 * Reads this object from XML code from the specified URL. It assumes UTF-8
	 * encoding.
	 * 
	 * @param url the URL
	 * @throws ParseException if the XML content is invalid
	 * @throws IOException if a reading error occurs
	 */
	public void readXml(URL url) throws ParseException, IOException {
		InputStream input = url.openStream();
		try {
			readXml(input);
		} finally {
			input.close();
		}
	}

	/**
	 * Reads this object from XML code from the specified file. It assumes
	 * UTF-8 encoding.
	 * 
	 * @param file the file
	 * @throws ParseException if the XML content is invalid
	 * @throws IOException if a reading error occurs
	 */
	public void readXml(File file) throws ParseException, IOException {
		FileInputStream input = new FileInputStream(file);
		try {
			readXml(input);
		} finally {
			input.close();
		}
	}
	
	/**
	 * Reads this object from XML code from the specified input stream. It
	 * assumes UTF-8 encoding.
	 * 
	 * @param input the input stream
	 * @throws ParseException if the XML content is invalid
	 * @throws IOException if a reading error occurs
	 */
	public void readXml(InputStream input) throws ParseException, IOException {
		readXml(new InputStreamReader(input, "UTF-8"));
	}
	
	/**
	 * Reads this object from XML code from the specified reader.
	 * 
	 * @param reader the reader
	 * @throws ParseException if the XML content is invalid
	 * @throws IOException if a reading error occurs
	 */
	public void readXml(Reader reader) throws ParseException, IOException {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder;
		try {
			builder = factory.newDocumentBuilder();
		} catch (ParserConfigurationException ex) {
			throw new RuntimeException("Can't create document builder: " +
					ex.getMessage(), ex);
		}
		InputSource source = new InputSource(reader);
		try {
			readXml(builder.parse(source));
		} catch (SAXException ex) {
			throw new ParseException(ex.getMessage(), ex);
		}
	}
	
	/**
	 * Reads this object from the specified XML document.
	 * 
	 * @param doc the XML document
	 * @throws ParseException if the XML content is invalid
	 */
	public void readXml(Document doc) throws ParseException {
		readXml(doc.getDocumentElement());
	}

	/**
	 * Reads this object from the specified XML element.
	 * 
	 * @param elem the XML element
	 * @throws ParseException if the XML content is invalid
	 */
	public abstract void readXml(Element elem) throws ParseException;
}
