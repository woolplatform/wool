package eu.woolplatform.utils.log;

/**
 * This factory can create log delegates with default settings.
 * 
 * @author Dennis Hofs
 */
public abstract class LogDelegateFactory {
	private static LogDelegateFactory instance = null;
	private static Object lock = new Object();
	
	/**
	 * Returns the log delegate factory that was set with {@link
	 * #setInstance(LogDelegateFactory) setInstance()}. If no factory was set,
	 * it will return a {@link DefaultLogDelegateFactory
	 * DefaultLogDelegateFactory}.
	 * 
	 * @return the log delegate factory
	 */
	public static LogDelegateFactory getInstance() {
		synchronized (lock) {
			if (instance == null)
				instance = new DefaultLogDelegateFactory();
			return instance;
		}
	}
	
	/**
	 * Sets the log delegate factory that should be returned by {@link
	 * #getInstance() getInstance()}.
	 * 
	 * @param factory the factory
	 */
	public static void setInstance(LogDelegateFactory factory) {
		synchronized (lock) {
			instance = factory;
		}
	}
	
	/**
	 * Creates a new log delegate.
	 * 
	 * @return the log delegate
	 */
	public abstract AbstractLogDelegate createLogDelegate();
}
