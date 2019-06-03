package nl.rrd.wool.execution;

import java.util.List;

import nl.rrd.wool.model.WoolDialogue;
import nl.rrd.wool.model.WoolNode;
import nl.rrd.wool.model.statement.WoolStatement;
import nl.rrd.wool.model.statement.WoolStatementBasic;
import nl.rrd.wool.model.statement.WoolStatementBasicIdentified;

public class WoolDefinitionScanner {
	
	private WoolDefinitionAnalysisReport report;
	
	public WoolDefinitionScanner() {

	}
	
	/**
	 * Runs a check on the given {@link WoolDialogue}, generating a set of error or warning
	 * messages in the return statement. The first {@code String} in the returned list is the summary report
	 * of this check.
	 * 
	 * TODO: Check colorings of nodes.
	 * 
	 * @param woolDialogue the {@link WoolDialogue} to check.
	 * @return a list of error/warning messages.
	 */
	public WoolDefinitionAnalysisReport checkDefinition(WoolDialogue woolDialogue) {
		
		report = new WoolDefinitionAnalysisReport(woolDialogue);
		
		List<WoolNode> woolNodes = woolDialogue.getNodes();
		
		for(WoolNode woolNode : woolNodes) {
			
			if(woolNode.getBody().getStatements().size() > 1) {
				report.addWarning("Wool Node '"+woolNode.getTitle()+"' contains more than one statement - consider splitting into multiple nodes.");
			}
			
			for(WoolStatement statement : woolNode.getBody().getStatements()) {
				if(statement instanceof WoolStatementBasic) {
					report.addWarning("Wool Node '"+woolNode.getTitle()+"' contains statement without specified speaker.");
				} else if(statement instanceof WoolStatementBasicIdentified) {
					WoolStatementBasicIdentified statementIdentified = (WoolStatementBasicIdentified)statement;
					report.addSpeakerIfUnique(statementIdentified.getSpeaker());
				}
			}
		}
		
		report.reportStopTime(); // Indicate that the analysis completed
		return report;
	}
	
}
