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
	 * Tries to parse any date/time string and return a date/time object of the
	 * specified class. Below is a list of supported date/time string patterns
	 * and the respective result classes.
	 *
	 * <p><b>UNIX or epoch timestamp in milliseconds</b></p>
	 *
	 * <p>The string should be a <code>long</code> value. Supported result
	 * classes:</p>
	 *
	 * <p><ul>
	 * <li>long/Long (UNIX timestamp in milliseconds), {@link Instant Instant},
	 * {@link Date Date}: The timestamp as is.</li>
	 * <li>{@link ZonedDateTime ZonedDateTime}, {@link Calendar Calendar}: The
	 * timestamp is translated to the default timezone.</li>
	 * <li>{@link LocalDateTime LocalDateTime}, {@link LocalDate LocalDate},
	 * {@link LocalTime LocalTime}: The timestamp is translated to the default
	 * timezone to get the local date/time. The timezone is not in the
	 * result.</li>
	 * </ul></p>
	 *
	 * <p><b>ISO date</b></p>
	 *
	 * <p>A string like "2022-10-17". The result class must be {@link LocalDate
	 * LocalDate}.</p>
	 *
	 * <p><b>ISO time</b></p>
	 *
	 * <p>A string like "16:45:23.768". The seconds and milliseconds are
	 * optional. The result class must be {@link LocalTime LocalTime}.</p>
	 *
	 * <p><b>SQL date/time</b></p>
	 *
	 * <p>A string like "2022-10-17 16:45:23". Supported result classes:</p>
	 *
	 * <p><ul>
	 * <li>{@link LocalDateTime LocalDateTime}. The specified date/time as
	 * is.</li>
	 * <li>{@link LocalDate LocalDate}. The time is ignored.
	 * <li>{@link LocalTime LocalTime}. The date is ignored.</li>
	 * <li>{@link ZonedDateTime ZonedDateTime}, {@link Calendar Calendar}. The
	 * local date/time is interpreted with the default timezone. If the
	 * date/time does not exist in the timezone (because of a DST change), then
	 * this method throws an exception.</li>
	 * <li>long/Long (UNIX timestamp in milliseconds), {@link Instant Instant},
	 * {@link Date Date}. The same as above, but these classes store UTC times,
	 * so the timezone is not in the result.</li>
	 * </ul></p>
	 *
	 * <p><b>ISO date/time with timezone</b></p>
	 *
	 * <p>The string should contain at least a date, hours, minutes and a
	 * timezone. It may also contain seconds and milliseconds. Example:
	 * "2022-10-17T16:45:23.768+01:00". Supported result classes:</p>
	 *
	 * <p><ul>
	 * <li>{@link ZonedDateTime ZonedDateTime}, {@link Calendar Calendar}. The
	 * specified date/time as is.</li>
	 * <li>long/Long (UNIX timestamp in milliseconds), {@link Instant Instant},
	 * {@link Date Date}: These classes store UTC times, so the specified
	 * timezone is not in the result.</li>
	 * <li>{@link LocalDateTime LocalDateTime}. The timezone is ignored.</li>
	 * <li>{@link LocalDate LocalDate}. The time and timezone are ignored.</li>
	 * <li>{@link LocalTime LocalTime}. The date and timezone are ignored.</li>
	 * </ul></p>
	 *
	 * <p><b>ISO date/time without timezone</b></p>
	 *
	 * <p>The string should contain at least a date, hours and minutes. It may
	 * also contain seconds and milliseconds. Example:
	 * "2022-10-17T16:45:23.768". Supported result classes:</p>
	 *
	 * <p><ul>
	 * <li>{@link LocalDateTime LocalDateTime}. The specified date/time as
	 * is.</li>
	 * <li>{@link LocalDate LocalDate}. The time is ignored.
	 * <li>{@link LocalTime LocalTime}. The date is ignored.</li>
	 * <li>{@link ZonedDateTime ZonedDateTime}, {@link Calendar Calendar}. The
	 * local date/time is interpreted with the default timezone. If the
	 * date/time does not exist in the timezone (because of a DST change), then
	 * this method throws an exception.</li>
	 * <li>long/Long (UNIX timestamp in milliseconds), {@link Instant Instant},
	 * {@link Date Date}. The same as above, but these classes store UTC times,
	 * so the timezone is not in the result.</li>
	 * </ul></p>
	 *
	 * @param dateTimeString the date/time string
	 * @param clazz the result class
	 * @return the date/time with the specified class
	 * @param <T> the result class
	 * @throws ParseException if the date/time string is invalid, or a
	 * date/time is parsed in a timezone where that date/time does not exist
	 */
	public static <T> T parseDateTime(String dateTimeString, Class<T> clazz)
			throws ParseException {
		try {
			return parseEpochMillis(dateTimeString, clazz);
		} catch (ParseException ex) {}
		LocalDate date = null;
		try {
			date = parseDate(dateTimeString);
		} catch (ParseException ex) {}
		try {
			if (date != null)
				return clazz.cast(date);
		} catch (ClassCastException ex) {
			throw new ParseException(
					"Expected result class LocalDate for specified date/time string, found: " +
					clazz.getName());
		}
		LocalTime time = null;
		try {
			time = parseIsoTime(dateTimeString);
		} catch (ParseException ex) {}
		try {
			if (time != null)
				return clazz.cast(time);
		} catch (ClassCastException ex) {
			throw new ParseException(
					"Expected result class LocalTime for specified date/time string, found: " +
					clazz.getName());
		}
		try {
			return parseSqlDateTime(dateTimeString, clazz);
		} catch (ParseException ex) {}
		return parseIsoDateTime(dateTimeString, clazz);
	}

	/**
	 * Parses a <code>long</code> value as a UNIX or epoch timestamp in
	 * milliseconds and returns a date/time object of the specified class. The
	 * supported result classes:
	 *
	 * <p><ul>
	 * <li>long/Long (UNIX timestamp in milliseconds), {@link Instant Instant},
	 * {@link Date Date}: The timestamp as is.</li>
	 * <li>{@link ZonedDateTime ZonedDateTime}, {@link Calendar Calendar}: The
	 * timestamp is translated to the default timezone.</li>
	 * <li>{@link LocalDateTime LocalDateTime}, {@link LocalDate LocalDate},
	 * {@link LocalTime LocalTime}: The timestamp is translated to the default
	 * timezone to get the local date/time. The timezone is not in the
	 * result.</li>
	 * </ul></p>
	 *
	 * @param longString the string with the <code>long</code> value
	 * @param clazz the result class
	 * @param <T> the result class
	 * @return the date/time with the specified class
	 * @throws ParseException if the string is invalid
	 */
	public static <T> T parseEpochMillis(String longString, Class<T> clazz)
			throws ParseException {
		long timestamp;
		try {
			timestamp = Long.parseLong(longString);
		} catch (NumberFormatException ex) {
			throw new ParseException("Invalid epoch string: " + longString);
		}
		return zonedDateTimeToType(Instant.ofEpochMilli(timestamp).atZone(
				ZoneId.systemDefault()), clazz);
	}

	/**
	 * Parses an ISO date string like "2022-10-17" and returns a LocalDate
	 * object.
	 *
	 * @param dateString the date string
	 * @return the LocalDate object
	 * @throws ParseException if the date string is invalid
	 */
	public static LocalDate parseDate(String dateString) throws ParseException {
		DateTimeFormatter parser = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		try {
			return parser.parse(dateString, LocalDate::from);
		} catch (DateTimeParseException ex) {
			throw new ParseException(
					"Invalid date string for pattern yyyy-MM-dd: " +
					dateString);
		}
	}

	/**
	 * Parses an SQL time string like "16:45:23" and returns a LocalTime object.
	 *
	 * @param timeString the time string
	 * @return the LocalTime object
	 * @throws ParseException if the time string is invalid
	 */
	public static LocalTime parseSqlTime(String timeString)
			throws ParseException {
		DateTimeFormatter parser = DateTimeFormatter.ofPattern("HH:mm:ss");
		try {
			return parser.parse(timeString, LocalTime::from);
		} catch (DateTimeParseException ex) {
			throw new ParseException(
					"Invalid time string for pattern HH:mm:ss: " + timeString);
		}
	}

	/**
	 * Parses an ISO time string like "16:45:23.768" and returns a LocalTime
	 * object. The seconds and milliseconds are optional.
	 *
	 * @param timeString the time string
	 * @return the LocalTime object
	 * @throws ParseException if the time string is invalid
	 */
	public static LocalTime parseIsoTime(String timeString)
			throws ParseException {
		DateTimeFormatter parser = DateTimeFormatter.ofPattern(
				"HH:mm[:ss][.SSS]");
		try {
			return parser.parse(timeString, LocalTime::from);
		} catch (DateTimeParseException ex) {
			throw new ParseException("Invalid ISO time string: " + timeString);
		}
	}

	/**
	 * Parses an SQL date/time string like "2022-10-17 16:45:23" and returns a
	 * date/time object of the specified class. The supported result classes:
	 *
	 * <p><ul>
	 * <li>{@link LocalDateTime LocalDateTime}. The specified date/time as
	 * is.</li>
	 * <li>{@link LocalDate LocalDate}. The time is ignored.
	 * <li>{@link LocalTime LocalTime}. The date is ignored.</li>
	 * <li>{@link ZonedDateTime ZonedDateTime}, {@link Calendar Calendar}. The
	 * local date/time is interpreted with the default timezone. If the
	 * date/time does not exist in the timezone (because of a DST change), then
	 * this method throws an exception.</li>
	 * <li>long/Long (UNIX timestamp in milliseconds), {@link Instant Instant},
	 * {@link Date Date}. The same as above, but these classes store UTC times,
	 * so the timezone is not in the result.</li>
	 * </ul></p>
	 *
	 * @param dateTimeString the date/time string
	 * @param clazz the result class
	 * @param <T> the result class
	 * @return the date/time with the specified class
	 * @throws ParseException if the date/time string is invalid, or the
	 * date/time is parsed in a timezone where that date/time does not exist
	 */
	public static <T> T parseSqlDateTime(String dateTimeString, Class<T> clazz)
			throws ParseException {
		DateTimeFormatter parser = DateTimeFormatter.ofPattern(
				"yyyy-MM-dd HH:mm:ss");
		LocalDateTime localDateTime;
		try {
			localDateTime = parser.parse(dateTimeString, LocalDateTime::from);
		} catch (DateTimeParseException ex) {
			throw new ParseException(
					"Invalid date/time string for pattern yyyy-MM-dd HH:mm:ss: " +
					dateTimeString);
		}
		return localDateTimeToType(localDateTime, clazz);
	}

	/**
	 * Parses a local ISO date/time string and returns a date/time object of the
	 * specified class. The date/time string should contain at least a date,
	 * hours and minutes. It may also contain seconds and milliseconds, but no
	 * timezone. Example: "2022-10-17T16:45:23.768". The supported result
	 * classes:
	 *
	 * <p><ul>
	 * <li>{@link LocalDateTime LocalDateTime}. The specified date/time as
	 * is.</li>
	 * <li>{@link LocalDate LocalDate}. The time is ignored.
	 * <li>{@link LocalTime LocalTime}. The date is ignored.</li>
	 * <li>{@link ZonedDateTime ZonedDateTime}, {@link Calendar Calendar}. The
	 * local date/time is interpreted with the default timezone. If the
	 * date/time does not exist in the timezone (because of a DST change), then
	 * this method throws an exception.</li>
	 * <li>long/Long (UNIX timestamp in milliseconds), {@link Instant Instant},
	 * {@link Date Date}. The same as above, but these classes store UTC times,
	 * so the timezone is not in the result.</li>
	 * </ul></p>
	 *
	 * @param dateTimeString the date/time string
	 * @param clazz the result class
	 * @param <T> the result class
	 * @return the date/time with the specified class
	 * @throws ParseException if the date/time string is invalid, or a
	 * date/time without a timezone is parsed in a timezone where that date/time
	 * does not exist
	 */
	public static <T> T parseLocalIsoDateTime(String dateTimeString,
			Class<T> clazz) throws ParseException {
		DateTimeFormatter parser = DateTimeFormatter.ofPattern(
				"yyyy-MM-dd'T'HH:mm[:ss][.SSS]");
		LocalDateTime localDateTime;
		try {
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
	 * Parses an ISO date/time string and returns a date/time object of the
	 * specified class. The date/time string should contain at least a date,
	 * hours and minutes. It may also contain seconds, milliseconds and a
	 * timezone. Example: "2022-10-17T16:45:23.768+01:00". The supported result
	 * classes are listed below. The result depends on whether a timezone is
	 * specified or not.
	 *
	 * <p><b>ISO date/time with timezone</b></p>
	 *
	 * <p><ul>
	 * <li>{@link ZonedDateTime ZonedDateTime}, {@link Calendar Calendar}. The
	 * specified date/time as is.</li>
	 * <li>long/Long (UNIX timestamp in milliseconds), {@link Instant Instant},
	 * {@link Date Date}: These classes store UTC times, so the specified
	 * timezone is not in the result.</li>
	 * <li>{@link LocalDateTime LocalDateTime}. The timezone is ignored.</li>
	 * <li>{@link LocalDate LocalDate}. The time and timezone are ignored.</li>
	 * <li>{@link LocalTime LocalTime}. The date and timezone are ignored.</li>
	 * </ul></p>
	 *
	 * <p><b>ISO date/time without timezone</b></p>
	 *
	 * <p><ul>
	 * <li>{@link LocalDateTime LocalDateTime}. The specified date/time as
	 * is.</li>
	 * <li>{@link LocalDate LocalDate}. The time is ignored.
	 * <li>{@link LocalTime LocalTime}. The date is ignored.</li>
	 * <li>{@link ZonedDateTime ZonedDateTime}, {@link Calendar Calendar}. The
	 * local date/time is interpreted with the default timezone. If the
	 * date/time does not exist in the timezone (because of a DST change), then
	 * this method throws an exception.</li>
	 * <li>long/Long (UNIX timestamp in milliseconds), {@link Instant Instant},
	 * {@link Date Date}. The same as above, but these classes store UTC times,
	 * so the timezone is not in the result.</li>
	 * </ul></p>
	 *
	 * @param dateTimeString the date/time string
	 * @param clazz the result class
	 * @param <T> the result class
	 * @return the date/time with the specified class
	 * @throws ParseException if the date/time string is invalid, or a
	 * date/time without a timezone is parsed in a timezone where that date/time
	 * does not exist
	 */
	public static <T> T parseIsoDateTime(String dateTimeString, Class<T> clazz)
			throws ParseException {
		// try ISO date/time with zone like +01:00
		DateTimeFormatter parser = DateTimeFormatter.ofPattern(
				"yyyy-MM-dd'T'HH:mm[:ss][.SSS]XXX");
		ZonedDateTime zonedDateTime = null;
		try {
			zonedDateTime = parser.parse(dateTimeString, ZonedDateTime::from);
		} catch (DateTimeParseException ex) {}
		try {
			if (zonedDateTime != null)
				return zonedDateTimeToType(zonedDateTime, clazz);
		} catch (IllegalArgumentException ex) {
			throw new ParseException("Invalid date/time target class: " +
					clazz.getName() + ": " + ex.getMessage(), ex);
		}
		// try ISO date/time with zone like +01 or +0100
		parser = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm[:ss][.SSS]X");
		try {
			zonedDateTime = parser.parse(dateTimeString, ZonedDateTime::from);
		} catch (DateTimeParseException ex) {}
		try {
			if (zonedDateTime != null)
				return zonedDateTimeToType(zonedDateTime, clazz);
		} catch (IllegalArgumentException ex) {
			throw new ParseException("Invalid date/time target class: " +
					clazz.getName() + ": " + ex.getMessage(), ex);
		}
		// try ISO date/time without zone
		return parseLocalIsoDateTime(dateTimeString, clazz);
	}

	/**
	 * Converts a {@link LocalDateTime LocalDateTime} object to an object of the
	 * specified class. It supports the following classes.
	 *
	 * <p><ul>
	 * <li>{@link LocalDateTime LocalDateTime}: The date/time as is.</li>
	 * <li>{@link LocalDate LocalDate}: The time is ignored</li>
	 * <li>{@link LocalTime LocalTime}: The date is ignored</li>
	 * <li>{@link ZonedDateTime ZonedDateTime}, {@link Calendar Calendar}: The
	 * local time interpreted with the default time zone. If the date/time does
	 * not exist in the timezone (because of a DST change), then this method
	 * throws an exception.</li>
	 * <li>long/Long (UNIX timestamp in milliseconds), {@link Instant Instant},
	 * {@link Date Date}: The same as above, but these classes store UTC times,
	 * so the timezone is not in the result.</li>
	 * </ul></p>
	 *
	 * @param dateTime the date/time
	 * @param clazz the result class
	 * @param <T> the result class
	 * @return the date/time with the specified class
	 * @throws IllegalArgumentException if the local date/time is interpreted in
	 * a timezone where that date/time does not exist, or if the target class is
	 * not supported
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
	 * <li>{@link ZonedDateTime ZonedDateTime}, {@link Calendar Calendar}: The
	 * specified date/time as is.</li>
	 * <li>long/Long (UNIX timestamp in milliseconds), {@link Instant Instant},
	 * {@link Date Date}: The date/time is translated to UTC time, the timezone
	 * is lost.</li>
	 * <li>{@link LocalDateTime LocalDateTime}: The time zone is ignored.</li>
	 * <li>{@link LocalDate LocalDate}: The time and timezone are ignored.</li>
	 * <li>{@link LocalTime LocalTime}: The date and timezone are ignored.</li>
	 * </ul></p>
	 * 
	 * @param dateTime the date/time
	 * @param clazz the result class
	 * @param <T> the result class
	 * @return the date/time with the specified class
	 * @throws IllegalArgumentException if the target class is not supported
	 */
	public static <T> T zonedDateTimeToType(ZonedDateTime dateTime,
			Class<T> clazz) throws IllegalArgumentException {
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

	/**
	 * Tries to convert the specified local date/time to a zoned date/time in
	 * the specified timezone. If the local date/time does not exist in that
	 * timezone (because of a DST change), then this method throws an
	 * {@link IllegalArgumentException IllegalArgumentException}.
	 *
	 * @param localDateTime the local date/time
	 * @param tz the timezone
	 * @return the zoned date/time
	 * @throws IllegalArgumentException if the local date/time does not exist
	 * in the specified timezone (because of a DST change)
	 */
	public static ZonedDateTime tryLocalToZonedDateTime(
			LocalDateTime localDateTime, ZoneId tz)
			throws IllegalArgumentException {
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
	 * Converts the specified local date/time to a zoned date/time in the
	 * specified timezone. If the local time is in a DST gap, it will add one
	 * hour. It could therefore occur in the next day.
	 *
	 * @param localDateTime the local date/time
	 * @param tz the timezone
	 * @return the zoned date/time
	 */
	public static ZonedDateTime localToUtcWithGapCorrection(
			LocalDateTime localDateTime, ZoneId tz) {
		return localDateTime.atZone(tz);
	}
}
