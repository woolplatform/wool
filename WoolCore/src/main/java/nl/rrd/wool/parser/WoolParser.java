package nl.rrd.wool.parser;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import nl.rrd.wool.exception.LineNumberParseException;
import nl.rrd.wool.exception.ParseException;
import nl.rrd.wool.exception.WoolInvalidNodeLinkException;
import nl.rrd.wool.expressions.Expression;
import nl.rrd.wool.expressions.ExpressionParser;
import nl.rrd.wool.model.WoolDialogue;
import nl.rrd.wool.model.WoolNode;
import nl.rrd.wool.model.WoolNodeBody;
import nl.rrd.wool.model.WoolNodeHeader;
import nl.rrd.wool.model.nodepointer.WoolNodePointer;
import nl.rrd.wool.model.nodepointer.WoolNodePointerExternal;
import nl.rrd.wool.model.nodepointer.WoolNodePointerInternal;
import nl.rrd.wool.model.reply.WoolReply;
import nl.rrd.wool.model.reply.WoolReplyAutoForward;
import nl.rrd.wool.model.reply.WoolReplyBasic;
import nl.rrd.wool.model.reply.WoolReplyInput;
import nl.rrd.wool.model.reply.WoolReplyInput.InputType;
import nl.rrd.wool.model.statement.WoolStatement;
import nl.rrd.wool.model.statement.WoolStatementBasic;
import nl.rrd.wool.model.statement.WoolStatementBasicIdentified;
import nl.rrd.wool.model.statement.WoolStatementCommandIf;
import nl.rrd.wool.model.statement.WoolStatementCommandSet;
import nl.rrd.wool.model.statement.WoolStatementMultimedia.MultimediaType;
import nl.rrd.wool.model.statement.WoolStatementMultimediaImage;
import nl.rrd.wool.model.statement.WoolStatementMultimediaTimer;

/**
 * Note: First a dialogue is created by creating the nodes. Then the links between nodes are verified (i.e. does the next node exist). 
 * 
 * @author Harm op den Akker
 * @author Tessa Beinema
 */
public class WoolParser {
	
	private final static Logger logger = Logger.getLogger(WoolParser.class.getName());
	private int replyIndex;
	
	/**
	 * Use to run the WoolParser as a command line tool. Attempts to read a given .wool.txt file that can
	 * be provided as a run-time argument.
	 * @param args the location of a .wool.txt file to parse.
	 * @throws IOException
	 * @throws WoolInvalidNodeLinkException
	 * @throws ParseException 
	 */
	public static void main(String[] args) throws IOException, WoolInvalidNodeLinkException, ParseException {
		long startRunTime = System.currentTimeMillis();
		
		WoolParser parser = new WoolParser();
		
		if(args.length > 1) { 
			logger.info("WARNING: Running WoolParser with first argument: '"+args[0]+"', ignoring subsequent arguments.");
		}
		
		if(args.length == 0) {
			logger.info("WARNING: Please run the WoolParser by providing a .wool.txt file to parse.");
			logger.info("WARNING: Exiting system.");
			System.exit(0);
		}
		
		if(args[0].endsWith(".wool.txt")) {
			WoolDialogue woolDialogue = parser.createWoolDialogue(new File(args[0]));
			long executionTime = System.currentTimeMillis() - startRunTime;
			logger.info("Finished parsing '"+args[0]+"' in "+executionTime+ "ms.");
			logger.info(woolDialogue.toString());
		} else {
			logger.info("WARNING: Can only parse Wool script files with the .wool.txt extension, the file location given is invalid: '"+args[0]+"'.");
			logger.info("WARNING: Exiting system.");
			System.exit(0);
		}
	}
	
	public WoolParser() { 
		logger.setLevel(Level.FINE);
	}
	
