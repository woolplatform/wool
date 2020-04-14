package eu.woolplatform.utils.log;

/**
 * This factory creates instances of {@link DefaultLogDelegate
 * DefaultLogDelegate}.
 * 
 * @author Dennis Hofs
 */
public class DefaultLogDelegateFactory extends LogDelegateFactory {
	@Override
	public AbstractLogDelegate createLogDelegate() {
		return new DefaultLogDelegate();
	}
}
