/*
 * Copyright 2019 Roessingh Research and Development.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 */

package eu.woolplatform.utils.schedule;

import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;
import org.joda.time.Months;
import org.joda.time.Years;
import org.slf4j.Logger;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import eu.woolplatform.utils.AppComponent;
import eu.woolplatform.utils.AppComponents;
import eu.woolplatform.utils.ReflectionUtils;
import eu.woolplatform.utils.exception.HandledException;
import eu.woolplatform.utils.exception.ParseException;

/**
 * The task scheduler can be used to schedule one-time or repeating tasks to
 * be run at specific times. It has an implementation for Android and for
 * standard Java.
 *
 * <p>If you schedule background tasks in Android, you should be aware that
 * Android devices may kill the app process in the background. As long as the
 * app process is running, the task scheduler will keep scheduled tasks in
 * memory so they can be run at the scheduled time. But if the app is killed,
 * the scheduled task needs to be reconstructed at the scheduled time. This is
 * done using a data string that is obtained from the {@link ScheduledTask
 * ScheduledTask} method {@link ScheduledTask#getTaskData() getTaskData()}.
 * The task is rebuilt with these steps:</p>
 *
 * <p><ul>
 * <li>A new instance of the same class of {@link ScheduledTask ScheduledTask}
 * is constructed. This is done using a constructor that takes one Context
 * parameter (in Android) or the default constructor.</li>
 * <li>The method {@link ScheduledTask#setTaskData(String) setTaskData()} is
 * called.</li>
 * </ul></p>
 *
 * <p>If you schedule a task that may need to be reconstructed, make sure that
 * the methods {@link ScheduledTask#getTaskData() getTaskData()} and {@link
 * ScheduledTask#setTaskData(String) setTaskData()} are implemented, and that
 * the {@link ScheduledTask ScheduledTask} class has an accessible default
 * constructor or a constructor that takes one Android Context parameter. In
 * general try to do this for any task with another schedule than {@link
 * TaskSchedule.Immediate TaskSchedule.Immediate}. You can omit it if the task
 * is scheduled while the app is in the foreground (e.g. an activity or fragment
 * is resumed) and it's cancelled when the app goes to the background (an
 * activity or fragment is paused).</p>
 *
 * @author Dennis Hofs (RRD)
 */
@AppComponent
public abstract class TaskScheduler {
	public static final String LOGTAG = TaskScheduler.class.getSimpleName();

	private final Object lock = new Object();

	// map from task ID to running task
	private Map<String,ScheduledTask> runningTasks = new HashMap<>();

	// map from task ID to scheduled task
	// This map contains tasks that have been scheduled and that have not been
	// cancelled or completed. Repeating tasks stay in this map until the final
	// run has been completed.
	private Map<String,ScheduledTask> scheduledTasks = new HashMap<>();

	// map from task ID to scheduled task instance
	// This map contains single task instances that have been scheduled to run
	// at a specific time. An instance is removed when the task is cancelled or
	// when it's triggered.
	private Map<String,ScheduledTaskSpec> scheduledTaskInstances =
			new HashMap<>();

	private Logger logger;

	public TaskScheduler() {
		logger = AppComponents.getLogger(LOGTAG);
	}

	/**
	 * Initializes the tasks that were scheduled at a previous run and that
	 * have not been triggered or cancelled yet. In Android this method is
	 * called on the UI thread.
	 *
	 * @param context the context (only set in Android)
	 * @param taskSpecs the tasks
	 */
	public void initScheduledTasks(Object context,
			List<ScheduledTaskSpec> taskSpecs) {
		synchronized (lock) {
			for (ScheduledTaskSpec taskSpec : taskSpecs) {
				cancelScheduledTask(context, taskSpec.getId());
				ScheduledTask task;
				try {
					task = buildTask(context, taskSpec.getClassName(),
							taskSpec.getId(), taskSpec.getTaskData());
				} catch (HandledException ex) {
					continue;
				}
				logger.info("Restore scheduled task " +
						getScheduledTaskSpecLog(taskSpec));
				scheduledTasks.put(taskSpec.getId(), task);
				scheduledTaskInstances.put(taskSpec.getId(), taskSpec);
				scheduleTask(context, taskSpec);
			}
		}
	}

	private String getScheduledTaskSpecLog(ScheduledTaskSpec taskSpec) {
		ScheduleParams scheduleParams = taskSpec.getScheduleParams();
		boolean exact = scheduleParams.isExact();
		String time;
		if (scheduleParams.getLocalTime() != null) {
			time = scheduleParams.getLocalTime().toString(
					"yyyy-MM-dd'T'HH:mm:ss.SSS");
		} else {
			time = new DateTime(scheduleParams.getUtcTime()).toString(
					"yyyy-MM-dd'T'HH:mm:ss.SSSZZ");
		}
		return String.format("\"%s\" (%s) scheduled %s at %s",
				taskSpec.getName(), taskSpec.getId(),
				exact ? "exactly" : "approximately", time);
	}

