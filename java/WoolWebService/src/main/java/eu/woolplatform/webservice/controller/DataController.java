package eu.woolplatform.webservice.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.woolplatform.utils.exception.ParseException;
import eu.woolplatform.utils.io.FileUtils;
import eu.woolplatform.utils.json.JsonMapper;
import eu.woolplatform.webservice.QueryRunner;
import eu.woolplatform.webservice.exception.*;
import eu.woolplatform.webservice.model.VariableStoreIO;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.util.*;

@RestController
@RequestMapping("/v{version}")
public class DataController {

	@RequestMapping(value="/variables", method=RequestMethod.GET)
	public Map<String,Object> getVariables(
			HttpServletRequest request,
			HttpServletResponse response,
			@PathVariable("version")
			@ApiIgnore
			String versionName,
			@RequestParam(value="names", required=false, defaultValue="")
			String names) throws HttpException, Exception {
		return QueryRunner.runQuery(
				(version, user) -> doGetVariables(user, names),
				versionName, request, response);
	}

	private Map<String,Object> doGetVariables(String user, String names)
			throws HttpException, Exception {
		Map<String,?> varStore = VariableStoreIO.readVariables(user);
		names = names.trim();
		List<String> nameList;
		if (names.length() == 0) {
			nameList = new ArrayList<>(varStore.keySet());
			Collections.sort(nameList);
		} else {
			List<String> invalidNames = new ArrayList<>();
			String[] nameArray = names.split("\\s+");
			for (String name : nameArray) {
				if (!name.matches("[A-Za-z][A-Za-z0-9_]*"))
					invalidNames.add(name);
			}
			if (!invalidNames.isEmpty()) {
				HttpFieldError error = new HttpFieldError("names",
						"Invalid variable names: " +
								String.join(", ", invalidNames));
				throw BadRequestException.withInvalidInput(error);
			}
			nameList = Arrays.asList(nameArray);
		}
		Map<String,Object> result = new LinkedHashMap<>();
		for (String name : nameList) {
			result.put(name, varStore.get(name));
		}
		return result;
	}

	@RequestMapping(value="/variable", method=RequestMethod.POST)
	public void setVariable(
			HttpServletRequest request,
			HttpServletResponse response,
			@PathVariable("version")
			@ApiIgnore
			String versionName,
			@RequestParam(value="name")
			String name,
			@RequestParam(value="value", required=false, defaultValue="")
			String value) throws HttpException, Exception {
		QueryRunner.runQuery((version, user) ->
				doSetVariable(request, user, name, value),
				versionName, request, response);
	}

	private Object doSetVariable(HttpServletRequest request, String user,
			String name, String value) throws HttpException, Exception {
		List<HttpFieldError> errors = new ArrayList<>();
		if (!name.matches("[A-Za-z][A-Za-z0-9_]*")) {
			errors.add(new HttpFieldError("name",
					"Invalid variable name: " + name));
		}
		if (!errors.isEmpty())
			throw BadRequestException.withInvalidInput(errors);
		String bodyString;
		try (InputStream input = request.getInputStream()) {
			bodyString = FileUtils.readFileString(input).trim();
		}
		Object setValue = value;
		if (!bodyString.isEmpty()) {
			Map<?,?> body;
			try {
				body = JsonMapper.parse(bodyString, Map.class);
			} catch (ParseException ex) {
				throw new BadRequestException("Invalid JSON content");
			}
			if (!body.containsKey("value")) {
				HttpFieldError error = new HttpFieldError("value",
						"Property \"value\" not found in content");
				throw BadRequestException.withInvalidInput(error);
			}
			setValue = body.get("value");
		}
		Map<String,Object> vars = new HashMap<>();
		vars.put(name, setValue);
		VariableStoreIO.writeVariables(user, vars);
		return null;
	}

	@RequestMapping(value="/variables", method=RequestMethod.POST)
	@ApiImplicitParams({
			@ApiImplicitParam(name="body", value="values", dataType="string", paramType="body")
	})
	public void setVariables(
			HttpServletRequest request,
			HttpServletResponse response,
			@PathVariable("version")
			@ApiIgnore
			String versionName) throws HttpException, Exception {
		QueryRunner.runQuery((version, user) -> doSetVariables(request, user),
				versionName, request, response);
	}

	private Object doSetVariables(HttpServletRequest request, String user)
			throws HttpException, Exception {
		InputStream input = request.getInputStream();
		Map<String, ?> varMap;
		try {
			ObjectMapper mapper = new ObjectMapper();
			varMap = mapper.readValue(input,
					new TypeReference<Map<String, ?>>() {
					});
		} catch (JsonProcessingException ex) {
			String msg = "Invalid input: " + ex.getMessage();
			HttpError error = new HttpError(ErrorCode.INVALID_INPUT, msg);
			throw new BadRequestException(error);
		} finally {
			input.close();
		}
		List<String> invalidNames = new ArrayList<>();
		for (String name : varMap.keySet()) {
			if (!name.matches("[A-Za-z][A-Za-z0-9_]*"))
				invalidNames.add(name);
		}
		if (!invalidNames.isEmpty()) {
			HttpError error = new HttpError(ErrorCode.INVALID_INPUT,
					"Invalid variable names: " +
					String.join(", ", invalidNames));
			throw new BadRequestException(error);
		}
		VariableStoreIO.writeVariables(user, varMap);
		return null;
	}
}
