package eu.woolplatform.utils.schedule;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import eu.woolplatform.utils.datetime.DateTimeUtils;

/**
 * The default implementation of {@link TaskScheduler TaskScheduler}. It uses
 * a {@link Timer Timer}. This is not a reliable way for task scheduling in
 * Android, because Android devices may pause the CPU clock when they go to
 * sleep.
 *
 * @author Dennis Hofs (RRD)
 */
public class DefaultTaskScheduler extends TaskScheduler {
	private final Object lock = new Object();

	// map from task ID to timer
	private Map<String,Timer> timerMap = new HashMap<>();

	// map from task ID to task specs
	private Map<String,ScheduledTaskSpec> taskMap = new HashMap<>();

	@Override
	protected void scheduleTask(Object context, ScheduledTaskSpec taskSpec) {
		synchronized (lock) {
			Timer timer = new Timer();
			final String taskId = taskSpec.getId();
			timerMap.put(taskId, timer);
			taskMap.put(taskId, taskSpec);
			DateTime time;
			ScheduleParams scheduleParams = taskSpec.getScheduleParams();
			if (scheduleParams.getLocalTime() != null) {
				time = DateTimeUtils.localToUtcWithGapCorrection(
						scheduleParams.getLocalTime(),
						DateTimeZone.getDefault());
			} else {
				time = new DateTime(scheduleParams.getUtcTime());
			}
			timer.schedule(new TimerTask() {
				@Override
				public void run() {
					runTask(taskId);
				}
			}, time.toDate());
		}
	}

	/**
	 * Runs a task.
	 *
	 * @param taskId the task ID
	 */
	private void runTask(String taskId) {
		final ScheduledTaskSpec taskSpec;
		synchronized (lock) {
			timerMap.remove(taskId);
			taskSpec = taskMap.remove(taskId);
		}
		if (taskSpec == null)
			return;
		onTriggerTask(null, taskSpec);
	}

	@Override
	protected void cancelScheduledTask(Object context, String taskId) {
		synchronized (lock) {
			taskMap.remove(taskId);
			Timer timer = timerMap.remove(taskId);
			if (timer != null)
				timer.cancel();
		}
	}

	@Override
	protected void runOnUiThread(Runnable runnable) {
		runnable.run();
	}

	@Override
	protected boolean canRunTaskOnMainThread() {
		return false;
	}
}
