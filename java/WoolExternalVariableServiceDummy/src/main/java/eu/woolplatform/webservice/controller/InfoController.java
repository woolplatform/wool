package eu.woolplatform.webservice.controller;

import eu.woolplatform.webservice.Application;
import eu.woolplatform.webservice.Configuration;
import eu.woolplatform.webservice.ServiceContext;
import eu.woolplatform.webservice.controller.model.ServiceInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v{version}/info")
public class InfoController {

	@Autowired
	Application application;

	@GetMapping("/all")
	public ServiceInfo all() {
		return new ServiceInfo(Configuration.getInstance().get(Configuration.BUILD),
				ServiceContext.getCurrentVersion(),
				Configuration.getInstance().get(Configuration.VERSION));
	}

}
