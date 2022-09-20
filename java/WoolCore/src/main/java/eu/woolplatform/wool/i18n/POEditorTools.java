package eu.woolplatform.wool.i18n;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import eu.woolplatform.wool.model.WoolDialogue;
import eu.woolplatform.wool.model.WoolNode;
import eu.woolplatform.wool.parser.WoolParser;
import eu.woolplatform.wool.parser.WoolParserResult;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * POEditorTools is a runnable class that provides a Command-Line-Interface allowing you
 * to conveniently execute different scripts covering different scenarios related to importing
 * from- or exporting to POEditor.
 *
 * @author Harm op den Akker
 * @author Tessa Beinema
 */
public class POEditorTools {

	/**
	 * Takes a Key-Value JSON export from POEditor and generates a set of {@link WoolTranslationFile} objects for
	 * each different dialogue found in the JSON file. The input JSON-file has the following structure:
	 * {
	 *     "dialogue-name Speaker": {
	 *         "term": "translation",
	 *         "term": "translation",
	 *         "term": "translation"
	 *     },
	 *     "dialogue-name _user": {
	 *         "term": "translation",
	 *         ...
	 *     }
	 * }
	 *
	 * @param jsonFile a {@link File} object pointing to a Key-Value-JSON export from POEditor with the format as defined
	 *                 above.
	 * @return a mapping from {@link String}s (dialogue names) to {@link WoolTranslationFile} objects.
	 * @throws IOException in case any error occurs in parsing the JSON from the input file.
	 */
	public Map<String,WoolTranslationFile> generateWoolTranslationFilesFromPOEditorExport(File jsonFile) throws IOException {
		ObjectMapper mapper = new ObjectMapper();

		TypeReference<Map<String, Map<String, String>>> mapType = new TypeReference<>(){};
		Map<String, Map<String,String>> terms = mapper.readValue(jsonFile, mapType);

		Map<String,WoolTranslationFile> woolTranslationFiles = new HashMap<>();

		// Iterate over all contexts available in the JSON body
		for (String contextString : terms.keySet()) {
			String dialogueName = contextString.split(" ")[0];
			String speakerName = contextString.split(" ")[1];

			WoolTranslationFile woolTranslationFile;
			if (woolTranslationFiles.containsKey(dialogueName)) {
				woolTranslationFile = woolTranslationFiles.get(dialogueName);
			} else {
				woolTranslationFile = new WoolTranslationFile(dialogueName);
				woolTranslationFiles.put(dialogueName, woolTranslationFile);
			}

			Map<String, String> termsMap = terms.get(contextString);
			for (String term : termsMap.keySet()) {
				String translation = termsMap.get(term);
				woolTranslationFile.addTerm(speakerName, term, translation);
			}

		}
		return woolTranslationFiles;
	}

	/**
	 * Generates a {@link List} of {@link WoolTranslationTerm}s from a .wool script located at the given {@link File} location.
	 * @param woolScriptFile a {@link File} link to a .wool script.
	 * @return all translatable terms as a {@link List} of {@link WoolTranslationTerm}s
	 * @throws IOException in case of an IO error when reading in the .wool script
	 */
	public List<WoolTranslationTerm> extractTranslationTermsFromWoolScript(File woolScriptFile) throws IOException {
		// Read in the dialogue from the .wool script file
		WoolDialogue dialogue = readDialogueFile(woolScriptFile);

		System.out.println("===== Processing: " + dialogue.getDialogueName() + " with " + dialogue.getNodeCount() + " nodes. =====");

		ArrayList<WoolTranslationTerm> terms = new ArrayList<>();


		for (WoolNode node : dialogue.getNodes()) {
			WoolTranslatableExtractor extractor = new WoolTranslatableExtractor();
			List<WoolSourceTranslatable> translatables = extractor.extractFromBody(
					node.getHeader().getSpeaker(), WoolSourceTranslatable.USER, node.getBody());

			for(WoolSourceTranslatable translatable : translatables) {
				WoolTranslationTerm term = new WoolTranslationTerm(translatable.getTranslatable().toExportFriendlyString(),dialogue.getDialogueName()+" "+translatable.getSpeaker());
				terms.add(term);
			}

		}

		/* -- Tessa's Version
		for (WoolNode node : dialogue.getNodes()) {
			//Extract everything an Agent can say, and remove all conditionals and set statements. (Note: not comments (=TODO).)
			for (WoolNodeBody.Segment segment : node.getBody().getSegments()) {
				String[] output = segment.toString().split("<<.*>>");
				for (String str : output) {
					if (str.length() > 0) {
						WoolTranslationTerm term = new WoolTranslationTerm(str.replace("\n", "").replace("\r",""), dialogue.getDialogueName() + " Agent");
						terms.add(term);
					}
				}
			}

			//Extract everything a user can reply, and ignore continue replies, i.e., buttons without text.
			for (WoolReply reply : node.getBody().getReplies()) {
				if (!(reply.getStatement() == null)) {
					WoolTranslationTerm term = new WoolTranslationTerm(reply.getStatement().toString(), dialogue.getDialogueName() + " _user");
					terms.add(term);
				}
			}
		}*/
		return terms;
	}

