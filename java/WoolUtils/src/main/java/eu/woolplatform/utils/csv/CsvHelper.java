package eu.woolplatform.utils.csv;

import java.io.Closeable;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CsvHelper implements Closeable {
	private Writer writer;
	private String newline;
	
	public CsvHelper(Writer writer) {
		this.writer = writer;
		newline = System.getProperty("line.separator");
	}
	
	public void createHeaderRow(List<String> fields) throws IOException {
		writer.write("sep=;" + newline);
		boolean first = true;
		for (String field : fields) {
			if (!first)
				writer.write(";");
			else
				first = false;
			writer.write(field);
		}
		writer.write(newline);
	}

	public void createDataRow(List<Object> values) throws IOException {
		boolean first = true;
		for (Object value : values) {
			if (!first)
				writer.write(";");
			else
				first = false;
			writer.write(CsvUtils.valueToString(value));
		}
		writer.write(newline);
	}
	
	public void createDataRow(List<String> fields, Map<String,?> data)
			throws IOException {
		List<Object> values = new ArrayList<>();
		for (String field : fields) {
			values.add(data.get(field));
		}
		createDataRow(values);
	}

	@Override
	public void close() throws IOException {
		writer.close();
	}
}
