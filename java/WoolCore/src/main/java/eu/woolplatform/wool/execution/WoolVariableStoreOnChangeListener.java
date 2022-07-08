package eu.woolplatform.wool.execution;

import java.util.List;

public interface WoolVariableStoreOnChangeListener {
	void onChange(WoolVariableStore varStore, List<WoolVariableStoreChange> changes);
}

