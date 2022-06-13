package eu.woolplatform.webservice.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class WoolVariableResponseList {

	private List<WoolVariableResponse> woolVariableResponses;

	public WoolVariableResponseList() {
		woolVariableResponses = new ArrayList<>();
	}

	public WoolVariableResponseList(List<WoolVariableResponse> woolVariableResponses) {
		this.woolVariableResponses = woolVariableResponses;
	}

	public List<WoolVariableResponse> getWoolVariableResponses() {
		return woolVariableResponses;
	}

	public void setWoolVariableResponses(List<WoolVariableResponse> woolVariableResponses) {
		this.woolVariableResponses = woolVariableResponses;
	}

	public String toString() {
		String result = "WoolVariableResponseList [";
		for(WoolVariableResponse wvr : woolVariableResponses) {
			result += wvr.toString();
		}
		result += "]";
		return result;
	}

}
