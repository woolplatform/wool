package eu.woolplatform.utils.log;

import java.io.PrintStream;

/**
 * This class writes log messages to standard output or standard error.
 * Messages with log level {@link Logger#WARN WARN} or higher will be written
 * to standard error. Other messages will be written to standard output.
 * 
 * <p>For more information see {@link Logger Logger} and {@link
 * AbstractLogDelegate AbstractLogDelegate}.</p>
 * 
 * @author Dennis Hofs
 */
public class DefaultLogDelegate extends AbstractLogDelegate {
	@Override
	public int printTaggedMessage(int priority, String tag, String msg) {
		PrintStream out;
		if (priority >= Logger.WARN)
			out = getDefaultStdErr();
		else
			out = getDefaultStdOut();
		out.print(msg);
		return 0;
	}
}
