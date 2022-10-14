/*
 * Copyright 2019-2022 WOOL Foundation - Licensed under the MIT License:
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package eu.woolplatform.wool.i18n;

import eu.woolplatform.wool.model.WoolNodeBody;
import eu.woolplatform.wool.model.command.WoolInputCommand;

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
	 * Returns the translatable segments.
	 *
	 * @return the translatable segments
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

	public String toExportFriendlyString() {
		StringBuilder builder = new StringBuilder();
		for (WoolNodeBody.Segment segment : segments) {
			String sourceString = segment.toString();
			builder.append(sourceString.trim());
		}
		return builder.toString();
	}
}
