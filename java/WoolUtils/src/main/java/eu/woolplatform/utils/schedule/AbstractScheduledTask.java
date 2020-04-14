package eu.woolplatform.utils.schedule;

import eu.woolplatform.utils.exception.ParseException;

/**
 * Base scheduled task class with default implementations.
 *
 * @author Dennis Hofs (RRD)
 */
public abstract class AbstractScheduledTask implements ScheduledTask {
	private String id = null;
	private boolean runOnWorkerThread = false;
	private TaskSchedule schedule = new TaskSchedule.Immediate();

	@Override
	public String getId() {
		return id;
	}

	@Override
	public void setId(String id) {
		this.id = id;
	}

	@Override
	public String getTaskData() {
		return null;
	}

	@Override
	public void setTaskData(String taskData) throws ParseException {
	}

	@Override
	public TaskSchedule getSchedule() {
		return schedule;
	}

	/**
	 * Sets the schedule at which the task should be run. The default is
	 * {@link TaskSchedule.Immediate TaskSchedule.Immediate}.
	 *
	 * @param schedule the schedule at which the task should be run
	 */
	public void setSchedule(TaskSchedule schedule) {
		this.schedule = schedule;
	}

	@Override
	public boolean isRunOnWorkerThread() {
		return runOnWorkerThread;
	}

	/**
	 * Sets whether the task should run on a worker thread or on the scheduling
	 * thread (the UI thread in Android). The default is false.
	 *
	 * @param runOnWorkerThread true if the task should run on a worker thread,
	 * false if it should run on the scheduling thread
	 */
	public void setRunOnWorkerThread(boolean runOnWorkerThread) {
		this.runOnWorkerThread = runOnWorkerThread;
	}

	@Override
	public void cancel(Object context) {
	}
}
