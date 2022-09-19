package eu.woolplatform.wool.i18n;

import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;

public class POEditorTools {

	public POEditorTools()  {

	}

	public Map<String,WoolTranslationFile> generateWoolTranslationFiles(File jsonFile) throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		Map<String, Map<String,String>> terms = mapper.readValue(jsonFile, Map.class);

		Map<String,WoolTranslationFile> woolTranslationFiles = new HashMap<>();

		// Iterate over all contexts available in the JSON body
		Iterator<String> contextIterator = terms.keySet().iterator();
		while(contextIterator.hasNext()) {
			String contextString = contextIterator.next();
			String dialogueName = contextString.split(" ")[0];
			String speakerName = contextString.split(" ")[1];

			WoolTranslationFile woolTranslationFile;
			if(woolTranslationFiles.containsKey(dialogueName)) {
				woolTranslationFile = woolTranslationFiles.get(dialogueName);
			} else {
				//System.out.println("POEditorTools: Creating new WOOL Translation File object: '"+dialogueName+"'.");
				woolTranslationFile = new WoolTranslationFile(dialogueName);
				woolTranslationFiles.put(dialogueName,woolTranslationFile);
			}

			Map<String,String> termsMap = terms.get(contextString);
			Iterator<String> termsIterator = termsMap.keySet().iterator();
			while(termsIterator.hasNext()) {
				String term = termsIterator.next();
				String translation = termsMap.get(term);
				//System.out.println("POEditorTools: Adding to file '"+dialogueName+"', for speaker '"+speakerName+"' the following term/translation: ['"+term+"','"+translation+"'].");
				woolTranslationFile.addTerm(speakerName,term,translation);
			}

		}

		return woolTranslationFiles;
	}

	public void writeWoolTranslationFile(File directory, WoolTranslationFile woolTranslationFile) throws IOException {
		// create object mapper instance
		ObjectMapper mapper = new ObjectMapper();

		File jsonFile = new File(directory.getAbsolutePath() + File.separator + woolTranslationFile.getFileName()+".json");

		ObjectWriter writer = mapper.writer(new DefaultPrettyPrinter());

		// convert map to JSON file
		writer.writeValue(jsonFile, woolTranslationFile.getContentMap());
	}

	public static void main(String[] args) {
		POEditorTools tools = new POEditorTools();

		System.out.println("Welcome to the WOOL POEditor Command Line Tool.\n\n"+
				"This command line tool is used for a number of different scenarios for converting WOOL and POEditor file formats.\n"+
				"Since you haven't provided command line arguments, we will take you through an interactive menu to determine your desired scenario and parameters.");

		System.out.println("The following scenarios are currently supported:\n");
		System.out.println("  1. Convert a single POEditor Key-Value JSON export to one or many WOOL Translation JSON files.");

		Scanner userInputScanner = new Scanner(System.in);  // Create a Scanner object
		System.out.print("\nChoose scenario: ");

		String scenario = userInputScanner.nextLine();  // Read user input
		switch(scenario) {
			case "1":
				System.out.println("Please provide the full file path to the POEditor Key-Value-pair export file.");

				System.out.print("POEditor export file: ");
				String poEditorKeyValueFileName = userInputScanner.nextLine();

				File poEditorKeyValueFile = new File(poEditorKeyValueFileName);
				try {
					Map<String, WoolTranslationFile> woolTranslationFiles = tools.generateWoolTranslationFiles(poEditorKeyValueFile);
					System.out.println("Successfully read translations for "+woolTranslationFiles.keySet().size()+" files.");
					System.out.println("Please choose a directory where you would like to store the WOOL Translation files.");

					System.out.print("Output directory: ");
					String outputDirectoryName = userInputScanner.nextLine();  // Read user input

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

					// The output directory should exist at this point...
					Iterator<String> translationFileIterator = woolTranslationFiles.keySet().iterator();
					while(translationFileIterator.hasNext()) {
						WoolTranslationFile wtf = woolTranslationFiles.get(translationFileIterator.next());
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
}
