package eu.woolplatform.web.varservice;

import eu.woolplatform.utils.AppComponents;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.util.ContentCachingRequestWrapper;

import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Enumeration;
import java.util.stream.Collectors;

@Component
public class RequestInterceptor implements HandlerInterceptor {

	private final Logger logger = AppComponents.getLogger(getClass().getSimpleName());

	@Override
	public boolean preHandle(
			HttpServletRequest request,
			HttpServletResponse response,
			Object handler) throws Exception {

		logger.info("[preHandle][" + request + "]" + "[" + request.getMethod()
				+ "]" + request.getRequestURI() + getParameters(request));

		//logRequestBody(request); // This breaks things as the buffer can only be read once.

		return true;
	}

	private void logRequestBody(HttpServletRequest request) throws IOException {
		ServletRequest servletRequest = new ContentCachingRequestWrapper(request);
		servletRequest.getParameterMap();
		ServletInputStream inputStream = servletRequest.getInputStream();

		String result = new BufferedReader(new InputStreamReader(inputStream))
				.lines().parallel().collect(Collectors.joining("\n"));

		logger.info(result);
	}

	@Override
	public void postHandle(
			HttpServletRequest request,
			HttpServletResponse response,
			Object handler,
			ModelAndView modelAndView) throws Exception {
		// your code
	}

	private String getParameters(HttpServletRequest request) {
		StringBuffer posted = new StringBuffer();
		Enumeration<?> e = request.getParameterNames();
		if (e != null) {
			posted.append("?");
		}
		while (e.hasMoreElements()) {
			if (posted.length() > 1) {
				posted.append("&");
			}
			String curr = (String) e.nextElement();
			posted.append(curr + "=");
			if (curr.contains("password")
					|| curr.contains("pass")
					|| curr.contains("pwd")) {
				posted.append("*****");
			} else {
				posted.append(request.getParameter(curr));
			}
		}
		String ip = request.getHeader("X-FORWARDED-FOR");
		String ipAddr = (ip == null) ? getRemoteAddr(request) : ip;
		if (ipAddr!=null && !ipAddr.equals("")) {
			posted.append("&_psip=" + ipAddr);
		}
		return posted.toString();
	}

	private String getRemoteAddr(HttpServletRequest request) {
		String ipFromHeader = request.getHeader("X-FORWARDED-FOR");
		if (ipFromHeader != null && ipFromHeader.length() > 0) {
			logger.info("ip from proxy - X-FORWARDED-FOR : " + ipFromHeader);
			return ipFromHeader;
		}
		return request.getRemoteAddr();
	}


}
