package ru.rave;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggingHttpServletRequestWrapper extends HttpServletRequestWrapper {
	
	private static final Logger log = LoggerFactory.getLogger(LoggingHttpServletRequestWrapper.class);
	
	private byte[] body = new byte[]{};
	private BufferedReader reader;
	private ServletInputStream input;
	
	public LoggingHttpServletRequestWrapper(HttpServletRequest request) {
		super(request);
		copyBody(request);
	}
	
	public byte[] getBody() {
		return body;
	}
	
	private void copyBody(HttpServletRequest request) {
		try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
				ServletInputStream in = request.getInputStream();) {
			int b;
			while ((b = in.read()) != -1) {
				baos.write(b);
			}
			body = baos.toByteArray();
		} catch (IOException e) {
			log.error("Can't copy request body: {}", e.toString());
		}
	}
	
	@Override
	public BufferedReader getReader() throws IOException {
		if (input != null) {
			throw new IllegalStateException("getInputStream() already called on this request");
		}
		if (reader == null) {
			reader = new BufferedReader(new InputStreamReader(getInputStream()));
		}
		return reader;
	}
	
	@Override
	public ServletInputStream getInputStream() throws IOException {
		if (reader != null) {
			throw new IllegalStateException("getReader() already called on this request");
		}
		if (input == null) {
			input = new LogServletInputStream(new ByteArrayInputStream(body));
		}
		return input;
	}
	
	private static class LogServletInputStream extends ServletInputStream {
		
		private final ByteArrayInputStream input;
		
		public LogServletInputStream(ByteArrayInputStream input){
			this.input = input;
		}
		
		//very important to override this method, this one called by Servlet API
		@Override
		public int available() throws IOException {
			return input.available();
		}
		
		@Override
		public boolean isFinished() {
			return input.available() == 0;
		}
		
		@Override
		public boolean isReady() {
			return input.available() > 0;
		}
		
		@Override
		public int read() throws IOException {
			return input.read();
		}
		
		@Override
		public int read(byte[] b) throws IOException {
			return input.read(b);
		}
		
		@Override
		public int read(byte[] b, int off, int len) throws IOException {
			return input.read(b, off, len);
		}
		
		@Override
		public void close() throws IOException {
			super.close();
			input.close();
		}
		
		@Override
		public void setReadListener(ReadListener readListener) {}
	}
}
