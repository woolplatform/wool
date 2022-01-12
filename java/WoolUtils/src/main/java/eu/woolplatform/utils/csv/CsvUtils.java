package eu.woolplatform.utils.csv;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;

import java.io.IOException;
import java.io.Writer;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class CsvUtils {
	public static void writeCsvHeader(Writer writer) throws IOException {
		String newline = System.getProperty("line.separator");
		writer.write("sep=;" + newline);
	}
	
	public static String valueToString(Object value) {
		DecimalFormatSymbols symbols = new DecimalFormatSymbols(
				Locale.getDefault());
		char decimal = symbols.getDecimalSeparator();
		if (value == null) {
			return "NULL";
		} else if (value instanceof Number) {
			String s = value.toString();
			int sep = s.indexOf('.');
			if (sep != -1)
				s = s.substring(0, sep) + decimal + s.substring(sep + 1);
			return s;
		} else if (value instanceof Boolean) {
			return value.toString();
		} else if (value instanceof LocalDate) {
			LocalDate date = (LocalDate)value;
			return date.toString("yyyy-MM-dd");
		} else if (value instanceof LocalTime) {
			LocalTime time = (LocalTime)value;
			return time.toString("HH:mm:ss");
		} else if (value instanceof LocalDateTime) {
			LocalDateTime time = (LocalDateTime)value;
			return time.toString("yyyy-MM-dd HH:mm:ss");
		} else if (value instanceof DateTime) {
			DateTime time = (DateTime)value;
			return time.toString("yyyy-MM-dd HH:mm:ss");
		} else {
			return '"' + value.toString().replaceAll("\"", "\"\"") + '"';
		}
	}
}