	/**
	 * Finds any scheduled or running tasks that are an instance of the
	 * specified task class. It returns a map from task ID to {@link
	 * ScheduledTask ScheduledTask}.
	 *
	 * @param taskClass the task class
	 * @param <T> the type of task
	 * @return the tasks
	 */
	public <T extends ScheduledTask> List<T> findTasksWithClass(
			Class<T> taskClass) {
		synchronized (lock) {
			List<T> result = new ArrayList<>();
			Map<String,ScheduledTask> tasks = new LinkedHashMap<>(
					runningTasks);
			tasks.putAll(scheduledTasks);
			for (String taskId : tasks.keySet()) {
				ScheduledTask task = tasks.get(taskId);
				if (taskClass.isInstance(task))
					result.add(taskClass.cast(task));
			}
			return result;
		}
	}

	/**
	 * Generates a random task ID.
	 *
	 * @return the task ID
	 */
	public String generateTaskId() {
		return UUID.randomUUID().toString().toLowerCase().replaceAll("-", "");
	}

	/**
	 * Schedules a task. See the discussion at the top of this page about
	 * rebuilding tasks. In Android this method is called on the UI thread.
	 *
	 * @param context the context (only set in Android)
	 * @param task the task
	 * @param taskId the task ID
	 */
	public void scheduleTask(Object context, ScheduledTask task,
			String taskId) {
		synchronized (lock) {
			task.setId(taskId);
			scheduledTasks.put(taskId, task);
			DateTime now = new DateTime();
			TaskSchedule schedule = task.getSchedule();
			if (schedule instanceof TaskSchedule.Immediate) {
				startImmediate(context, task, now);
			} else if (schedule instanceof TaskSchedule.FixedDelay) {
				startFixedDelay(context, task, now, new ScheduleParams(
						now.getMillis(), false));
			} else if (schedule instanceof TaskSchedule.FixedRate) {
				startFixedRate(context, task, now, new ScheduleParams(
						now.getMillis(), true));
			} else if (schedule instanceof TaskSchedule.TimeSchedule) {
				scheduleTimeSchedule(context, task, now.toLocalDateTime());
			} else if (schedule instanceof TaskSchedule.LocalTime) {
				scheduleLocalTime(context, task);
			} else if (schedule instanceof TaskSchedule.UtcTime) {
				scheduleUtcTime(context, task);
			}
		}
	}

	/**
	 * Cancels the scheduled or running task with the specified task ID. In
	 * Android this method is called on the UI thread.
	 *
	 * @param context the context (only set in Android)
	 * @param taskId the task ID
	 */
	public void cancelTask(Object context, String taskId) {
		synchronized (lock) {
			scheduledTaskInstances.remove(taskId);
			cancelScheduledTask(context, taskId);
			ScheduledTask task = scheduledTasks.remove(taskId);
			if (task != null) {
				logger.info(String.format(
						"Cancelled scheduled task \"%s\" (%s)",
						task.getName(), taskId));
			}
			task = runningTasks.remove(taskId);
			if (task != null) {
				task.cancel(context);
				logger.info(String.format(
						"Cancelled running task \"%s\" (%s)",
						task.getName(), taskId));
			}
		}
	}

	/**
	 * Cancels any scheduled or running tasks that are an instance of the
	 * specified task class.
	 *
	 * @param context the context
	 * @param taskClass the task class
	 */
	public void cancelTasksWithClass(Object context,
			Class<? extends ScheduledTask> taskClass) {
		synchronized (lock) {
			List<? extends ScheduledTask> tasks = findTasksWithClass(taskClass);
			for (ScheduledTask task : tasks) {
				cancelTask(context, task.getId());
			}
		}
	}

	/**
	 * Schedules a task at a specified time. At the due time it should call
	 * {@link #onTriggerTask(Object, ScheduledTaskSpec) onTriggerTask()}. In
	 * Android this method is called on the UI thread.
	 *
	 * @param context the context (only set in Android)
	 * @param taskSpec the specification of the task instance
	 */
	protected abstract void scheduleTask(Object context,
			ScheduledTaskSpec taskSpec);

	/**
	 * Cancels the scheduled task with the specified task ID. In Android this
	 * method is called on the UI thread.
	 *
	 * @param context the context (only set in Android)
	 * @param taskId the task ID
	 */
	protected abstract void cancelScheduledTask(Object context, String taskId);

	/**
	 * Runs code on the UI thread in Android. In standard Java, the code can
	 * be run immediately on the calling thread.
	 *
	 * @param runnable the code to run
	 */
	protected abstract void runOnUiThread(Runnable runnable);
	
	/**
	 * Returns whether {@link ScheduledTask#isRunOnWorkerThread()
	 * isRunOnWorkerThread()} should be checked to see if a task should be run
	 * on the main thread or on the worker thread. This is used in Android to
	 * run tasks on the UI thread. If this method returns false, a task is
	 * always run on a worker thread.
	 * 
	 * @return true if a task can be run on the main thread, false otherwise
	 */
	protected abstract boolean canRunTaskOnMainThread();

