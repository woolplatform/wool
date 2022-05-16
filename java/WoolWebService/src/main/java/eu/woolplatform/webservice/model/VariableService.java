package eu.woolplatform.webservice.model;

import eu.woolplatform.webservice.Configuration;

public class VariableService {

    public String variableServiceDefined () {
        Configuration config = Configuration.getInstance();
        String varServiceUrl = config.get(Configuration.VARIABLE_SERVICE_URL);
        String varServiceToken = config.get(Configuration.VARIABLE_SERVICE_API_TOKEN);
        return "Service url: " + varServiceUrl + ", token: " + varServiceToken;
    }
}