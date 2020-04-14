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
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;

/**
 * Base class for a task schedule. This is part of a {@link ScheduledTask
 * ScheduledTask}.
 *
 * @author Dennis Hofs (RRD)
 */
public abstract class TaskSchedule {

	/**
	 * Schedule for a task that should be run once and start immediately.
	 */
	public static class Immediate extends TaskSchedule {
	}

	/**
	 * Schedule for a task that should be run immediately and then repeated
	 * indefinitely at the specified rate. This means that each run is started
	 * at time n * interval milliseconds after the start of the first task.
	 * An iteration may be skipped if a run takes longer than "interval"
	 * milliseconds.
	 */
	public static class FixedRate extends TaskSchedule {
		private long interval;

		/**
		 * Constructs a new fixed rate schedule.
		 *
		 * @param interval the interval in milliseconds
		 */
		public FixedRate(long interval) {
			this.interval = interval;
		}

		/**
		 * Returns the interval in milliseconds.
		 *
		 * @return the interval in milliseconds
		 */
		public long getInterval() {
			return interval;
		}
	}

	/**
	 * Schedule for a task that should be run immediately and then repeated
	 * indefinitely after the specified delay. This means that each run is
	 * started "delay" milliseconds after the previous run ended. The device
	 * may decide to run a task after a longer delay than specified to save
	 * energy.
	 */
	public static class FixedDelay extends TaskSchedule {
		private long delay;

		/**
		 * Constructs a new fixed delay schedule.
		 *
		 * @param delay the delay in milliseconds
		 */
		public FixedDelay(long delay) {
			this.delay = delay;
		}

		/**
		 * Returns the delay in milliseconds.
		 *
		 * @return the delay in milliseconds
		 */
		public long getDelay() {
			return delay;
		}
	}

	/**
	 * Schedule for a task that should be run at a specified date/time with
	 * possible repeats. The repeats can be specified by date and by time
	 * independently. An iteration may be skipped if a run doesn't end before
	 * the scheduled next run.
	 */
	public static class TimeSchedule extends TaskSchedule {
		private LocalDate startDate;
		private org.joda.time.LocalTime startTime;
		private LocalDate endDate = null;
		private org.joda.time.LocalTime endTime = null;
		private DateDuration repeatDate = null;
		private TimeDuration repeatTime = null;

		/**
		 * Constructs a new schedule that is run once at the specified
		 * date and time. After construction you can set additional properties
		 * to define repeats. In that case the specified date/time defines a
		 * start date/time. That is the time when the first run is scheduled.
		 *
		 * @param startDate the start date
		 * @param startTime the start time
		 */
		public TimeSchedule(LocalDate startDate,
				org.joda.time.LocalTime startTime) {
			this.startDate = startDate;
			this.startTime = startTime;
		}

		/**
		 * Returns the date when the task should be first run. The time is
		 * returned by {@link #getStartTime() getStartTime()}. If no date repeat
		 * is defined, this is the only date.
		 *
		 * @return the start date
		 */
		public LocalDate getStartDate() {
			return startDate;
		}

		/**
		 * Sets the date when the task should be first run. The time is set with
		 * {@link #setStartTime(org.joda.time.LocalTime) setStartTime()}. If no
		 * date repeat is defined, this is the only date.
		 *
		 * @param startDate the start date
		 */
		public void setStartDate(LocalDate startDate) {
			this.startDate = startDate;
		}

		/**
		 * Returns the time when the task should be first run on each scheduled
		 * date. If no time repeat is defined, this is the only time at each
		 * date.
		 *
		 * @return the start time
		 */
		public org.joda.time.LocalTime getStartTime() {
			return startTime;
		}

		/**
		 * Sets the time when the task should be first run on each scheduled
		 * date. If no time repeat is defined, this is the only time at each
		 * date.
		 *
		 * @param startTime the start time
		 */
		public void setStartTime(org.joda.time.LocalTime startTime) {
			this.startTime = startTime;
		}

		/**
		 * Returns the date at and after which the task should no longer be
		 * run. This can be used together with a date repeat. If there is no
		 * date repeat or the task should be repeated indefinitely, this
		 * method returns null.
		 *
		 * @return the end date or null (default)
		 */
		public LocalDate getEndDate() {
			return endDate;
		}

		/**
		 * Sets the date at and after which the task should no longer be run.
		 * This can be used together with a date repeat. If there is no date
		 * repeat or the task should be repeated indefinitely, the end date
		 * should be null.
		 *
		 * @param endDate the end date or null (default)
		 */
		public void setEndDate(LocalDate endDate) {
			this.endDate = endDate;
		}