	/**
	 * Starts a task with schedule {@link TaskSchedule.Immediate
	 * TaskSchedule.Immediate}. In Android this method is called on the UI
	 * thread.
	 *
	 * @param context the context (only set in Android)
	 * @param task the task
	 * @param now the current time
	 */
	private void startImmediate(final Object context, final ScheduledTask task,
			final DateTime now) {
		if (!canRunTaskOnMainThread() || task.isRunOnWorkerThread()) {
			new Thread() {
				@Override
				public void run() {
					runImmediate(context, task, now);
				}
			}.start();
		} else {
			runImmediate(context, task, now);
		}
	}

	/**
	 * Runs a task with schedule {@link TaskSchedule.Immediate
	 * TaskSchedule.Immediate}. In Android this method is called on a worker
	 * thread or on the UI thread depending on the result of {@link
	 * ScheduledTask#isRunOnWorkerThread() task.isRunOnWorkerThread()}.
	 *
	 * @param context the context (only set in Android)
	 * @param task the task
	 * @param now the current time
	 */
	private void runImmediate(Object context, ScheduledTask task,
			DateTime now) {
		String taskId = task.getId();
		synchronized (lock) {
			if (!scheduledTasks.containsKey(taskId))
				return;
			logger.info(String.format("Start immediate task \"%s\" (%s)",
					task.getName(), taskId));
			runningTasks.put(taskId, task);
		}
		ScheduleParams scheduleParams = new ScheduleParams(now.getMillis(),
				true);
		Throwable exception = null;
		try {
			task.run(context, taskId, now, scheduleParams);
		} catch (Throwable ex) {
			exception = ex;
		}
		synchronized (lock) {
			if (!scheduledTasks.containsKey(taskId))
				return;
			runningTasks.remove(taskId);
			scheduledTasks.remove(taskId);
			if (exception == null) {
				logger.info(String.format(
						"Immediate task \"%s\" (%s) completed",
						task.getName(), taskId));
			} else {
				logger.error(String.format(
						"Error in immediate task \"%s\" (%s)",
						task.getName(), taskId) + ": " + exception.getMessage(),
						exception);
			}
		}
	}

	/**
	 * Schedules a task with schedule {@link TaskSchedule.FixedDelay
	 * TaskSchedule.FixedDelay} to run at the specified time. In Android this
	 * method is called on the UI thread.
	 *
	 * @param context the context (only set in Android)
	 * @param task the task
	 * @param time the scheduled time
	 */
	private void scheduleFixedDelay(Object context, ScheduledTask task,
			DateTime time) {
		synchronized (lock) {
			String taskId = task.getId();
			if (!scheduledTasks.containsKey(taskId))
				return;
			logger.info(String.format(
					"Schedule fixed delay task \"%s\" (%s) at %s",
					task.getName(), taskId,
					time.toString("yyyy-MM-dd'T'HH:mm:ss.SSSZZ")));
			ScheduleParams scheduleParams = new ScheduleParams(time.getMillis(),
					false);
			ScheduledTaskSpec taskSpec = new ScheduledTaskSpec(taskId, task,
					scheduleParams);
			scheduledTaskInstances.put(taskId, taskSpec);
			scheduleTask(context, taskSpec);
		}
	}

	/**
	 * Starts a task with schedule {@link TaskSchedule.FixedDelay
	 * TaskSchedule.FixedDelay}. In Android this method is called on the UI
	 * thread.
	 *
	 * @param context the context (only set in Android)
	 * @param task the task
	 * @param now the current time
	 * @param scheduleParams the scheduled time parameters
	 */
	private void startFixedDelay(final Object context, final ScheduledTask task,
			final DateTime now, final ScheduleParams scheduleParams) {
		if (!canRunTaskOnMainThread() || task.isRunOnWorkerThread()) {
			new Thread() {
				@Override
				public void run() {
					runFixedDelay(context, task, now, scheduleParams);
				}
			}.start();
		} else {
			runFixedDelay(context, task, now, scheduleParams);
		}
	}

