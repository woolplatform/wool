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

package eu.woolplatform.utils.i18n;

import java.text.Collator;
import java.util.Comparator;
import java.util.Locale;

/**
 * This comparator can compare strings using a collator for a specified locale.
 * The collator has primary strength, which means that diacritics and case are
 * ignored.
 * 
 * @author Dennis Hofs (RRD)
 */
public class I18nComparator implements Comparator<String> {
	private Collator collator;

	/**
	 * Constructs a new comparator with the default locale.
	 */
	public I18nComparator() {
		this(null);
	}

	/**
	 * Constructs a new comparator with the specified locale. If the locale is
	 * null, it will use the default locale.
	 * 
	 * @param locale the locale or null
	 */
	public I18nComparator(Locale locale) {
		if (locale == null)
			collator = Collator.getInstance();
		else
			collator = Collator.getInstance(locale);
		collator.setStrength(Collator.PRIMARY);
	}

	@Override
	public int compare(String o1, String o2) {
		return collator.compare(o1, o2);
	}
}
