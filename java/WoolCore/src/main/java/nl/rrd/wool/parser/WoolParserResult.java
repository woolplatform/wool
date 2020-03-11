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

package nl.rrd.wool.parser;

import nl.rrd.wool.exception.ParseException;
import nl.rrd.wool.model.WoolDialogue;
import java.util.ArrayList;
import java.util.List;

/**
 * A {@link WoolParserResult} object contains the results of parsing a .wool file, including
 * the resulting {@link WoolDialogue} and a list of {@link ParseException}s.
 *
 * @author Dennis Hofs
 * @author Harm op den Akker
 */
public class WoolParserResult {

    private WoolDialogue dialogue;
    private List<ParseException> parseErrors;

    /**
     * Creates an instance of an empty {@link WoolParserResult} object.
     */
    public WoolParserResult() {
        parseErrors = new ArrayList<ParseException>();
    }

    /**
     * Returns the {@link WoolDialogue} that is part of this {@link WoolParserResult}.
     * @return the {@link WoolDialogue} that is part of this {@link WoolParserResult}.
     */
    public WoolDialogue getDialogue() {
        return dialogue;
    }

    /**
     * Returns a {@link List} of {@link ParseException}s that have occurred during
     * the parsing of the .wool file.
     * @return a {@link List} of {@link ParseException}s.
     */
    public List<ParseException> getParseErrors() {
        return parseErrors;
    }

    /**
     * Sets the {@link WoolDialogue} that is part of this {@link WoolParserResult}.
     * @param dialogue the {@link WoolDialogue} that is part of this {@link WoolParserResult}.
     */
    public void setDialogue(WoolDialogue dialogue) {
        this.dialogue = dialogue;
    }
}
