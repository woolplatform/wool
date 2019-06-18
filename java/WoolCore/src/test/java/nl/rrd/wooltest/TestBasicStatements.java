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

package nl.rrd.wooltest;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import nl.rrd.wool.exception.ParseException;
import nl.rrd.wool.model.WoolDialogue;
import nl.rrd.wool.parser.WoolParser;

public class TestBasicStatements {
	
	private File file;
	private WoolDialogue woolDialogue;
	
	@Before
	public void setupFile() {
		ClassLoader classLoader = this.getClass().getClassLoader();
		this.file = new File(classLoader.getResource("test-dialogue-basic.wool.txt").getFile());
		assertTrue(file.exists());
	}
	
	@Test
	public void testDialogueCreation() throws ParseException, IOException {
		WoolParser woolParser = new WoolParser(this.file);
		this.woolDialogue = woolParser.readDialogue();
		assertNotNull(woolDialogue);

		/**for (int i = 0; i < woolDialogue.getNodes().size(); i++) {
			WoolNode node = woolDialogue.getNodes().get(i);
			assertThat(node, not(null));
		}*/
	}
}
