package eu.woolplatform.utils.http;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.Locale;

public class HttpDate {
	public static String generate(DateTime time) {
		DateTimeFormatter format = DateTimeFormat.forPattern(
				"EEE, dd MMM yyyy HH:mm:ss").withLocale(Locale.US);
		DateTime utcTime = time.withZone(DateTimeZone.UTC);
		return format.print(utcTime) + " GMT";
	}
}
