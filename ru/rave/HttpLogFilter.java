package ru.rave;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.Enumeration;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@WebFilter(urlPatterns = "/*")
public class HttpLogFilter implements Filter {
	
	private static final Logger log = LoggerFactory.getLogger(HttpLogFilter.class);
	private static final String HEADER_VALUES_SEPARATOR = "|";

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {}

	@Override
	public void destroy() {}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		if (log.isDebugEnabled()) {
			LoggingHttpServletRequestWrapper requestWrapper = new LoggingHttpServletRequestWrapper((HttpServletRequest)request);
			LoggingHttpServletResponseWrapper responseWrapper = new LoggingHttpServletResponseWrapper((HttpServletResponse) response);
			logRequest(requestWrapper);
			chain.doFilter(
					requestWrapper, 
					responseWrapper
			);
			logResponse(responseWrapper);
		} else {
			chain.doFilter(request,response);
		}
	}

	private void logRequest(LoggingHttpServletRequestWrapper request) {
		log.debug(">> {} {}{}", request.getMethod(), request.getRequestURI(), request.getQueryString() != null && !request.getQueryString().trim().equalsIgnoreCase("null") ? 
				new StringBuilder("?").append(request.getQueryString()).toString() : "");
		Enumeration<String> headers = request.getHeaderNames();
		while (headers.hasMoreElements()) {
			String header = headers.nextElement();
			log.debug(">> {} : {}", header, enumerationToString(request.getHeaders(header)));
		}
		logBody(">> {}", request.getBody());
	}
	
	private void logResponse(LoggingHttpServletResponseWrapper response) {
		log.debug("<< {}", response.getStatus());
		Collection<String> headers = response.getHeaderNames();
		for (String header : headers) {
			log.debug("<< {} : {}", header, response.getHeaders(header));
		}
		logBody("<< {}", response.getBody());
	}
	
	private static void logBody(String format, byte[] body) {
		if (body != null && body.length > 0) {
			try {
				log.debug(format, new String(body, "UTF-8"));
			} catch (UnsupportedEncodingException e) {
				log.error("Can't log body: {}", e.toString());
			}
		}
	}
	
	private static String enumerationToString(Enumeration<String> enumeration) {
		if (enumeration == null) {
			return "";
		} else {
			StringBuilder sb = new StringBuilder();
			while (enumeration.hasMoreElements()) {
				sb.append(enumeration.nextElement());
				if (enumeration.hasMoreElements()) {
					sb.append(HEADER_VALUES_SEPARATOR);
				}
			}
			return sb.toString();
		}
	}
}