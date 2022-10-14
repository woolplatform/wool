package eu.woolplatform.utils.http;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class HttpDate {
	public static String generate(ZonedDateTime time) {
		DateTimeFormatter format = DateTimeFormatter.ofPattern(
				"EEE, dd MMM yyyy HH:mm:ss").withLocale(Locale.US);
		ZonedDateTime utcTime = time.withZoneSameInstant(ZoneOffset.UTC);
		return format.format(utcTime) + " GMT";
	}
}
