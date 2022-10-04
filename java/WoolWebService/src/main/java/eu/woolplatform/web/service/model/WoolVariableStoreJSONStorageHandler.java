/*
 * Copyright 2019-2022 WOOL Foundation.
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
package eu.woolplatform.web.service.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import eu.woolplatform.utils.exception.ParseException;
import eu.woolplatform.utils.io.FileUtils;
import eu.woolplatform.utils.json.JsonMapper;
import eu.woolplatform.web.service.Configuration;
import eu.woolplatform.wool.execution.WoolUser;
import eu.woolplatform.wool.execution.WoolVariable;
import eu.woolplatform.wool.execution.WoolVariableStore;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * A {@link WoolVariableStoreJSONStorageHandler} can manage reading and writing
 * {@link eu.woolplatform.wool.execution.WoolVariableStore}s to and from JSON file representations. You can instantiate
 * an instance of a {@link WoolVariableStoreJSONStorageHandler} by providing a root dataDirectory. The storage handler
 * will assume/create a single {username}.json file for every WOOL User that will contain a JSON representation of the
 * WOOL Variable Store.
 *
 * @author Harm op den Akker
 */
public class WoolVariableStoreJSONStorageHandler implements WoolVariableStoreStorageHandler {

    private String dataDirectory;
    private static final Object LOCK = new Object();

    public WoolVariableStoreJSONStorageHandler(String dataDirectory) {
        this.dataDirectory = dataDirectory;
    }

    @Override
    public WoolVariableStore read(WoolUser woolUser) throws IOException, ParseException {
        synchronized (LOCK) {
            File dataDir = new File(dataDirectory);
            FileUtils.mkdir(dataDir);
            File dataFile = new File(dataDir, woolUser.getId() + ".json");
            if (!dataFile.exists())
                return new WoolVariableStore(woolUser);
            ObjectMapper mapper = new ObjectMapper();
            try {
                WoolVariable[] woolVariableArray = mapper.readValue(dataFile,
                        new TypeReference<WoolVariable[]>() {});
                return new WoolVariableStore(woolUser, woolVariableArray);
            } catch (JsonProcessingException ex) {
                throw new ParseException(
                        "Failed to parse variable store file: " +
                                dataFile.getAbsolutePath() + ": " + ex.getMessage(), ex);
            }
        }
    }

    @Override
    public void write(WoolVariableStore woolVariableStore) throws IOException {
        synchronized (LOCK) {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());
            objectMapper.configure(SerializationFeature.WRITE_DATES_WITH_CONTEXT_TIME_ZONE, true);

            File dataDir = new File(dataDirectory);
            FileUtils.mkdir(dataDir);
            File dataFile = new File(dataDir, woolVariableStore.getWoolUser().getId() + ".json");

            objectMapper.writeValue(dataFile,woolVariableStore);

            //String json = JsonMapper.generate(woolVariableStore.getWoolVariables());

            //FileUtils.writeFileString(dataFile, json);
        }
    }
}
