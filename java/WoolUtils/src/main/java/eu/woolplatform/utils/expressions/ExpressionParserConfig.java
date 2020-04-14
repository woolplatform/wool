package eu.woolplatform.utils.expressions;

public class ExpressionParserConfig {
	private boolean allowPlainVariables = true;
	private boolean allowDollarVariables = false;

	public boolean isAllowPlainVariables() {
		return allowPlainVariables;
	}

	public void setAllowPlainVariables(boolean allowPlainVariables) {
		this.allowPlainVariables = allowPlainVariables;
	}

	public boolean isAllowDollarVariables() {
		return allowDollarVariables;
	}

	public void setAllowDollarVariables(boolean allowDollarVariables) {
		this.allowDollarVariables = allowDollarVariables;
	}
}
