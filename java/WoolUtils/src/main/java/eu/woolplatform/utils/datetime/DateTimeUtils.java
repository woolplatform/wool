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

package eu.woolplatform.utils.datetime;

import eu.woolplatform.utils.exception.ParseException;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * This class contains various utility methods related to date and time.
 * 
 * @author Dennis Hofs (RRD)
 */
public class DateTimeUtils {
	
	/**
	 * Parses a date/time string and returns a date/time object of the
	 * specified class. We distinguish three types of date/time classes:
	 * UTC time, time with time zone, local date/time. The supported date/time
	 * classes are:
	 * 
	 * <p><ul>
	 * <li>long/Long (UNIX timestamp in milliseconds, UTC time)</li>
	 * <li>{@link Date Date} (UTC time)</li>
	 * <li>{@link Instant Instant} (UTC time)</li>
	 * <li>{@link Calendar Calendar} (with time zone)</li>
	 * <li>{@link ZonedDateTime ZonedDateTime} (with time zone)</li>
	 * <li>{@link LocalDate LocalDate}</li>
	 * <li>{@link LocalTime LocalTime}</li>
	 * <li>{@link LocalDateTime LocalDateTime}</li>
	 * </ul></p>
	 * 
	 * <p>It depends on the string format what class can be returned. This is
	 * detailed below. Supported string formats:</p>
	 *  
	 * <p><ul>
	 * <li>
	 * UNIX timestamp
	 * <p><ul>
	 * <li>long/Long, Date, Instant</li>
	 * <li>Calendar, ZonedDateTime: the timestamp is translated to the default
	 * time zone.</li>
	 * <li>LocalDate, LocalTime, LocalDateTime: the timestamp is translated to
	 * the default time zone before creating the local date/time.</li>
	 * </ul></p>
	 * </li>
	 * 
	 * <li>
	 * SQL date: yyyy-MM-dd
	 * <p><ul>
	 * <li>LocalDate</li>
	 * </ul></p>
	 * </li>
	 * 
	 * <li>
	 * SQL time: HH:mm:ss
	 * <p><ul>
	 * <li>LocalTime</li>
	 * </ul></p>
	 * </li>
	 * 
	 * <li>
	 * SQL datetime: yyyy-MM-dd HH:mm:ss
	 * <p><ul>
	 * <li>LocalDateTime</li>
	 * </ul></p>
	 * </li>
	 *
	 * <li>
	 * any ISO date/time with timezone accepted by {@link
	 * DateTimeFormatter#ISO_OFFSET_DATE_TIME ISO_OFFSET_DATE_TIME}
	 * <p><ul>
	 * <li>long/Long, Date, Instant. These classes store UTC times, so any
	 * specified time zone is eventually lost.</li>
	 * <li>Calendar, ZonedDateTime. The same as the UTC times except that any
	 * specified time zone is preserved in the result.</li>
	 * <li>LocalDate, LocalTime, LocalDateTime. Any specified time zone is
	 * ignored.</li>
	 * </ul></p>
	 * </li>
	 * 
	 * <li>
	 * any ISO date/time without timezone accepted by {@link
	 * DateTimeFormatter#ISO_LOCAL_DATE_TIME ISO_LOCAL_DATE_TIME}
	 * <p><ul>
	 * <li>long/Long, Date, Instant. The local date/time is interpreted with the
	 * default time zone. If the date/time does not exist in the time zone
	 * (because of a DST change), this method throws an exception. These classes
	 * store UTC times, so the time zone is not in the result.</li>
	 * <li>Calendar, ZonedDateTime. The same as the UTC times except that the
	 * default time zone is preserved in the result.</li>
	 * <li>LocalDate, LocalTime, LocalDateTime</li>
	 * </ul></p>
	 * </li>
	 * </ul></p>
	 * 
	 * @param dateTimeString the date/time string
	 * @param clazz the result class
	 * @param <T> the type of date/time to return
	 * @return the date/time with the specified class
	 * @throws ParseException if the date/time string is invalid, or a
	 * date/time without a time zone is parsed in a time zone where that
	 * date/time does not exist
	 */
	public static <T> T parseDateTime(String dateTimeString,
			Class<T> clazz) throws ParseException {
		// try long
		try {
			long timestamp = Long.parseLong(dateTimeString);
			return zonedDateTimeToType(Instant.ofEpochMilli(timestamp).atZone(
					ZoneId.systemDefault()), clazz);
		} catch (NumberFormatException ex) {}

		// try yyyy-MM-dd
		DateTimeFormatter parser = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		LocalDate localDate = null;
		try {
			localDate = parser.parse(dateTimeString, LocalDate::from);
		} catch (DateTimeParseException ex) {}
		try {
			if (localDate != null)
				return clazz.cast(localDate);
		} catch (ClassCastException ex) {
			throw new ParseException(
					"Pattern yyyy-MM-dd expects result class LocalDate, found: " +
					clazz.getName());
		}
		
		// try HH:mm:ss
		parser = DateTimeFormatter.ofPattern("HH:mm:ss");
		LocalTime localTime = null;
		try {
			localTime = parser.parse(dateTimeString, LocalTime::from);
		} catch (DateTimeParseException ex) {}
		try {
			if (localTime != null)
				return clazz.cast(localTime);
		} catch (ClassCastException ex) {
			throw new ParseException(
					"Pattern HH:mm:ss expects result class LocalTime, found: " +
					clazz.getName());
		}

		// try yyyy-MM-dd HH:mm:ss
		parser = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		LocalDateTime localDateTime = null;
		try {
			localDateTime = parser.parse(dateTimeString, LocalDateTime::from);
		} catch (DateTimeParseException ex) {}
		try {
			if (localDateTime != null)
				return clazz.cast(localDateTime);
		} catch (ClassCastException ex) {
			throw new ParseException(
					"Pattern yyyy-MM-dd HH:mm:ss expects result class LocalDateTime, found: " +
					clazz.getName());
		}

		// try ISO time with zone
		ZonedDateTime zonedDateTime = null;
		try {
			parser = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
			zonedDateTime = parser.parse(dateTimeString, ZonedDateTime::from);
		} catch (DateTimeParseException ex) {}
		try {
			if (zonedDateTime != null)
				return zonedDateTimeToType(zonedDateTime, clazz);
		} catch (IllegalArgumentException ex) {
			throw new ParseException("Invalid date/time target class: " +
					clazz.getName() + ": " + ex.getMessage(), ex);
		}
		// try ISO time without zone
		try {
			parser = DateTimeFormatter.ISO_LOCAL_DATE_TIME
					.withZone(ZoneId.systemDefault());
			localDateTime = parser.parse(dateTimeString, LocalDateTime::from);
		} catch (IllegalArgumentException ex) {
			throw new ParseException("Invalid date/time string: " +
					dateTimeString + ": " + ex.getMessage(), ex);
		}
		try {
			return localDateTimeToType(localDateTime, clazz);
		} catch (IllegalArgumentException ex) {
			throw new ParseException("Invalid date/time target class: " +
					clazz.getName() + ": " + ex.getMessage(), ex);
		}
	}

