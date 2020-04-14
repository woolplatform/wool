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

package eu.woolplatform.utils.csv;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.List;

import eu.woolplatform.utils.exception.ParseException;

public class CsvParser {
	private LineNumberReader reader;
	
	public CsvParser(LineNumberReader reader) {
		this.reader = reader;
	}
	
	public void close() throws IOException {
		reader.close();
	}
	
	public int getLineNumber() {
		return reader.getLineNumber() + 1;
	}
	
	public List<String> readLineColumns() throws ParseException, IOException {
		int lineNum = getLineNumber();
		String line = reader.readLine();
		if (line == null)
			return null;
		List<String> result = new ArrayList<>();
		boolean atColStart = true;
		StringBuilder stringBuilder = null;
		boolean prevQuote = false;
		int start = 0;
		for (int i = 0; i < line.length(); i++) {
			int c = line.charAt(i);
			if (c == '"') {
				if (stringBuilder == null && !atColStart) {
					throw new ParseException(String.format(
							"Found \" after column start (line %s, column %s)",
							lineNum, i + 1));
				}
				atColStart = false;
				if (stringBuilder == null) {
					stringBuilder = new StringBuilder();
					start = i + 1;
				} else if (prevQuote) {
					stringBuilder.append('"');
					prevQuote = false;
					start = i + 1;
				} else {
					prevQuote = true;
					stringBuilder.append(line.substring(start, i));
					start = i;
				}
			} else if (prevQuote) {
				if (c != ',') {
					throw new ParseException(String.format(
							"Found character %s after \" (line %s, column %s)",
							(char)c, lineNum, i + 1));
				}
				addColumn(stringBuilder.toString(), result);
				atColStart = true;
				stringBuilder = null;
				prevQuote = false;
				start = i + 1;
			} else if (stringBuilder == null && c == ',') {
				addColumn(line.substring(start, i), result);
				atColStart = true;
				start = i + 1;
			} else {
				atColStart = false;
			}
		}
		if (prevQuote) {
			addColumn(stringBuilder.toString(), result);
		} else if (atColStart || start < line.length()) {
			addColumn(line.substring(start), result);
		}
		return result;
	}
	
	private void addColumn(String column, List<String> list) {
		column = column.trim();
		if (column.equals("NULL"))
			list.add(null);
		else
			list.add(column);
	}
}
