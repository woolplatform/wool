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