	/**
	 * Converts a {@link LocalDateTime LocalDateTime} object to an object of the
	 * specified class. It supports the following classes.
	 *
	 * <p><ul>
	 * <li>long/Long (UNIX timestamp in milliseconds): local time interpreted in
	 * default time zone, result is UTC time without specified time zone</li>
	 * <li>{@link Date Date}: local time interpreted in default time zone,
	 * result is UTC time without specified time zone</li>
	 * <li>{@link Instant Instant}: local time interpreted in default time zone,
	 * result is UTC time without specified time zone</li>
	 * <li>{@link Calendar Calendar}: local time interpreted in default time
	 * zone</li>
	 * <li>{@link ZonedDateTime ZonedDateTime}: local time interpreted in
	 * default time zone</li>
	 * <li>{@link LocalDate LocalDate}: time is ignored</li>
	 * <li>{@link LocalTime LocalTime}: date is ignored</li>
	 * <li>{@link LocalDateTime LocalDateTime}</li>
	 * </ul></p>
	 *
	 * @param dateTime the date/time
	 * @param clazz the result class
	 * @param <T> the type of date/time to return
	 * @return the date/time with the specified class
	 * @throws IllegalArgumentException if the target class is not supported
	 */
	public static <T> T localDateTimeToType(LocalDateTime dateTime,
			Class<T> clazz) throws IllegalArgumentException {
		if (clazz == Long.TYPE || clazz == Long.class) {
			ZonedDateTime zonedTime = tryLocalToZonedDateTime(dateTime,
					ZoneId.systemDefault());
			@SuppressWarnings("unchecked")
			T result = (T)Long.class.cast(zonedTime.toInstant().toEpochMilli());
			return result;
		} else if (clazz == Date.class) {
			ZonedDateTime zonedTime = tryLocalToZonedDateTime(dateTime,
					ZoneId.systemDefault());
			return clazz.cast(Date.from(zonedTime.toInstant()));
		} else if (clazz == Instant.class) {
			ZonedDateTime zonedTime = tryLocalToZonedDateTime(dateTime,
					ZoneId.systemDefault());
			return clazz.cast(zonedTime.toInstant());
		} else if (clazz == Calendar.class) {
			ZonedDateTime zonedTime = tryLocalToZonedDateTime(dateTime,
					ZoneId.systemDefault());
			return clazz.cast(GregorianCalendar.from(zonedTime));
		} else if (clazz == ZonedDateTime.class) {
			ZonedDateTime zonedTime = tryLocalToZonedDateTime(dateTime,
					ZoneId.systemDefault());
			return clazz.cast(zonedTime);
		} else if (clazz == LocalDate.class) {
			return clazz.cast(dateTime.toLocalDate());
		} else if (clazz == LocalTime.class) {
			return clazz.cast(dateTime.toLocalTime());
		} else if (clazz == LocalDateTime.class) {
			return clazz.cast(dateTime);
		} else {
			throw new IllegalArgumentException(
					"Unsupported date/time class: " + clazz.getName());
		}
	}