	public WoolDialogue createWoolDialogue(File file) throws IOException, WoolInvalidNodeLinkException, ParseException {
		logger.finest("Attempting to create Wool Dialogue from file '"+file.getCanonicalPath()+"'.");
		WoolDialogue woolDialogue = new WoolDialogue();
		
		// Set the dialogue name
		String dialogueName = file.getName().substring(0, file.getName().length()-9); // TODO: Get name in a nicer way
		woolDialogue.setDialogueName(dialogueName);
		
		Scanner scanner = new Scanner(file, "utf-8");
		scanner.useDelimiter("===");
		
		return populateWoolDialogue(scanner,woolDialogue);
	}
	
	public WoolDialogue createWoolDialogue(InputStream inputStream, String dialogueName) throws WoolInvalidNodeLinkException, ParseException {
		logger.finest("Attempting to create Wool Dialogue from input stream, with dialogueName '"+dialogueName+"'.");

		WoolDialogue woolDialogue = new WoolDialogue();
		woolDialogue.setDialogueName(dialogueName);
		
		Scanner scanner = new Scanner(inputStream, "utf-8");
		scanner.useDelimiter("===");
		
		return populateWoolDialogue(scanner,woolDialogue);		
		
	}
	
	private WoolDialogue populateWoolDialogue(Scanner scanner, WoolDialogue woolDialogue) throws WoolInvalidNodeLinkException, ParseException {
		List<WoolNode> nodes = new ArrayList<WoolNode>();
		
		while(scanner.hasNext()) {
			WoolNode node = parseWoolNode(scanner.next());
			if(node != null) {
				nodes.add(node);
			}
		}
		scanner.close();
		
		woolDialogue.setNodes(nodes);
		WoolNode startNode = null;
		for (WoolNode node : woolDialogue.getNodes()) {
			if (node.getTitle().equalsIgnoreCase("Start")) {
				startNode = node;
			}
		}
		if (!(startNode == null)) {
			woolDialogue.setStartNode(startNode);
		}
		else {
			startNode = nodes.get(0);
			woolDialogue.setStartNode(nodes.get(0));
		}
		logger.info("Start node of dialogue '"+woolDialogue.getDialogueName()+"' set to '"+startNode.getTitle()+"'.");

		return validateWoolDialogue(woolDialogue);
	}
	
	private WoolDialogue validateWoolDialogue(WoolDialogue dialogue) throws WoolInvalidNodeLinkException {
		for(WoolNode node : dialogue.getNodes()) {
			for(WoolReply reply : node.getBody().getReplies()) {
				WoolNodePointer nextNodePointer = reply.getNodePointer();
				validateNextNodePointer(dialogue, node.getTitle(), nextNodePointer); 
			}
			for (WoolStatement statement : node.getBody().getStatements()) {
				if (statement instanceof WoolStatementBasicIdentified) {
					node.getHeader().setSpeaker(((WoolStatementBasicIdentified) statement).getSpeaker());
					dialogue.addSpeaker(((WoolStatementBasicIdentified)statement).getSpeaker());
				}
				if (statement instanceof WoolStatementCommandIf) {
					validateRepliesInWoolStatementCommandIf(dialogue, node, (WoolStatementCommandIf)statement);
				}
			}
		}
		return dialogue;
	}
	
	private void validateRepliesInWoolStatementCommandIf(WoolDialogue dialogue, WoolNode node, WoolStatementCommandIf ifStatement) throws WoolInvalidNodeLinkException {
		for (WoolStatement statement : ifStatement.getIfBody().getStatements()) {
			if (statement instanceof WoolStatementBasicIdentified) {
				dialogue.addSpeaker(((WoolStatementBasicIdentified)statement).getSpeaker());
			}
			if (statement instanceof WoolStatementCommandIf) {
				validateRepliesInWoolStatementCommandIf(dialogue, node, (WoolStatementCommandIf)statement);
			}
		}
		for(WoolReply reply : ifStatement.getIfBody().getReplies()) {
			WoolNodePointer nextNodePointer = reply.getNodePointer();
			validateNextNodePointer(dialogue, node.getTitle(), nextNodePointer);
		}
	}
	
