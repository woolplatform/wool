/*
 * Copyright 2019-2022 WOOL Foundation - Licensed under the MIT License:
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package eu.woolplatform.web.service.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.woolplatform.utils.exception.ParseException;
import eu.woolplatform.utils.io.FileUtils;
import eu.woolplatform.utils.json.JsonMapper;
import eu.woolplatform.web.service.Configuration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class VariableStoreIO {
	private static final Object LOCK = new Object();

	private static final String VARSTORE_DIR = "varstore";

	public static Map<String,?> readVariablez(String user)
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

	public static void writeVariablez(String user, Map<String,?> vars)
			throws ParseException, IOException {
		synchronized (LOCK) {
			Map<String,Object> currVars = new HashMap<>(readVariablez(user));
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
