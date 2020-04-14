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