	/**
	 * If the given {@code woolScriptFile} is a correct {@link File} pointer to a .wool script, this function will
	 * return a {@link Set} of {@link File}s that contains all the .wool scripts that are linked from the given {@code woolScriptFile} and
	 * recursively from those referenced .wool scripts.
	 * @param allDialogueFiles call this method with an empty set of files, {@code allDialogueFiles} is used to store progressively the encountered
	 *                         .wool script {@link File}s as the method recursively traverses the dialogue tree.
	 * @param woolScriptFile the origin .wool script {@link File} pointer.
	 * @return a {@link Set} of {@link File}s that represent all .wool scripts that are linked through {@code woolScriptFile} (including itself).
	 * @throws IOException in case of a read error for any of the .wool scripts.
	 */
	public Set<File> getCompleteReferencedDialoguesSet(Set<File> allDialogueFiles, File woolScriptFile) throws IOException {
		WoolDialogue woolDialogue = readDialogueFile(woolScriptFile);

		// Include the given root woolScriptFile if not already in the result set
		allDialogueFiles.add(woolScriptFile);

		// Get all dialogues that are referenced from the given woolScriptFile
		Set<String> referencedDialogues = woolDialogue.getDialoguesReferenced();
		for(String referencedDialogue : referencedDialogues) {
			File referencedDialogueFile = new File(woolScriptFile.getParent() + File.separator + referencedDialogue + ".wool");
			if(!allDialogueFiles.contains(referencedDialogueFile)) {
				allDialogueFiles.add(referencedDialogueFile);
				Set<File> additionalDialogueFiles = getCompleteReferencedDialoguesSet(allDialogueFiles, new File(woolScriptFile.getParent()+File.separator+referencedDialogue+".wool"));
				allDialogueFiles.addAll(additionalDialogueFiles);
			}
		}
		return allDialogueFiles;
	}

