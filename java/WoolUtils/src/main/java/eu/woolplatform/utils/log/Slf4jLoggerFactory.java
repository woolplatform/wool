package eu.woolplatform.utils.log;

import org.slf4j.ILoggerFactory;

/**
 * This SLF4J logger factory creates instances of {@link Slf4jLogger
 * Slf4jLogger}, which uses the RRD {@link Logger Logger} class.
 * 
 * @author Dennis Hofs (RRD)
 */
public class Slf4jLoggerFactory implements ILoggerFactory {

	@Override
	public org.slf4j.Logger getLogger(String name) {
		return new Slf4jLogger(name);
	}
}
