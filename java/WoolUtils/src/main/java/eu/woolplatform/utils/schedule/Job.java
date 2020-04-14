package eu.woolplatform.utils.schedule;

/**
 * This interface defines a job that can be run on a separate thread and that
 * can be cancelled. It can be posted to a {@link SerialJobRunner
 * SerialJobRunner}.
 *
 * @author Dennis Hofs (RRD)
 */
public interface Job {

	/**
	 * Runs the job.
	 */
	void run();

	/**
	 * Called when the job is cancelled. This can also be called if {@link
	 * #run() run()} hasn't been called. This method is called on the same
	 * thread as {@link SerialJobRunner#cancelJobs()
	 * SerialJobRunner.cancelJobs()}.
	 */
	void cancel();
}