	private void validateNextNodePointer(WoolDialogue dialogue, String nodeTitle, WoolNodePointer woolNodePointer) throws WoolInvalidNodeLinkException {
		if(woolNodePointer instanceof WoolNodePointerInternal) {
			WoolNodePointerInternal woolNodePointerInternal = (WoolNodePointerInternal)woolNodePointer;
			String nextNodeId = woolNodePointerInternal.getNodeId();
			if(!dialogue.nodeExists(nextNodeId)) {
				throw new WoolInvalidNodeLinkException("A reply in Wool Node '"+nodeTitle+ "' refers to a non-existing node ("+dialogue.getDialogueName()+"."+woolNodePointerInternal.toFriendlyString()+").");
			}
		}
		else {
			WoolNodePointerExternal woolNodePointerExternal = (WoolNodePointerExternal)woolNodePointer;
			String nextNodeId = woolNodePointerExternal.getNodeId();
		}
	}
	
	private WoolNode parseWoolNode(String nodeLines) throws ParseException {		
		WoolNode woolNode = new WoolNode();
		
		Scanner scanner = new Scanner(nodeLines);
		scanner.useDelimiter("---");
		
		WoolNodeHeader woolNodeHeader = null;
		WoolNodeBody woolNodeBody = null;
		
		// Parse WoolNodeHeader
		if(scanner.hasNext()) {
			woolNodeHeader = parseWoolNodeHeader(scanner.next());
			if(woolNodeHeader == null) {
				scanner.close();
				return null;
			} else {
				logger.finest("Finished parsing Wool Node Header: \n-----\n"+woolNodeHeader.toString()+"-----");
			}
		} else {
			scanner.close();
			return null;
		}
		
		// Parse WoolNodeBody
		if(scanner.hasNext()) {
			String bodyLines = scanner.next();
			this.replyIndex = 0;
			
			ArrayList<String> lines = new ArrayList<String>();
			Scanner toListScanner = new Scanner(bodyLines);
			while (toListScanner.hasNext()) {
				String line = toListScanner.nextLine();
				if (!line.equals("") && !line.trim().startsWith("//")) {
					lines.add(line);
				}
			}
			woolNodeBody = parseWoolNodeBody(lines);
			toListScanner.close();
			
			if(woolNodeBody == null) {
				scanner.close();
				return null;
			} else {
				logger.finest("Finished parsing Wool Node Body: \n-----\n"+woolNodeBody.toString()+"-----");
			}
		} else {
			scanner.close();
			return null;
		}
		
		woolNode.setHeader(woolNodeHeader);
		woolNode.setBody(woolNodeBody);
		
		scanner.close();
		
		return woolNode;
	}
	
	private WoolNodeHeader parseWoolNodeHeader(String lines) {
		
		WoolNodeHeader header = new WoolNodeHeader();
		
		Scanner scanner = new Scanner(lines);
		while(scanner.hasNextLine()) {
			String line = scanner.nextLine();

			if(!line.equals("")) {
				Scanner tagScanner = new Scanner(line);
				tagScanner.useDelimiter(":");
				
				String tag = null;
				String value = null;
				
				if(tagScanner.hasNext()) {
					tag = tagScanner.next();
					if(tagScanner.hasNext()) {
						value = tagScanner.next();
					}
				}
				
				if(tag != null && value != null) {
					tag = tag.trim();
					value = value.trim();
					
					if(tag.equals("title")) {
						header.setTitle(value);
					} else {
						header.addOptionalTag(tag,  value);
					}
				}
				tagScanner.close();
			}
		}
		
		scanner.close();
		
		if(header.getTitle() == null) {
			return null;
		} else {
			return header;
		}
	}
	
