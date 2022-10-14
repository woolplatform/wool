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

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;

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
			ZonedDateTime time;
			ScheduleParams scheduleParams = taskSpec.getScheduleParams();
			if (scheduleParams.getLocalTime() != null) {
				time = DateTimeUtils.localToUtcWithGapCorrection(
						scheduleParams.getLocalTime(), ZoneId.systemDefault());
			} else {
				time = ZonedDateTime.ofInstant(Instant.ofEpochMilli(
						scheduleParams.getUtcTime()), ZoneId.systemDefault());
			}
			timer.schedule(new TimerTask() {
				@Override
				public void run() {
					runTask(taskId);
				}
			}, Date.from(time.toInstant()));
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
