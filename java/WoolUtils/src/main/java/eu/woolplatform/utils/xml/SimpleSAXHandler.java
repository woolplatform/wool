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

import java.util.List;

import org.xml.sax.Attributes;

import eu.woolplatform.utils.exception.ParseException;

/**
 * This is a simplified handler for SAX XML parsers. It can be used with a
 * {@link SimpleSAXParser SimpleSAXParser}. The handler receives XML events
 * from a SAX parser and constructs an object of a specified type. If the
 * parser completes without errors, the object can be obtained with {@link
 * #getObject() getObject()}.
 * 
 * @author Dennis Hofs
 */
public interface SimpleSAXHandler<T> {
	/**
	 * Called when the start tag of a new element is found.
	 * 
	 * @param name the name of the element
	 * @param atts the attributes defined in the start tag
	 * @param parents the parents of the new element (ending with the direct
	 * parent)
	 * @throws ParseException if the content is invalid
	 */
	void startElement(String name, Attributes atts,
			List<String> parents) throws ParseException;
	
	/**
	 * Called when the end tag of a new element is found.
	 * 
	 * @param name the name of the element
	 * @param parents the parents of the element (ending with the direct
	 * parent)
	 * @throws ParseException if the content is invalid
	 */
	void endElement(String name, List<String> parents) throws ParseException;
	
	/**
	 * Called when text content is found. This method is called when the text
	 * node is completed, so all consecutive characters are included in one
	 * string. It also includes all whitespace.
	 * 
	 * @param ch the text
	 * @param parents the names of the elements that are parents of the text
	 * node (ending with the direct parent)
	 * @throws ParseException if the content is invalid
	 */
	void characters(String ch, List<String> parents) throws ParseException;
	
	/**
	 * Returns the object that was constructed from the XML code. This method
	 * can be called if the parser completed without errors.
	 * 
	 * @return the object
	 */
	T getObject();
}
