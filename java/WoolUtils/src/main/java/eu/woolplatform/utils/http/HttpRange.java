package eu.woolplatform.utils.http;

import eu.woolplatform.utils.exception.ParseException;

import java.util.ArrayList;
import java.util.List;

public class HttpRange {
	private String unit;
	private List<Interval> intervals = new ArrayList<>();

	public String getUnit() {
		return unit;
	}

	public void setUnit(String unit) {
		this.unit = unit;
	}

	public List<Interval> getIntervals() {
		return intervals;
	}

	public void setIntervals(List<Interval> intervals) {
		this.intervals = intervals;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder(unit + "=");
		for (Interval interval : intervals) {
			builder.append(interval.toString());
		}
		return builder.toString();
	}

	public static HttpRange parse(String value) throws ParseException {
		int sep = value.indexOf('=');
		if (sep == -1)
			throw new ParseException("Character = not found");
		String unit = value.substring(0, sep).trim();
		if (unit.length() == 0)
			throw new ParseException("Unit is empty");
		HttpRange result = new HttpRange();
		result.unit = unit;
		String intervals = value.substring(sep + 1).trim();
		if (intervals.length() == 0)
			throw new ParseException("Range value is empty");
		String[] intervalList = intervals.split(",");
		for (String interval : intervalList) {
			result.intervals.add(parseInterval(interval, intervalList.length));
		}
		return result;
	}

	private static Interval parseInterval(String interval, int intervalCount)
			throws ParseException {
		int sep = interval.indexOf('-');
		if (sep == -1)
			throw new ParseException("Found range without -");
		String startStr = interval.substring(0, sep).trim();
		String endStr = interval.substring(sep + 1).trim();
		Interval result = new Interval();
		if (startStr.length() > 0) {
			try {
				result.start = Long.parseLong(startStr);
			} catch (NumberFormatException ex) {
				throw new ParseException("Invalid start value: " + startStr);
			}
		}
		if (endStr.length() > 0) {
			try {
				result.end = Long.parseLong(endStr);
			} catch (NumberFormatException ex) {
				throw new ParseException("Invalid end value: " + endStr);
			}
		}
		if (result.start == null && result.end == null)
			throw new ParseException("Found range without start or end");
		if (result.start == null && intervalCount != 1) {
			throw new ParseException(
					"Found range without start in multi-range value");
		}
		return result;
	}

	public static class Interval {
		private Long start = null;
		private Long end = null;

		public Long getStart() {
			return start;
		}

		public void setStart(Long start) {
			this.start = start;
		}

		public Long getEnd() {
			return end;
		}

		public void setEnd(Long end) {
			this.end = end;
		}

		@Override
		public String toString() {
			String result = "";
			if (start != null)
				result += start;
			result += "-";
			if (end != null)
				result += end;
			return result;
		}
	}
}