	/**
	 * Converts a {@link ZonedDateTime ZonedDateTime} object to an object of the
	 * specified class. It supports the following classes.
	 * 
	 * <p><ul>
	 * <li>long/Long (UNIX timestamp in milliseconds): translated to UTC time,
	 * time zone is lost</li>
	 * <li>{@link Date Date}: translated to UTC time, time zone is lost</li>
	 * <li>{@link Instant Instant}: translated to UTC time, time zone is
	 * lost</li>
	 * <li>{@link Calendar Calendar}</li>
	 * <li>{@link ZonedDateTime ZonedDateTime}</li>
	 * <li>{@link LocalDate LocalDate}: time and time zone is ignored</li>
	 * <li>{@link LocalTime LocalTime}: date and time zone is ignored</li>
	 * <li>{@link LocalDateTime LocalDateTime}: time zone is ignored</li>
	 * </ul></p>
	 * 
	 * @param dateTime the date/time
	 * @param clazz the result class
	 * @param <T> the type of date/time to return
	 * @return the date/time with the specified class
	 * @throws IllegalArgumentException if the target class is not supported
	 */
	public static <T> T zonedDateTimeToType(ZonedDateTime dateTime, Class<T> clazz)
			throws IllegalArgumentException {
		if (clazz == Long.TYPE || clazz == Long.class) {
			@SuppressWarnings("unchecked")
			T result = (T)Long.class.cast(dateTime.toInstant().toEpochMilli());
			return result;
		} else if (clazz == Date.class) {
			return clazz.cast(Date.from(dateTime.toInstant()));
		} else if (clazz == Instant.class) {
			return clazz.cast(dateTime.toInstant());
		} else if (clazz == Calendar.class) {
			return clazz.cast(GregorianCalendar.from(dateTime));
		} else if (clazz == ZonedDateTime.class) {
			return clazz.cast(dateTime);
		} else if (clazz == LocalDate.class) {
			return clazz.cast(dateTime.toLocalDate());
		} else if (clazz == LocalTime.class) {
			return clazz.cast(dateTime.toLocalTime());
		} else if (clazz == LocalDateTime.class) {
			return clazz.cast(dateTime.toLocalDateTime());
		} else {
			throw new IllegalArgumentException(
					"Unsupported date/time class: " + clazz.getName());
		}
	}

	private static ZonedDateTime tryLocalToZonedDateTime(
			LocalDateTime localDateTime, ZoneId tz) {
		ZonedDateTime zonedDateTime = localToUtcWithGapCorrection(localDateTime,
				tz);
		if (zonedDateTime.toLocalDateTime().isEqual(localDateTime))
			return zonedDateTime;
		String timeStr = localDateTime.format(DateTimeFormatter.ofPattern(
				"yyyy-MM-dd'T'HH:mm:ss.SSS"));
		throw new IllegalArgumentException("Local date/time " + timeStr +
				" does not exist in timezone " + tz.getId());
	}

	/**
	 * Converts the specified local date/time to a date/time in the specified
	 * time zone. If the local time is in a DST gap, it will add one hour. It
	 * could therefore occur in the next day.
	 *
	 * @param localDateTime the local date/time
	 * @param tz the time zone
	 * @return the date/time
	 */
	public static ZonedDateTime localToUtcWithGapCorrection(
			LocalDateTime localDateTime, ZoneId tz) {
		return localDateTime.atZone(tz);
	}
}
