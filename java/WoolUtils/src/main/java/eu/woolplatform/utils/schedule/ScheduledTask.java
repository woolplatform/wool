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
import eu.woolplatform.utils.exception.TaskException;

import java.time.ZonedDateTime;

/**
 * This class defines a scheduled task that can be run with the {@link
 * TaskScheduler TaskScheduler}.
 *
 * <p>In some cases the {@link TaskScheduler TaskScheduler} needs to build a
 * copy of an instance that was passed earlier. This is done using a data string
 * (see {@link #getTaskData() getTaskData()} and {@link #setTaskData(String)
 * setTaskData()}). See the discussion at {@link TaskScheduler TaskScheduler}
 * for the cases when this is done.</p>
 *
 * @author Dennis Hofs (RRD)
 */
public interface ScheduledTask {

	/**
	 * Returns the task ID. This is set by the {@link TaskScheduler
	 * TaskScheduler} when the task is scheduled.
	 *
	 * @return the task ID
	 */
	String getId();


	/**
	 * Sets the task ID. This is set by the {@link TaskScheduler TaskScheduler}
	 * when the task is scheduled.
	 *
	 * @param id the task ID
	 */
	void setId(String id);

	/**
	 * Returns the name of this task. This is used for logging. It could be
	 * the simple name of the task class.
	 *
	 * @return the name
	 */
	String getName();

	/**
	 * Returns the task data that is needed to build a copy of this task
	 * instance. It could be a JSON string and it could also be null. See the
	 * discussion at {@link TaskScheduler TaskScheduler} for the cases when
	 * this data string is used.
	 *
	 * @return the task data
	 */
	String getTaskData();

	/**
	 * The {@link TaskScheduler TaskScheduler} may call this method when it
	 * needs to build a copy of an instance that was passed earlier. In that
	 * case it will construct an instance with the default constructor and then
	 * call this method. The specified data string was obtained from {@link
	 * #getTaskData() getTaskData()}. It could be a JSON string and it could
	 * also be null. See the discussion at {@link TaskScheduler TaskScheduler}
	 * for the cases when this is used.
	 *
	 * @param taskData the task data
	 * @throws ParseException if the data string can't be parsed
	 */
	void setTaskData(String taskData) throws ParseException;

	/**
	 * Returns the schedule at which the task should be run.
	 *
	 * @return the schedule at which the task should be run
	 */
	TaskSchedule getSchedule();

	/**
	 * Runs the task. This method is called by the {@link TaskScheduler
	 * TaskScheduler} at the scheduled times according to the schedule returned
	 * by {@link #getSchedule() getSchedule()}. With repeating tasks this method
	 * is called sequentially. If {@link #isRunOnWorkerThread()
	 * isRunOnWorkerThread()} returns true, it will run on a worker thread.
	 * Otherwise it will run on the scheduling thread (the UI thread in
	 * Android).
	 *
	 * @param context the context (only set in Android)
	 * @param taskId the task ID
	 * @param now the current time
	 * @param scheduleParams the scheduled time parameters
	 * @throws TaskException if an error occurs that should be logged
	 */
	void run(Object context, String taskId, ZonedDateTime now,
			ScheduleParams scheduleParams) throws TaskException;

	/**
	 * Returns whether the task should run on a worker thread or on the UI
	 * thread in Android. In standard Java this is ignored and a task is always
	 * run on a worker thread.
	 *
	 * @return true if the task should run on a worker thread, false if it
	 * should run on the UI thread
	 */
	boolean isRunOnWorkerThread();

	/**
	 * Cancels the task. This is called by the {@link TaskScheduler
	 * TaskScheduler} if the task was started and should be cancelled. In
	 * Android this method is called on the UI thread.
	 *
	 * @param context the context (only set in Android)
	 */
	void cancel(Object context);
}