	private WoolNodeBody parseWoolNodeBody(ArrayList<String> lines) throws ParseException {
		WoolNodeBody body = new WoolNodeBody();
		
		while(lines.size() != 0) {
			
			// Parse replies
			if(lines.get(0).contains("[[")) {
				body.addReply(parseWoolReply(lines.get(0)));
				lines.remove(lines.get(0));
				if(lines.size() == 0) {
					break;
				}
			}
			
			// Parse statements
			else {
				body.addStatement(parseWoolStatement(lines));
				if(lines.size() == 0) {
					break;
				}
			}
		}
		return body;
	}
	
	private WoolReply parseWoolReply(String line) {
		WoolReply woolReply;
		boolean hasSetVariables;
		String[] splitReply = line.split("\\|");
		if(splitReply.length == 3) {
			hasSetVariables = true;
			if (splitReply[0].contains("<<")) {
				woolReply = parseWoolReplyInput(line, hasSetVariables);
			}
			else {
				woolReply = parseWoolReplyBasic(line, hasSetVariables);
			}
		}
		else if (splitReply.length == 2) {
			if(splitReply[1].contains("<<")) {
				hasSetVariables = true;
				woolReply = parseWoolReplyAutoForward(line, hasSetVariables);
			}
			else {
				hasSetVariables = false;
				if(splitReply[0].contains("<<")) {
					woolReply = parseWoolReplyInput(line, hasSetVariables);
				}
				else {
					woolReply = parseWoolReplyBasic(line, hasSetVariables);
				}
			}
		}
		else {
			hasSetVariables = false;
			woolReply = parseWoolReplyAutoForward(line, hasSetVariables);
		}
		this.replyIndex++;
		return woolReply;
	}
	
	private WoolReply parseWoolReplyBasic(String line, boolean hasSetVariables) {
		// Remove leading and trailing spaces
		String strippedLine = line.trim();
		strippedLine = strippedLine.substring(2,strippedLine.length()-2);
		String[] parts = strippedLine.split("\\|");
		Set<String> variablesInReply = this.checkLineForVariables(parts[0]);
		WoolReplyBasic woolReplyBasic = new WoolReplyBasic(this.replyIndex, parseNodePointer(parts[1]), parts[0]);
		if (variablesInReply.size() > 0) {
			woolReplyBasic.setVariablesInStatement(variablesInReply);
		}
		if (hasSetVariables) {
			Map<String, String> setVariables = parseSetInReply(parts[2]);
			woolReplyBasic.setVariablesToSet(setVariables);
		}
		return woolReplyBasic;	
	}
	
	private WoolReply parseWoolReplyAutoForward(String line, boolean hasSetVariables) {
		String strippedLine = line.trim().substring(2, line.length()-2);
		String[] parts = strippedLine.split("\\|");
		String nextNodeId = parts[0].trim();
		WoolReplyAutoForward woolReplyAutoForward = new WoolReplyAutoForward(this.replyIndex, parseNodePointer(nextNodeId));
		if(hasSetVariables) {
			Map<String, String> setVariables = parseSetInReply(parts[1]);
			woolReplyAutoForward.setVariablesToSet(setVariables);
		}
		return woolReplyAutoForward;
	}
	
