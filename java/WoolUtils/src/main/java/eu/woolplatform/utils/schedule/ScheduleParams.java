package eu.woolplatform.utils.schedule;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import org.joda.time.LocalDateTime;

import eu.woolplatform.utils.json.LocalDateTimeDeserializer;
import eu.woolplatform.utils.json.LocalDateTimeSerializer;

/**
 * This class contains information about the scheduled time for one run of a
 * {@link ScheduledTask ScheduledTask}. It can contain a local time or a UTC
 * time. This depends on the {@link TaskSchedule TaskSchedule}.
 *
 * @author Dennis Hofs (RRD)
 */
public class ScheduleParams {

	@JsonSerialize(using=LocalDateTimeSerializer.class)
	@JsonDeserialize(using=LocalDateTimeDeserializer.class)
	private LocalDateTime localTime = null;
	private Long utcTime = null;
	private boolean exact;

	/**
	 * This default constructor is used for JSON serialization.
	 */
	public ScheduleParams() {
	}

	/**
	 * Constructs a new instance with a local time.
	 *
	 * @param localTime the local time
	 * @param exact true if the task should be run exactly at the specified
	 * time, false if it can be run later to save energy
	 */
	public ScheduleParams(LocalDateTime localTime, boolean exact) {
		this.localTime = localTime;
		this.exact = exact;
	}

	/**
	 * Constructs a new instance with a UTC time.
	 *
	 * @param utcTime the UTC time
	 * @param exact true if the task should be run exactly at the specified
	 * time, false if it can be run later to save energy
	 */
	public ScheduleParams(long utcTime, boolean exact) {
		this.utcTime = utcTime;
		this.exact = exact;
	}

	/**
	 * If this is a schedule with a local time, this method returns the time.
	 * Otherwise it returns null and {@link #getUtcTime() getUtcTime()} should
	 * return a UTC time.
	 *
	 * @return the local time or null
	 */
	public LocalDateTime getLocalTime() {
		return localTime;
	}

	/**
	 * Sets the local time. For a schedule with a UTC time, you should not call
	 * this method but use {@link #setUtcTime(Long) setUtcTime()}.
	 *
	 * @param localTime the local time
	 */
	public void setLocalTime(LocalDateTime localTime) {
		this.localTime = localTime;
	}

	/**
	 * If this is a schedule with a UTC time, this method returns the unix time
	 * in milliseconds. Otherwise it returns null and {@link #getLocalTime()
	 * getLocalTime()} should return a local time.
	 *
	 * @return the UTC time or null
	 */
	public Long getUtcTime() {
		return utcTime;
	}

	/**
	 * Sets the UTC time. For a schedule with a local time, you should not call
	 * this method but use {@link #setLocalTime(LocalDateTime) setLocalTime()}.
	 *
	 * @param utcTime the UTC time
	 */
	public void setUtcTime(Long utcTime) {
		this.utcTime = utcTime;
	}

	/**
	 * Returns whether the task should be run exactly at the specified time,
	 * or whether it can be run later to save energy.
	 *
	 * @return true if the task should be run exactly at the specified time,
	 * false if it can be run later to save energy
	 */
	public boolean isExact() {
		return exact;
	}

	/**
	 * Sets whether the task should be run exactly at the specified time, or
	 * whether it can be run later to save energy.
	 *
	 * @param exact true if the task should be run exactly at the specified
	 * time, false if it can be run later to save energy
	 */
	public void setExact(boolean exact) {
		this.exact = exact;
	}
}
