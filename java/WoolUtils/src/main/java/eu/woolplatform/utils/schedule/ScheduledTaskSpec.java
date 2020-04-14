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

import eu.woolplatform.utils.json.JsonObject;

/**
 * Specification of one instance of a {@link ScheduledTask ScheduledTask}. The
 * specification contains information to identify the task and to rebuild the
 * {@link ScheduledTask ScheduledTask} if needed. See the discussion at {@link
 * TaskScheduler TaskScheduler} for the cases when this is done.
 *
 * @author Dennis Hofs (RRD)
 */
public class ScheduledTaskSpec extends JsonObject {
	private String id;
	private String name;
	private String className;
	private String taskData;
	private ScheduleParams scheduleParams;

	/**
	 * This default constructor is used for JSON serialization.
	 */
	public ScheduledTaskSpec() {
	}

	/**
	 * Constructs a new task specification.
	 *
	 * @param id the ID of the task instance
	 * @param task the task
	 * @param scheduleParams the schedule parameters
	 */
	public ScheduledTaskSpec(String id, ScheduledTask task,
			ScheduleParams scheduleParams) {
		this.id = id;
		this.name = task.getName();
		this.className = task.getClass().getName();
		this.taskData = task.getTaskData();
		this.scheduleParams = scheduleParams;
	}

	/**
	 * Constructs a new task specification.
	 *
	 * @param id the ID of the task instance
	 * @param name the task name (for logging)
	 * @param className the class name of the {@link ScheduledTask
	 * ScheduledTask}
	 * @param taskData the task data (can be null)
	 * @param scheduleParams the schedule parameters
	 */
	public ScheduledTaskSpec(String id, String name, String className,
			String taskData, ScheduleParams scheduleParams) {
		this.id = id;
		this.name = name;
		this.className = className;
		this.taskData = taskData;
		this.scheduleParams = scheduleParams;
	}

	/**
	 * Returns the ID of the task instance.
	 *
	 * @return the ID of the task instance
	 */
	public String getId() {
		return id;
	}

	/**
	 * Sets the ID of the task instance.
	 *
	 * @param id the ID of the task instance
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * Returns the task name. This is used for logging. See {@link
	 * ScheduledTask#getName() ScheduledTask.getName()}.
	 *
	 * @return the task name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the task name. This is used for logging. See {@link
	 * ScheduledTask#getName() ScheduledTask.getName()}.
	 *
	 * @param name the task name
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Returns the class name of the {@link ScheduledTask ScheduledTask}.
	 *
	 * @return the class name
	 */
	public String getClassName() {
		return className;
	}

	/**
	 * Sets the class name of the {@link ScheduledTask ScheduledTask}.
	 *
	 * @param className the class name
	 */
	public void setClassName(String className) {
		this.className = className;
	}

	/**
	 * Returns the task data. This may be used for rebuilding the {@link
	 * ScheduledTask ScheduledTask}. It can be null. See {@link
	 * ScheduledTask#getTaskData() ScheduledTask.getTaskData()}.
	 *
	 * @return the task data (can be null)
	 */
	public String getTaskData() {
		return taskData;
	}

	/**
	 * Sets the task data. This may be used for rebuilding the {@link
	 * ScheduledTask ScheduledTask}. It can be null. See {@link
	 * ScheduledTask#getTaskData() ScheduledTask.getTaskData()}.
	 *
	 * @param taskData the task data (can be null)
	 */
	public void setTaskData(String taskData) {
		this.taskData = taskData;
	}

	/**
	 * Returns the schedule parameters of the task instance.
	 *
	 * @return the schedule parameters of the task instance
	 */
	public ScheduleParams getScheduleParams() {
		return scheduleParams;
	}

	/**
	 * Sets the schedule parameters of the task instance.
	 *
	 * @param scheduleParams the schedule parameters of the task instance
	 */
	public void setScheduleParams(ScheduleParams scheduleParams) {
		this.scheduleParams = scheduleParams;
	}
}