	private WoolReply parseWoolReplyInput(String line, boolean hasSetVariables) {
		// [[Hi, my name is <<TextInput->$UserName min=2 max=15>>.|DialogueId.NodeId]]
		// Remove spaces before/after and remove [[ and ]]
		String strippedLine = line.trim();
		strippedLine = strippedLine.substring(2, strippedLine.length()-2);
		
		// Separate statement and node pointer.
		String[] splitReplyParts = strippedLine.split("\\|");
		String statement = splitReplyParts[0];
		statement = statement.trim();
		String nodePointer = splitReplyParts[1];
		String setVariablesString = "";
		if(hasSetVariables) {
			setVariablesString = splitReplyParts[2];
		}

		// Start with statement parsing:
		// Get substring from << to >> from statement and remove << and >> and any spaces in between.
		Matcher m = Pattern.compile("<<[^\\n]*>>").matcher(statement);
		String inputStatement = null;
		if(m.find()) {
			inputStatement = statement.substring(m.start()+2, m.end()-2).trim();
		}
		
		//If there are characters left before the << appoint a BeforeInputStatement and if there are any left after appoint an AfterInputStatement.
		String beforeInputStatement = "";
		if(m.start() != 0) {
			beforeInputStatement = statement.substring(0, m.start());
		}
		String afterInputStatement = ""; 
		if((statement.length()-m.end()) > 0) {
			afterInputStatement = statement.substring(m.end(), statement.length());
		}
		
		//Parse anything that was in between << and >>
		m = Pattern.compile("[\\w]+Input->\\$[\\w]+").matcher(inputStatement);
		String inputTypeAndVariable = null;
		if(m.find()) {
			inputTypeAndVariable = inputStatement.substring(m.start(), m.end());
		}
		String minAndMaxString = "";
		if ((inputStatement.length()-m.end()) > 0) {
			minAndMaxString = inputStatement.substring(m.end(), inputStatement.length()).trim();
		}
		
		int min = 0;
		int max = 0;
		if(minAndMaxString.length() > 0) {
			String[] minAndMaxParts = minAndMaxString.split(" ");
			String minString = minAndMaxParts[0].trim();
			String maxString = minAndMaxParts[1].trim();
			if (minString.length() > 0) {
				String minValue = minString.split("=")[1].trim();
				min = Integer.parseInt(minValue);
			}
			if (maxString.length() > 0) {
				String maxValue = maxString.split("=")[1].trim();
				max = Integer.parseInt(maxValue);
			}
		}
				
		//Parse the TextInput->$VarName (or NumericInput) and variable name
		String[] splitInputTypeAndVariable = inputTypeAndVariable.split("->");
		String inputTypeString = splitInputTypeAndVariable[0].trim();
		String inputVariableString = splitInputTypeAndVariable[1].trim();
		InputType inputType = null;
		if(inputTypeString.equalsIgnoreCase("TextInput")) {
			inputType = InputType.TEXT;
		}
		else if(inputTypeString.equalsIgnoreCase("NumericInput")){
			inputType = InputType.NUMERIC;
		}
		String inputVariable = inputVariableString.substring(1, inputVariableString.length());
		
		WoolReplyInput woolReplyInput = new WoolReplyInput(this.replyIndex, parseNodePointer(nodePointer), inputType, beforeInputStatement, inputVariable, afterInputStatement, min, max);
		if(hasSetVariables) {
			Map<String, String> setVariables = parseSetInReply(setVariablesString);
			woolReplyInput.setVariablesToSet(setVariables);
		}
		return woolReplyInput;
	}
	
	private WoolNodePointer parseNodePointer(String pointerText) {
		WoolNodePointer woolNodePointer;
		if(pointerText.contains(".")) {
			String[] splitPointerText = pointerText.split("\\.");
			String nextDialogueId = splitPointerText[0].trim();
			String nextNodeId = splitPointerText[1].trim();
			woolNodePointer = new WoolNodePointerExternal(nextDialogueId, nextNodeId);
		} else {
			String nextNodeId = pointerText.trim();
			woolNodePointer = new WoolNodePointerInternal(nextNodeId);
		}
		return woolNodePointer;
	}
	
	private Map<String, String> parseSetInReply(String replyString) {
		replyString = replyString.trim();
		Map<String, String> setVariables = new HashMap<String, String>();
		
		Matcher m = Pattern.compile("<<set \\$[\\w_-]+ to (\\w+|\"\\w+ \\w+\"|\"\\w+\")>>").matcher(replyString);

		while(m.find()) {
			String setExpression = m.group();
			setExpression = setExpression.trim();
			String setContents = setExpression.substring(5, setExpression.length()-2).trim();
			
			Matcher m2 = Pattern.compile("\\$[\\w_-]+").matcher(setContents);
			String variableName = null;
			String variableValue = null;
			if(m2.find()) {
				variableName = setContents.substring(m2.start()+1, m2.end());
				variableValue = setContents.substring(m2.end(),setContents.length()).trim();
			}
			variableValue = variableValue.substring(3, variableValue.length()).trim();
			if(variableValue.contains("\"")) {
				variableValue = variableValue.substring(1, variableValue.length()-1);
			}
			setVariables.put(variableName, variableValue);
		}
		return setVariables;
	}
	