	/**
	 * Runs a task with schedule {@link TaskSchedule.FixedDelay
	 * TaskSchedule.FixedDelay}. In Android this method is called on a worker
	 * thread or on the UI thread depending on the result of {@link
	 * ScheduledTask#isRunOnWorkerThread() task.isRunOnWorkerThread()}.
	 *
	 * @param context the context (only set in Android)
	 * @param task the task
	 * @param now the current time
	 * @param scheduleParams the scheduled time parameters
	 */
	private void runFixedDelay(final Object context, final ScheduledTask task,
			DateTime now, ScheduleParams scheduleParams) {
		String taskId = task.getId();
		synchronized (lock) {
			if (!scheduledTasks.containsKey(taskId))
				return;
			DateTime time = new DateTime(scheduleParams.getUtcTime());
			logger.info(String.format(
					"Start fixed delay task \"%s\" (%s) scheduled at %s",
					task.getName(), taskId,
					time.toString("yyyy-MM-dd'T'HH:mm:ss.SSSZZ")));
			runningTasks.put(taskId, task);
		}
		Throwable exception = null;
		try {
			task.run(context, taskId, now, scheduleParams);
		} catch (Throwable ex) {
			exception = ex;
		}
		synchronized (lock) {
			if (!scheduledTasks.containsKey(taskId))
				return;
			now = new DateTime();
			runningTasks.remove(taskId);
			if (exception == null) {
				logger.info(String.format(
						"Fixed delay task \"%s\" (%s) completed",
						task.getName(), taskId));
			} else {
				logger.error(String.format(
						"Error in fixed delay task \"%s\" (%s)",
						task.getName(), taskId) + ": " + exception.getMessage(),
						exception);
			}
			TaskSchedule.FixedDelay schedule =
					(TaskSchedule.FixedDelay)task.getSchedule();
			final long next = now.getMillis() + schedule.getDelay();
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					scheduleFixedDelay(context, task, new DateTime(next));
				}
			});
		}
	}

	/**
	 * Schedules a task with schedule {@link TaskSchedule.FixedRate
	 * TaskSchedule.FixedRate} to run at the specified time. In Android this
	 * method is called on the UI thread.
	 *
	 * @param context the context (only set in Android)
	 * @param task the task
	 * @param time the scheduled time
	 */
	private void scheduleFixedRate(Object context, ScheduledTask task,
			DateTime time) {
		synchronized (lock) {
			String taskId = task.getId();
			if (!scheduledTasks.containsKey(taskId))
				return;
			logger.info(String.format(
					"Schedule fixed rate task \"%s\" (%s) at %s",
					task.getName(), taskId,
					time.toString("yyyy-MM-dd'T'HH:mm:ss.SSSZZ")));
			ScheduleParams scheduleParams = new ScheduleParams(time.getMillis(),
					true);
			ScheduledTaskSpec taskSpec = new ScheduledTaskSpec(taskId, task,
					scheduleParams);
			scheduledTaskInstances.put(taskId, taskSpec);
			scheduleTask(context, taskSpec);
		}
	}

	/**
	 * Starts a task with schedule {@link TaskSchedule.FixedRate
	 * TaskSchedule.FixedRate}. In Android this method is called on the UI
	 * thread.
	 *
	 * @param context the context (only set in Android)
	 * @param task the task
	 * @param now the current time
	 * @param scheduleParams the scheduled time parameters
	 */
	private void startFixedRate(final Object context, final ScheduledTask task,
			final DateTime now, final ScheduleParams scheduleParams) {
		if (!canRunTaskOnMainThread() || task.isRunOnWorkerThread()) {
			new Thread() {
				@Override
				public void run() {
					runFixedRate(context, task, now, scheduleParams);
				}
			}.start();
		} else {
			runFixedRate(context, task, now, scheduleParams);
		}
	}

	/**
	 * Runs a task with schedule {@link TaskSchedule.FixedRate
	 * TaskSchedule.FixedRate}. In Android this method is called on a worker
	 * thread or on the UI thread depending on the result of {@link
	 * ScheduledTask#isRunOnWorkerThread() task.isRunOnWorkerThread()}.
	 *
	 * @param context the context (only set in Android)
	 * @param task the task
	 * @param now the current time
	 * @param scheduleParams the scheduled time parameters
	 */
	private void runFixedRate(final Object context, final ScheduledTask task,
			DateTime now, ScheduleParams scheduleParams) {
		String taskId = task.getId();
		DateTime time = new DateTime(scheduleParams.getUtcTime());
		synchronized (lock) {
			if (!scheduledTasks.containsKey(taskId))
				return;
			logger.info(String.format(
					"Start fixed rate task \"%s\" (%s) scheduled at %s",
					task.getName(), taskId,
					time.toString("yyyy-MM-dd'T'HH:mm:ss.SSSZZ")));
			runningTasks.put(taskId, task);
		}
		Throwable exception = null;
		try {
			logger.info("BEFORE FIXED RATE TASK: " + task);
			task.run(context, taskId, now, scheduleParams);
			logger.info("AFTER FIXED RATE TASK: " + task);
		} catch (Throwable ex) {
			exception = ex;
		}
		synchronized (lock) {
			if (!scheduledTasks.containsKey(taskId))
				return;
			runningTasks.remove(taskId);
			if (exception == null) {
				logger.info(String.format(
						"Fixed rate task \"%s\" (%s) completed",
						task.getName(), taskId));
			} else {
				logger.error(String.format(
						"Error in fixed rate task \"%s\" (%s)",
						task.getName(), taskId) + ": " + exception.getMessage(),
						exception);
			}
			TaskSchedule.FixedRate schedule =
					(TaskSchedule.FixedRate)task.getSchedule();
			long interval = schedule.getInterval();
			long nowMs = System.currentTimeMillis();
			long timeMs = time.getMillis();
			long iter = (nowMs - timeMs) / interval;
			final long next = timeMs + (iter + 1) * interval;
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					scheduleFixedRate(context, task, new DateTime(next));
				}
			});
		}
	}

	/**
	 * Schedules a task with schedule {@link TaskSchedule.TimeSchedule
	 * TaskSchedule.TimeSchedule}. In Android this method is called on the UI
	 * thread.
	 *
	 * @param context the context (only set in Android)
	 * @param task the task
	 * @param start the time at or after which the next task should be scheduled
	 */
	private void scheduleTimeSchedule(Object context, ScheduledTask task,
			LocalDateTime start) {
		synchronized (lock) {
			String taskId = task.getId();
			if (!scheduledTasks.containsKey(taskId))
				return;
			TaskSchedule.TimeSchedule schedule =
					(TaskSchedule.TimeSchedule)task.getSchedule();
			String logStr = String.format(
					"Find next time for time schedule task \"%s\" (%s) at or after %s",
					task.getName(), taskId,
					start.toString("yyyy-MM-dd'T'HH:mm:ss.SSS"));
			LocalDateTime taskTime = getNextScheduledDateTime(start, schedule);
			if (taskTime == null) {
				logger.info(logStr + ": no next time");
				scheduledTasks.remove(taskId);
				return;
			}
			logger.info(logStr + ": " +
					taskTime.toString("yyyy-MM-dd'T'HH:mm:ss.SSS"));
			ScheduleParams scheduleParams = new ScheduleParams(taskTime, true);
			ScheduledTaskSpec taskSpec = new ScheduledTaskSpec(taskId, task,
					scheduleParams);
			scheduledTaskInstances.put(taskId, taskSpec);
			scheduleTask(context, taskSpec);
		}
	}

	/**
	 * Starts a task with schedule {@link TaskSchedule.TimeSchedule
	 * TaskSchedule.TimeSchedule}. In Android this method is called on the UI
	 * thread.
	 *
	 * @param context the context (only set in Android)
	 * @param task the task
	 * @param now the current time
	 * @param scheduleParams the scheduled time parameters
	 */
	private void startTimeSchedule(final Object context,
			final ScheduledTask task, final DateTime now,
			final ScheduleParams scheduleParams) {
		if (!canRunTaskOnMainThread() || task.isRunOnWorkerThread()) {
			new Thread() {
				@Override
				public void run() {
					runTimeSchedule(context, task, now, scheduleParams);
				}
			}.start();
		} else {
			runTimeSchedule(context, task, now, scheduleParams);
		}
	}

	/**
	 * Runs a task with schedule {@link TaskSchedule.TimeSchedule
	 * TaskSchedule.TimeSchedule}. In Android this method is called on a worker
	 * thread or on the UI thread depending on the result of {@link
	 * ScheduledTask#isRunOnWorkerThread() task.isRunOnWorkerThread()}.
	 *
	 * @param context the context (only set in Android)
	 * @param task the task
	 * @param now the current time
	 * @param scheduleParams the scheduled time parameters
	 */
	private void runTimeSchedule(final Object context, final ScheduledTask task,
			DateTime now, ScheduleParams scheduleParams) {
		String taskId = task.getId();
		LocalDateTime time = scheduleParams.getLocalTime();
		synchronized (lock) {
			if (!scheduledTasks.containsKey(taskId))
				return;
			logger.info(String.format(
					"Start time schedule task \"%s\" (%s) scheduled at %s",
					task.getName(), taskId,
					time.toString("yyyy-MM-dd'T'HH:mm:ss.SSS")));
			runningTasks.put(taskId, task);
		}
		Throwable exception = null;
		try {
			task.run(context, taskId, now, scheduleParams);
		} catch (Throwable ex) {
			exception = ex;
		}
		synchronized (lock) {
			if (!scheduledTasks.containsKey(taskId))
				return;
			runningTasks.remove(taskId);
			if (exception == null) {
				logger.info(String.format(
						"Time schedule task \"%s\" (%s) completed",
						task.getName(), taskId));
			} else {
				logger.error(String.format(
						"Error in time schedule task \"%s\" (%s)",
						task.getName(), taskId) + ": " + exception.getMessage(),
						exception);
			}
			LocalDateTime start = new LocalDateTime();
			if (!start.isAfter(time))
				start = time.plusMillis(1);
			final LocalDateTime finalStart = start;
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					scheduleTimeSchedule(context, task, finalStart);
				}
			});
		}
	}

	/**
	 * Returns the next scheduled date/time at or after "start" according to a
	 * time schedule. If there is no next date/time, this method returns null.
	 *
	 * @param start the time from where to start searching
	 * @param timeSchedule the time schedule
	 * @return the next date/time or null
	 */
	private LocalDateTime getNextScheduledDateTime(LocalDateTime start,
			TaskSchedule.TimeSchedule timeSchedule) {
		LocalDate startDate = start.toLocalDate();
		LocalDate nextDate = getNextScheduledDate(startDate, timeSchedule);
		if (nextDate == null)
			return null;
		LocalTime startDayTime = new LocalTime(0, 0, 0);
		LocalTime fromTime = nextDate.isEqual(startDate) ? start.toLocalTime() :
				startDayTime;
		LocalTime nextTime = getNextScheduledTime(fromTime, timeSchedule);
		if (nextTime == null) {
			nextDate = getNextScheduledDate(startDate.plusDays(1),
					timeSchedule);
			if (nextDate == null)
				return null;
			nextTime = getNextScheduledTime(startDayTime, timeSchedule);
		}
		return nextDate.toLocalDateTime(nextTime);
	}

	/**
	 * Returns the next scheduled date at or after the specified date according
	 * to a time schedule. If there is no next date, this method returns null.
	 *
	 * @param date the date from where to start searching
	 * @param timeSchedule the time schedule
	 * @return the next date or null
	 */
	private LocalDate getNextScheduledDate(LocalDate date,
			TaskSchedule.TimeSchedule timeSchedule) {
		LocalDate startDate = timeSchedule.getStartDate();
		if (!date.isAfter(startDate))
			return startDate;
		DateDuration repeat = timeSchedule.getRepeatDate();
		if (repeat == null)
			return null;
		LocalDate nextDate;
		if (repeat.getUnit() == DateUnit.YEAR) {
			// get years rounded up
			int years = Years.yearsBetween(timeSchedule.getStartDate(),
					date.minusDays(1)).getYears() + 1;
			int repeatCount = repeat.getCount();
			// get iterations rounded up
			int it = (years + repeatCount - 1) / repeatCount;
			nextDate = startDate.plusYears(it * repeatCount);
		} else if (repeat.getUnit() == DateUnit.MONTH) {
			// get months rounded up
			int months = Months.monthsBetween(timeSchedule.getStartDate(),
					date.minusDays(1)).getMonths() + 1;
			int repeatCount = repeat.getCount();
			// get iterations rounded up
			int it = (months + repeatCount - 1) / repeatCount;
			nextDate = startDate.plusMonths(it * repeatCount);
		} else {
			int days = Days.daysBetween(timeSchedule.getStartDate(), date)
					.getDays();
			int repeatCount = repeat.getUnit() == DateUnit.WEEK ?
					7 * repeat.getCount() : repeat.getCount();
			// get iterations rounded up
			int it = (days + repeatCount - 1) / repeatCount;
			nextDate = startDate.plusDays(it * repeatCount);
		}
		LocalDate endDate = timeSchedule.getEndDate();
		if (endDate != null && !nextDate.isBefore(endDate))
			return null;
		return nextDate;
	}

	/**
	 * Returns the next scheduled time at or after the specified time according
	 * to a time schedule. If there is no next time, this method returns null.
	 * If the specified time is 0:00, the result should never be null.
	 *
	 * @param time the time from where to start searching
	 * @param timeSchedule the time schedule
	 * @return the next time or null
	 */
	private LocalTime getNextScheduledTime(LocalTime time,
			TaskSchedule.TimeSchedule timeSchedule) {
		LocalTime startTime = timeSchedule.getStartTime();
		if (!time.isAfter(startTime))
			return startTime;
		TimeDuration repeat = timeSchedule.getRepeatTime();
		if (repeat == null)
			return null;
		int startMs = startTime.getMillisOfDay();
		int dayEndMs = 86400000;
		int repeatMs = (int)repeat.getDuration();
		int intervalMs = time.getMillisOfDay() - startTime.getMillisOfDay();
		int it = (intervalMs + repeatMs - 1) / repeatMs;
		int nextMs = startMs + it * repeatMs;
		if (nextMs >= dayEndMs)
			return null;
		LocalTime nextTime = new LocalTime().withMillisOfDay(nextMs);
		LocalTime endTime = timeSchedule.getEndTime();
		if (endTime != null && !nextTime.isBefore(endTime))
			return null;
		return nextTime;
	}

	/**
	 * Schedules a task with schedule {@link TaskSchedule.LocalTime
	 * TaskSchedule.LocalTime}. In Android this method is called on the UI
	 * thread.
	 *
	 * @param context the context (only set in Android)
	 * @param task the task
	 */
	private void scheduleLocalTime(Object context, ScheduledTask task) {
		synchronized (lock) {
			String taskId = task.getId();
			if (!scheduledTasks.containsKey(taskId))
				return;
			TaskSchedule.LocalTime schedule =
					(TaskSchedule.LocalTime)task.getSchedule();
			LocalDateTime time = schedule.getTime();
			logger.info(String.format(
					"Schedule local time task \"%s\" (%s) at %s",
					task.getName(), taskId,
					time.toString("yyyy-MM-dd'T'HH:mm:ss.SSS")));
			ScheduleParams scheduleParams = new ScheduleParams(time,
					schedule.isExact());
			ScheduledTaskSpec taskSpec = new ScheduledTaskSpec(taskId, task,
					scheduleParams);
			scheduledTaskInstances.put(taskId, taskSpec);
			scheduleTask(context, taskSpec);
		}
	}

	/**
	 * Starts a task with schedule {@link TaskSchedule.LocalTime
	 * TaskSchedule.LocalTime}. In Android this method is called on the UI
	 * thread.
	 *
	 * @param context the context (only set in Android)
	 * @param task the task
	 * @param now the current time
	 * @param scheduleParams the scheduled time parameters
	 */
	private void startLocalTime(final Object context, final ScheduledTask task,
			final DateTime now, final ScheduleParams scheduleParams) {
		if (!canRunTaskOnMainThread() || task.isRunOnWorkerThread()) {
			new Thread() {
				@Override
				public void run() {
					runLocalTime(context, task, now, scheduleParams);
				}
			}.start();
		} else {
			runLocalTime(context, task, now, scheduleParams);
		}
	}

	/**
	 * Runs a task with schedule {@link TaskSchedule.LocalTime
	 * TaskSchedule.LocalTime}. In Android this method is called on a worker
	 * thread or on the UI thread depending on the result of {@link
	 * ScheduledTask#isRunOnWorkerThread() task.isRunOnWorkerThread()}.
	 *
	 * @param context the context (only set in Android)
	 * @param task the task
	 * @param now the current time
	 * @param scheduleParams the scheduled time parameters
	 */
	private void runLocalTime(Object context, ScheduledTask task, DateTime now,
			ScheduleParams scheduleParams) {
		String taskId = task.getId();
		synchronized (lock) {
			if (!scheduledTasks.containsKey(taskId))
				return;
			LocalDateTime time = scheduleParams.getLocalTime();
			logger.info(String.format(
					"Start local time task \"%s\" (%s) scheduled at %s",
					task.getName(), taskId,
					time.toString("yyyy-MM-dd'T'HH:mm:ss.SSS")));
			runningTasks.put(taskId, task);
		}
		Throwable exception = null;
		try {
			task.run(context, taskId, now, scheduleParams);
		} catch (Throwable ex) {
			exception = ex;
		}
		synchronized (lock) {
			if (!scheduledTasks.containsKey(taskId))
				return;
			runningTasks.remove(taskId);
			scheduledTasks.remove(taskId);
			if (exception == null) {
				logger.info(String.format(
						"Local time task \"%s\" (%s) completed",
						task.getName(), taskId));
			} else {
				logger.error(String.format(
						"Error in local time task \"%s\" (%s)",
						task.getName(), taskId) + ": " + exception.getMessage(),
						exception);
			}
		}
	}

	/**
	 * Schedules a task with schedule {@link TaskSchedule.UtcTime
	 * TaskSchedule.UtcTime}. In Android this method is called on the UI
	 * thread.
	 *
	 * @param context the context (only set in Android)
	 * @param task the task
	 */
	private void scheduleUtcTime(Object context, ScheduledTask task) {
		synchronized (lock) {
			String taskId = task.getId();
			if (!scheduledTasks.containsKey(taskId))
				return;
			TaskSchedule.UtcTime schedule =
					(TaskSchedule.UtcTime)task.getSchedule();
			DateTime time = schedule.getTime();
			logger.info(String.format(
					"Schedule UTC time task \"%s\" (%s) at %s",
					task.getName(), taskId,
					time.toString("yyyy-MM-dd'T'HH:mm:ss.SSSZZ")));
			ScheduleParams scheduleParams = new ScheduleParams(
					time.getMillis(), schedule.isExact());
			ScheduledTaskSpec taskSpec = new ScheduledTaskSpec(taskId, task,
					scheduleParams);
			scheduledTaskInstances.put(taskId, taskSpec);
			scheduleTask(context, taskSpec);
		}
	}

	/**
	 * Starts a task with schedule {@link TaskSchedule.UtcTime
	 * TaskSchedule.UtcTime}. In Android this method is called on the UI thread.
	 *
	 * @param context the context (only set in Android)
	 * @param task the task
	 * @param now the current time
	 * @param scheduleParams the scheduled time parameters
	 */
	private void startUtcTime(final Object context, final ScheduledTask task,
			final DateTime now, final ScheduleParams scheduleParams) {
		if (!canRunTaskOnMainThread() || task.isRunOnWorkerThread()) {
			new Thread() {
				@Override
				public void run() {
					runUtcTime(context, task, now, scheduleParams);
				}
			}.start();
		} else {
			runUtcTime(context, task, now, scheduleParams);
		}
	}

	/**
	 * Runs a task with schedule {@link TaskSchedule.UtcTime
	 * TaskSchedule.UtcTime}. In Android this method is called on a worker
	 * thread or on the UI thread depending on the result of {@link
	 * ScheduledTask#isRunOnWorkerThread() task.isRunOnWorkerThread()}.
	 *
	 * @param context the context (only set in Android)
	 * @param task the task
	 * @param now the current time
	 * @param scheduleParams the scheduled time parameters
	 */
	private void runUtcTime(Object context, ScheduledTask task, DateTime now,
			ScheduleParams scheduleParams) {
		String taskId = task.getId();
		synchronized (lock) {
			if (!scheduledTasks.containsKey(taskId))
				return;
			DateTime time = new DateTime(scheduleParams.getUtcTime());
			logger.info(String.format(
					"Start UTC time task \"%s\" (%s) scheduled at %s",
					task.getName(), taskId,
					time.toString("yyyy-MM-dd'T'HH:mm:ss.SSSZZ")));
			runningTasks.put(taskId, task);
		}
		Throwable exception = null;
		try {
			task.run(context, taskId, now, scheduleParams);
		} catch (Throwable ex) {
			exception = ex;
		}
		synchronized (lock) {
			if (!scheduledTasks.containsKey(taskId))
				return;
			runningTasks.remove(taskId);
			scheduledTasks.remove(taskId);
			if (exception == null) {
				logger.info(String.format("UTC time task \"%s\" (%s) completed",
						task.getName(), taskId));
			} else {
				logger.error(String.format("Error in UTC time task \"%s\" (%s)",
						task.getName(), taskId) + ": " + exception.getMessage(),
						exception);
			}
		}
	}

	/**
	 * Called when a scheduled task should be run. In Android this method is
	 * called on the UI thread.
	 *
	 * @param context the context (only set in Android)
	 * @param taskSpec the specification of the task instance
	 */
	public void onTriggerTask(Object context, ScheduledTaskSpec taskSpec) {
		synchronized (lock) {
			DateTime now = new DateTime();
			String taskId = taskSpec.getId();
			ScheduledTaskSpec scheduledSpec = scheduledTaskInstances.get(
					taskId);
			if (scheduledSpec == null || !scheduledSpec.equals(taskSpec)) {
				logger.info(String.format(
						"Scheduled task %s not found on trigger",
						getScheduledTaskSpecLog(taskSpec)));
				return;
			}
			logger.info("Start triggered task " +
					getScheduledTaskSpecLog(taskSpec));
			scheduledTaskInstances.remove(taskId);
			ScheduledTask task = scheduledTasks.get(taskId);
			TaskSchedule schedule = task.getSchedule();
			if (schedule instanceof TaskSchedule.FixedDelay) {
				startFixedDelay(context, task, now,
						taskSpec.getScheduleParams());
			} else if (schedule instanceof TaskSchedule.FixedRate) {
				startFixedRate(context, task, now,
						taskSpec.getScheduleParams());
			} else if (schedule instanceof TaskSchedule.TimeSchedule) {
				startTimeSchedule(context, task, now,
						taskSpec.getScheduleParams());
			} else if (schedule instanceof TaskSchedule.LocalTime) {
				startLocalTime(context, task, now,
						taskSpec.getScheduleParams());
			} else if (schedule instanceof TaskSchedule.UtcTime) {
				startUtcTime(context, task, now, taskSpec.getScheduleParams());
			}
		}
	}

	/**
	 * Tries to build a scheduled task from a class name, task ID and task data.
	 * In Android this method is called on the UI thread.
	 *
	 * @param context the context (only set in Android)
	 * @param className the class name
	 * @param taskId the task ID
	 * @param taskData the task data
	 * @return the scheduled task
	 * @throws HandledException if the task could not be constructed (an error
	 * has been logged)
	 */
	private ScheduledTask buildTask(Object context, String className,
			String taskId, String taskData) throws HandledException {
		Class<?> clazz;
		try {
			clazz = Class.forName(className);
		} catch (ClassNotFoundException ex) {
			logger.error("ScheduledTask class " + className + " not found: " +
					ex.getMessage());
			throw new HandledException();
		}
		Class<? extends ScheduledTask> taskClass;
		try {
			taskClass = clazz.asSubclass(ScheduledTask.class);
		} catch (ClassCastException ex) {
			logger.error("Class " + className + " is not a ScheduledTask: " +
					ex.getMessage());
			throw new HandledException();
		}
		ScheduledTask task = null;
		if (context != null) {
			try {
				task = ReflectionUtils.newInstance(taskClass, context);
			} catch (InstantiationException ex) {
			} catch (InvocationTargetException ex) {
			}
		}
		if (task == null) {
			try {
				task = ReflectionUtils.newInstance(taskClass);
			} catch (InstantiationException ex) {
				logger.error("Can't construct instance of " + className + ": " +
						ex.getMessage());
				throw new HandledException();
			} catch (InvocationTargetException ex) {
				logger.error("Exception in constructor of class " + className +
						": " + ex.getCause().getMessage());
				throw new HandledException();
			}
		}
		task.setId(taskId);
		try {
			task.setTaskData(taskData);
		} catch (ParseException ex) {
			logger.error("Can't parse task data for task class " +
					className + "\": " + ex.getMessage());
			throw new HandledException();
		}
		return task;
	}
}