		/**
		 * Returns the time at and after which the task should no longer be run
		 * at each scheduled date. This can be used together with a time repeat.
		 * If there is no time repeat or the task should be repeated
		 * indefinitely within each date, this method should return null.
		 *
		 * @return the end time or null (default)
		 */
		public org.joda.time.LocalTime getEndTime() {
			return endTime;
		}

		/**
		 * Sets the time at and after which the task should no longer be run at
		 * each scheduled date. This can be used together with a time repeat. If
		 * there is no time repeat or the task should be repeated indefinitely
		 * within each date, the end time should be null.
		 *
		 * @param endTime the end time or null (default)
		 */
		public void setEndTime(org.joda.time.LocalTime endTime) {
			this.endTime = endTime;
		}

		/**
		 * If the task should be repeated at multiple dates, this method returns
		 * the repeat interval. The task will be run at the start date and then
		 * at each interval after that until an optional end date (see {@link
		 * #getEndDate() getEndDate()}). At each date the task is run according
		 * to the defined time schedule (start time and possible repeats).
		 *
		 * <p>If the task should only run at the start date, this method
		 * returns null.</p>
		 *
		 * @return the date repeat interval or null
		 */
		public DateDuration getRepeatDate() {
			return repeatDate;
		}

		/**
		 * If the task should be repeated at multiple dates, this method sets
		 * the repeat interval. The task will be run at the start date and then
		 * at each interval after that until an optional end date (see {@link
		 * #setEndDate(LocalDate) setEndDate()}). At each date the task is run
		 * according to the defined time schedule (start time and possible
		 * repeats).
		 *
		 * <p>If the task should only run at the start date, you should set the
		 * repeat interval to null.</p>
		 *
		 * @param repeatDate the date repeat interval or null
		 */
		public void setRepeatDate(DateDuration repeatDate) {
			this.repeatDate = repeatDate;
		}

		/**
		 * If the task should be repeated at multiple times within each
		 * scheduled date, this method returns the repeat interval. At each
		 * date, the task will be run at the start time and then at each
		 * interval after that until an optional end time (see {@link
		 * #getEndTime() getEndTime()}).
		 *
		 * <p>If the task should only run at the start time within each
		 * scheduled date, this method returns null.</p>
		 *
		 * @return the time repeat interval or null
		 */
		public TimeDuration getRepeatTime() {
			return repeatTime;
		}

		/**
		 * If the task should be repeated at multiple times within each
		 * scheduled date, this method sets the repeat interval. At each date,
		 * the task will be run at the start time and then at each interval
		 * after that until an optional end time (see {@link
		 * #setEndTime(org.joda.time.LocalTime) setEndTime()}).
		 *
		 * <p>If the task should only run at the start time within each
		 * scheduled date, you should set the repeat interval to null.</p>
		 *
		 * @param repeatTime the time repeat interval or null
		 */
		public void setRepeatTime(TimeDuration repeatTime) {
			this.repeatTime = repeatTime;
		}
	}

	/**
	 * Schedule for a task that should run once at a specific local time.
	 */
	public static class LocalTime extends TaskSchedule {
		private LocalDateTime time;
		private boolean exact;

		/**
		 * Constructs a new local time schedule.
		 *
		 * @param time the time when the task should run
		 * @param exact true if the task should be run exactly at the specified
		 * time, false if it can be run later to save energy
		 */
		public LocalTime(LocalDateTime time, boolean exact) {
			this.time = time;
			this.exact = exact;
		}

		/**
		 * Returns the time when the task should run.
		 *
		 * @return the time when the task should run
		 */
		public LocalDateTime getTime() {
			return time;
		}

		/**
		 * Returns true if the task should be run exactly at the specified time,
		 * false if it can be run later to save energy.
		 *
		 * @return true if the task should be run exactly at the specified time,
		 * false if it can be run later to save energy
		 */
		public boolean isExact() {
			return exact;
		}
	}

	/**
	 * Schedule for a task that should run once at a specific UTC time.
	 */
	public static class UtcTime extends TaskSchedule {
		private DateTime time;
		private boolean exact;

		/**
		 * Constructs a new UTC time schedule.
		 *
		 * @param time the time when the task should run
		 * @param exact true if the task should be run exactly at the specified
		 * time, false if it can be run later to save energy
		 */
		public UtcTime(DateTime time, boolean exact) {
			this.time = time;
			this.exact = exact;
		}

		/**
		 * Returns the time when the task should run.
		 *
		 * @return the time when the task should run
		 */
		public DateTime getTime() {
			return time;
		}

		/**
		 * Returns true if the task should be run exactly at the specified time,
		 * false if it can be run later to save energy.
		 *
		 * @return true if the task should be run exactly at the specified time,
		 * false if it can be run later to save energy
		 */
		public boolean isExact() {
			return exact;
		}
	}
}