	private WoolStatement parseWoolStatement(ArrayList<String> lines) throws ParseException {
		String firstLine = lines.get(0).trim();
		if (firstLine.startsWith("<<if")) {
			ArrayList<String> ifLines = new ArrayList<String>();
			int numberOfIfs = 1;
			ifLines.add(lines.get(0));
			lines.remove(lines.get(0));
			while (numberOfIfs != 0) {
				if (lines.get(0).contains("<<if")) {
					numberOfIfs++;
				}
				if (lines.get(0).startsWith("<<endif")) {
					numberOfIfs--;
				}
				ifLines.add(lines.get(0));
				lines.remove(lines.get(0));
			}
			return parseWoolStatementCommandIf(ifLines);
		}
		else if (firstLine.startsWith("<<set")) {
			String line = lines.get(0);
			lines.remove(0);
			return parseWoolStatementCommandSet(line);
		}
		else if (firstLine.startsWith("<<multimedia")) {
			String line = lines.get(0);
			lines.remove(0);
			return parseWoolStatementMultimedia(line);
		}
		
		else {
			Matcher m = Pattern.compile("\\w+:").matcher(firstLine);
			if (m.find()) {
				lines.remove(0);
				return parseWoolStatementBasicIdentified(firstLine);
			}
			else {
				String line = lines.get(0);
				lines.remove(0);
				return parseWoolStatementBasic(line);
			}
		}
	}
	
	private WoolStatement parseWoolStatementBasic(String line) {
		WoolStatementBasic woolStatementBasic = new WoolStatementBasic(line);
		Set<String> variablesInStatement = this.checkLineForVariables(line);
		if (variablesInStatement.size() > 0) {
			woolStatementBasic.setVariables(variablesInStatement);
		}
		return woolStatementBasic;
	}
	
	private WoolStatement parseWoolStatementBasicIdentified(String line) {
		Matcher m = Pattern.compile("\\w+:").matcher(line);
		String speaker = null;
		if(m.find()) {
			speaker = line.substring(m.start(), m.end()-1);
		}
		String statement = line.substring(line.indexOf(':') + 1).trim();
		WoolStatementBasicIdentified woolStatementBasicIdentified = new WoolStatementBasicIdentified(speaker, statement);
		Set<String> variablesInStatement = this.checkLineForVariables(statement);
		if (variablesInStatement.size() > 0) {
			woolStatementBasicIdentified.setVariables(variablesInStatement);
		}
		return woolStatementBasicIdentified;
	}
	
	private Set<String> checkLineForVariables(String line){
		Set<String> variablesInLine = new HashSet<String>();
		Matcher m = Pattern.compile("\\$[\\w_-]+").matcher(line);
		while (m.find()) {
			String variable = m.group();
			variable = variable.substring(1, variable.length());
			variablesInLine.add(variable);
		}
		return variablesInLine;
	}
	
