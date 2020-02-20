package nl.rrd.wool.i18n;

import nl.rrd.wool.model.WoolNodeBody;
import nl.rrd.wool.model.command.WoolInputCommand;

import java.util.List;

/**
 * This class models a translatable segment from a WOOL node. It basically
 * consists of plain text, variables and &lt;&lt;input&gt;&gt; commands.
 *
 * <p>The class contains {@link WoolNodeBody.TextSegment TextSegment}s (with
 * plain text and variables) and {@link WoolNodeBody.CommandSegment
 * CommandSegment}s where the command is a {@link WoolInputCommand
 * WoolInputCommand}.</p>
 *
 * <p></p>Instances of this class can be obtained from {@link
 * WoolTranslatableExtractor WoolTranslatableExtractor} or {@link
 * WoolTranslationParser WoolTranslationParser}.</p>
 *
 * @author Dennis Hofs (RRD)
 */
public class WoolTranslatable {
	private WoolNodeBody parent;
	private List<WoolNodeBody.Segment> segments;

	/**
	 * Constructs a new WOOL translatable.
	 *
	 * @param parent the parent (used in {@link WoolTranslator WoolTranslator})
	 * @param segments the segments
	 */
	public WoolTranslatable(WoolNodeBody parent,
			List<WoolNodeBody.Segment> segments) {
		this.parent = parent;
		this.segments = segments;
	}

	/**
	 * Returns the parent (used in {@link WoolTranslator WoolTranslator}).
	 *
	 * @return the parent (used in {@link WoolTranslator WoolTranslator})
	 */
	public WoolNodeBody getParent() {
		return parent;
	}

	/**
	 * Returns the translatable segment.
	 *
	 * @return the translatable segment
	 */
	public List<WoolNodeBody.Segment> getSegments() {
		return segments;
	}

	@Override
	public int hashCode() {
		return toString().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (obj.getClass() != getClass())
			return false;
		WoolTranslatable other = (WoolTranslatable)obj;
		return toString().equals(other.toString());
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		for (WoolNodeBody.Segment segment : segments) {
			builder.append(segment);
		}
		return builder.toString();
	}
}
