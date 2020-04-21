package eu.woolplatform.webservice.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.woolplatform.utils.exception.ParseException;
import eu.woolplatform.utils.io.FileUtils;
import eu.woolplatform.utils.json.JsonMapper;
import eu.woolplatform.webservice.Configuration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class VariableStore {
	private static final Object LOCK = new Object();

	private static final String VARSTORE_DIR = "varstore";

	public static Map<String,?> readVariables(String user)
			throws ParseException, IOException {
		synchronized (LOCK) {
			Configuration config = Configuration.getInstance();
			File dataDir = new File(config.get(Configuration.DATA_DIR));
			dataDir = new File(dataDir, VARSTORE_DIR);
			FileUtils.mkdir(dataDir);
			File dataFile = new File(dataDir, user + ".json");
			if (!dataFile.exists())
				return new HashMap<>();
			ObjectMapper mapper = new ObjectMapper();
			try {
				return mapper.readValue(dataFile,
						new TypeReference<Map<String, ?>>() {});
			} catch (JsonProcessingException ex) {
				throw new ParseException(
						"Failed to parse variable store file: " +
						dataFile.getAbsolutePath() + ": " + ex.getMessage(),
						ex);
			}
		}
	}

	public static void writeVariables(String user, Map<String,?> vars)
			throws ParseException, IOException {
		synchronized (LOCK) {
			Map<String,Object> currVars = new HashMap<>(readVariables(user));
			currVars.putAll(vars);
			String json = JsonMapper.generate(currVars);
			Configuration config = Configuration.getInstance();
			File dataDir = new File(config.get(Configuration.DATA_DIR));
			dataDir = new File(dataDir, VARSTORE_DIR);
			FileUtils.mkdir(dataDir);
			File dataFile = new File(dataDir, user + ".json");
			FileUtils.writeFileString(dataFile, json);
		}
	}
}