	/**
	 * Returns a {@link WoolDialogue} object as read in from a wool script identified by the
	 * given {@code fileName}.
	 * @param woolScriptFile the .wool script {@link File} to read
	 * @return a {@link WoolDialogue} object representation of the given .wool script
	 */
	public WoolDialogue readDialogueFile (File woolScriptFile) {
		try(WoolParser parser = new WoolParser(woolScriptFile)) {
			WoolParserResult parserResult = parser.readDialogue();
			return parserResult.getDialogue();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Takes a given {@link List} of {@link WoolTranslationTerm}s and writes them to the given {@code exportFile} in
	 * JSON format.
	 * @param terms the {@link List} of {@link WoolTranslationTerm}s to write to file.
	 * @param exportFile the file to write to.
	 * @throws IOException in case of any write error.
	 */
	public void writeWoolTranslationTermsToJSON (List<WoolTranslationTerm> terms, File exportFile) throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		ObjectWriter writer = mapper.writer(new DefaultPrettyPrinter());
		writer.writeValue(exportFile, terms);
	}

	public static void main(String[] args) {
		POEditorTools tools = new POEditorTools();

		System.out.println("Welcome to the WOOL POEditor Command Line Tool.\n\n"+
				"This command line tool is used for a number of different scenarios for converting WOOL and POEditor file formats.\n"+
				"Since you haven't provided command line arguments, we will take you through an interactive menu to determine your desired scenario and parameters.");

		System.out.println("The following scenarios are currently supported:\n");
		System.out.println("  1. Generate a single POEditor Terms file from a .wool script, including all linked scripts.");
		System.out.println("  2. Generate multiple POEditor Terms files from a .wool script, including all linked scripts.");
		System.out.println("  3. Convert a single POEditor Key-Value JSON export to one or many WOOL Translation JSON files.");

		Scanner userInputScanner = new Scanner(System.in);  // Create a Scanner object
		System.out.print("\nChoose scenario: ");

		String scenario = userInputScanner.nextLine();  // Read user input
		switch(scenario) {
			case "1":
				System.out.println("Please provide the full file path to the starting .wool script.");

				System.out.print("WOOL Script File: ");
				String woolScriptFile = userInputScanner.nextLine();

				Set<File> allDialogues;
				try {
					allDialogues = tools.getCompleteReferencedDialoguesSet(new HashSet<>(),new File(woolScriptFile));
					System.out.println("Found a total of "+allDialogues.size()+" linked dialogue scripts: ");
					for(File dialogueFile : allDialogues) {
						System.out.println(dialogueFile);
					}

				} catch (IOException e) {
					throw new RuntimeException(e);
				}

				// A list for collection all terms from all files
				List<WoolTranslationTerm> allTerms = new ArrayList<>();

				for(File dialogueFile : allDialogues) {
					try {
						List<WoolTranslationTerm> terms = tools.extractTranslationTermsFromWoolScript(dialogueFile);
						allTerms.addAll(terms);
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
				}

				// Write to outputFile
				System.out.println("Where would you like to save the exported terms?");

				System.out.print("POEditor Terms export file: ");
				String poEditorTermsExportFileName = userInputScanner.nextLine();

				try {
					tools.writeWoolTranslationTermsToJSON(allTerms,new File(poEditorTermsExportFileName));
				} catch (
						IOException e) {
					throw new RuntimeException(e);
				}
				break;
			case "2":
				System.out.println("Please provide the full file path to the starting .wool script.");

				System.out.print("WOOL Script File: ");
				woolScriptFile = userInputScanner.nextLine();

				try {
					allDialogues = tools.getCompleteReferencedDialoguesSet(new HashSet<>(),new File(woolScriptFile));
					System.out.println("Found a total of "+allDialogues.size()+" linked dialogue scripts: ");
					for(File dialogueFile : allDialogues) {
						System.out.println(dialogueFile);
					}

				} catch (IOException e) {
					throw new RuntimeException(e);
				}

				System.out.println("Please choose a directory where you would like to store the POEditor Terms files.");
				File outputDirectory = tools.getOutputDirectoryInteractive();

				for(File dialogueFile : allDialogues) {
					try {
						List<WoolTranslationTerm> terms = tools.extractTranslationTermsFromWoolScript(dialogueFile);
						tools.writeWoolTranslationTermsToJSON(terms,new File(outputDirectory+File.separator+dialogueFile.getName()+".json"));
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
				}
				break;

			case "3":
				System.out.println("Please provide the full file path to the POEditor Key-Value-pair export file.");

				System.out.print("POEditor export file: ");
				String poEditorKeyValueFileName = userInputScanner.nextLine();

				File poEditorKeyValueFile = new File(poEditorKeyValueFileName);
				try {
					Map<String, WoolTranslationFile> woolTranslationFiles = tools.generateWoolTranslationFilesFromPOEditorExport(poEditorKeyValueFile);
					System.out.println("Successfully read translations for "+woolTranslationFiles.keySet().size()+" files.");
					System.out.println("Please choose a directory where you would like to store the WOOL Translation files.");
					outputDirectory = tools.getOutputDirectoryInteractive();

					// The output directory should exist at this point...
					for (String s : woolTranslationFiles.keySet()) {
						WoolTranslationFile wtf = woolTranslationFiles.get(s);
						wtf.writeToFile(outputDirectory);
					}
				} catch(IOException e) {
					System.out.println("An error has occurred reading from the given file '"+poEditorKeyValueFile+"'.");
					e.printStackTrace();
					System.exit(1);
				}
				break;
			default:
				System.out.println("Unknown scenario '"+scenario+"', please provide a number from the list provided above.");
				System.exit(1);
		}

		System.out.println("Finished.");
		System.exit(0);
	}

	private File getOutputDirectoryInteractive() {
		Scanner userInputScanner = new Scanner(System.in);
		System.out.print("Output directory: ");
		String outputDirectoryName = userInputScanner.nextLine();

		File outputDirectory = new File(outputDirectoryName);
		if(!outputDirectory.exists()) {
			System.out.println("The given directory '"+outputDirectoryName+"' does not exist, do you want to create it?");
			boolean inputUnderstood = false;
			boolean createDirectory = false;
			while(!inputUnderstood) {
				System.out.print("Create directory? ");
				String createDirectoryConfirm = userInputScanner.nextLine();
				if(createDirectoryConfirm.equals("y") || createDirectoryConfirm.equals("yes")) {
					createDirectory = true;
					inputUnderstood = true;
				} else if(createDirectoryConfirm.equals("n") || createDirectoryConfirm.equals("no")) {
					inputUnderstood = true;
				}
				if(!inputUnderstood) System.out.println("I don't know what you mean by '"+createDirectoryConfirm+"', why don't you try 'y' or 'n'?");
			}
			if(createDirectory) {
				if(outputDirectory.mkdir()) {
					System.out.println("Created directory '" + outputDirectory + "'.");
				} else {
					System.out.println("An error occurred in attempting to create the following directory: '"+outputDirectoryName+"', please try again.");
					System.exit(1);
				}
			} else {
				System.out.println("Please provide a valid output directory.");
				System.exit(0);
			}
		}
		return outputDirectory;
	}
}
