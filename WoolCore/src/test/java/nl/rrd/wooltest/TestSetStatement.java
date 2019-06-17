package nl.rrd.wooltest;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import nl.rrd.wool.exception.ParseException;
import nl.rrd.wool.parser.WoolParser;

public class TestSetStatement {
	
	private File file;
	
	@Before
	public void setupFile() {
		ClassLoader classLoader = this.getClass().getClassLoader();
		this.file = new File(classLoader.getResource("test-dialogue-with-set.txt").getFile());
		assertTrue(file.exists());
	}
	
	@Test
	public void testSetStatement() throws ParseException, IOException {
		WoolParser woolParser = new WoolParser(this.file);
		woolParser.readDialogue();
	}
}
