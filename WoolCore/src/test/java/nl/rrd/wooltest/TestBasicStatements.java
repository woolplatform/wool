package nl.rrd.wooltest;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import nl.rrd.wool.exception.ParseException;
import nl.rrd.wool.exception.WoolInvalidNodeLinkException;
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
	public void testDialogueCreation() throws WoolInvalidNodeLinkException, ParseException, IOException {
		WoolParser woolParser = new WoolParser();
		this.woolDialogue = woolParser.createWoolDialogue(this.file);
		assertNotNull(woolDialogue);

		/**for (int i = 0; i < woolDialogue.getNodes().size(); i++) {
			WoolNode node = woolDialogue.getNodes().get(i);
			assertThat(node, not(null));
		}*/
	}
}
