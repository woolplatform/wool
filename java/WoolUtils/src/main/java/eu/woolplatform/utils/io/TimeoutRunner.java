package eu.woolplatform.utils.io;

import eu.woolplatform.utils.exception.TimeoutException;

public class TimeoutRunner {
	/**
	 * Runs the specified runner and waits until it is completed or the time-out
	 * duration expired. If the time-out duration expires, the runner will be
	 * stopped.
	 *
	 * @param runner the runner
	 * @param timeout the time-out duration in milliseconds
	 * @throws TimeoutException if the runner has timed out
	 */
	public static void run(Runner runner, int timeout) throws TimeoutException {
		RunState runState = new RunState();
		new Thread(() -> waitTimeout(runner, timeout, runState)).start();
		try {
			runner.run();
		} finally {
			synchronized (runState.lock) {
				runState.finished = true;
				runState.lock.notifyAll();
			}
		}
		if (runState.timedOut) {
			throw new TimeoutException(String.format(
					"Time-out of %s ms expired", timeout));
		}
	}

	private static void waitTimeout(Runner runner, int timeout,
			RunState runState) {
		long now = System.currentTimeMillis();
		long end = now + timeout;
		synchronized (runState.lock) {
			while (!runState.finished && now < end) {
				try {
					runState.lock.wait(end - now);
				} catch (InterruptedException ex) {
					throw new RuntimeException("Thread interrupted: " +
							ex.getMessage(), ex);
				}
				now = System.currentTimeMillis();
			}
			if (runState.finished)
				return;
			runState.timedOut = true;
		}
		// time-out expired
		runner.stop();
	}

	private static class RunState {
		public final Object lock = new Object();
		public boolean finished = false;
		public boolean timedOut = false;
	}

	public interface Runner {
		void run();
		void stop();
	}
}
