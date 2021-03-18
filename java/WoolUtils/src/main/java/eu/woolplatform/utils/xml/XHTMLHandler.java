package eu.woolplatform.utils.xml;

import eu.woolplatform.utils.exception.ParseException;
import org.xml.sax.Attributes;

import java.util.List;

public class XHTMLHandler extends AbstractSimpleSAXHandler<String> {
	private StringBuilder stringContent = new StringBuilder();

	@Override
	public void startElement(String name, Attributes atts, List<String> parents)
			throws ParseException {
		stringContent.append("<");
		stringContent.append(name);
		for (int i = 0; i < atts.getLength(); i++) {
			String attName = atts.getLocalName(i);
			String attValue = atts.getValue(i);
			stringContent.append(" ");
			stringContent.append(attName);
			stringContent.append("=\"");
			stringContent.append(XMLWriter.escapeString(attValue));
			stringContent.append("\"");
		}
		stringContent.append(">");
	}

	@Override
	public void endElement(String name, List<String> parents)
			throws ParseException {
		stringContent.append("</");
		stringContent.append(name);
		stringContent.append(">");
	}

	@Override
	public void characters(String ch, List<String> parents)
			throws ParseException {
		String trimmed = ch.replaceAll("\\s+", " ");
		stringContent.append(XMLWriter.escapeString(trimmed));
	}

	@Override
	public String getObject() {
		return stringContent.toString();
	}
}