	// <<if  A is  B>>
	private WoolStatement parseWoolStatementCommandIf(ArrayList<String> lines) throws ParseException {
		Expression expression;
		try {
			try {
				String ifExpression = lines.get(0).trim().substring(4, lines.get(0).length()-2);
				ExpressionParser expressionParser = new ExpressionParser(ifExpression);
				List<Expression> expressions = new ArrayList<>();
				Expression subExpr;
				while ((subExpr = expressionParser.readExpression()) != null) {
					expressions.add(subExpr);
				}
				if (expressions.size() != 1) {
					throw new ParseException("Invalid if expression: " + ifExpression);
				}
				expression = expressions.get(0);
			}
			catch (LineNumberParseException e) {
				String[] ifLine = lines.get(0).trim().split(" "); // [<<if, A, is, B>>]
				String conditionVariable = ifLine[1].trim(); // A
				conditionVariable = conditionVariable.substring(1, conditionVariable.length());
				String isString = ifLine[2].trim();
				if (!isString.equals("is"))
					throw new ParseException("Invalid if expression: " + lines.get(0));
				String strippedLine = ifLine[3].trim(); // B>> 
				String conditionValue = strippedLine.substring(0, strippedLine.length()-2); // B
				
				String expressionString = conditionVariable + " == \"" + conditionValue + "\"";
				
				ExpressionParser expressionParser = new ExpressionParser(expressionString);
				expression = expressionParser.readExpression();
			}
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
		
		WoolStatementCommandIf ifStatement = new WoolStatementCommandIf(expression);
		
		lines.remove(0);
		lines.remove(lines.size()-1);

		// Parse replies
		while(lines.size() > 0)
		{
			if(lines.get(0).contains("[[")) {
				String line = lines.get(0);
				ifStatement.getIfBody().addReply(parseWoolReply(line));
				lines.remove(line);
			}
			
			// Parse statements
			else {
				String line = lines.get(0);
				ifStatement.getIfBody().addStatement(parseWoolStatement(lines));
				lines.remove(line);
			}
		}
		return ifStatement;
	}
	
	private WoolStatement parseWoolStatementCommandSet(String line) {
		// Remove the <<set and >>
		line = line.trim();
		String setContents = line.substring(5, line.length()-2).trim();
		
		Matcher m = Pattern.compile("\\$[\\w_-]+").matcher(setContents);
		String variableName = null;
		String variableValue = null;
		if(m.find()) {
			variableName = setContents.substring(m.start()+1, m.end());
			variableValue = setContents.substring(m.end(),setContents.length()).trim();
		}
		variableValue = variableValue.substring(3, variableValue.length()).trim();
		if(variableValue.contains("\"")) {
			variableValue = variableValue.substring(1, variableValue.length()-1);
		}

		return new WoolStatementCommandSet(variableName, variableValue);
	}
	
	private WoolStatement parseWoolStatementMultimedia(String line) {
		//<<multimedia type=image name=name>> (or video)
		//<<multimedia type=timer duration=600>>
		String[] parts = line.trim().split(" ");
		String typeString = parts[1].trim();
		String[] typeParts = typeString.split("=");
		MultimediaType multimediaType;
		if(typeParts[1].equalsIgnoreCase("image")) {
			multimediaType = MultimediaType.IMAGE;
			String resourceNameString = parts[2].trim();
			String[] resourceNameParts = resourceNameString.split("=");
			String resourceName = resourceNameParts[1].trim();
			if (resourceName.contains(">>")) {
				resourceName = resourceName.substring(0, resourceName.length()-2); //Remove >> at the end
			}
			return new WoolStatementMultimediaImage(multimediaType, resourceName);
		}
		else if(typeParts[1].equalsIgnoreCase("video")) {
			multimediaType = MultimediaType.VIDEO;
			String resourceNameString = parts[2].trim();
			String[] resourceNameParts = resourceNameString.split("=");
			String resourceName = resourceNameParts[1].trim();
			if (resourceName.contains(">>")) {
				resourceName = resourceName.substring(0, resourceName.length()-2); //Remove >> at the end
			}
			return new WoolStatementMultimediaImage(multimediaType, resourceName);
		}
		else {
			multimediaType = MultimediaType.TIMER;
			String timerDurationString = parts[2].trim();
			String[] timerDurationParts = timerDurationString.split("=");
			String timerDuration = timerDurationParts[1].trim();
			if (timerDuration.contains(">>")) {
				timerDuration = timerDuration.substring(0, timerDuration.length()-2); //Remove >> at the end
			}
			int timerDurationInt = Integer.parseInt(timerDuration);
			return new WoolStatementMultimediaTimer(multimediaType, timerDurationInt);
		}
	}
}