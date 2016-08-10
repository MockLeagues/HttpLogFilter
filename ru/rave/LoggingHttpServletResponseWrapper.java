package ru.rave;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

public class LoggingHttpServletResponseWrapper extends HttpServletResponseWrapper {
	
	private HttpServletResponse response;
	private PrintWriter writer;
	private LogServletOutputStream stream;
	
	public LoggingHttpServletResponseWrapper(HttpServletResponse response) {
		super(response);
		this.response = response;
	}
	
	public byte[] getBody() {
		 return stream == null ? new byte[]{} : stream.getBody();
	}
	
	@Override
	public PrintWriter getWriter() throws IOException {
		if (stream != null) {
			throw new IllegalStateException("getOutputStream() already called on this response");
		}
		if (writer == null) {
			writer = new PrintWriter(getOutputStream());
		}
		return writer;
	}
	
	@Override
	public ServletOutputStream getOutputStream() throws IOException {
		if (writer != null) {
			throw new IllegalStateException("getWriter() already called on this response");
		}
		if (stream == null) {
			stream = new LogServletOutputStream(response.getOutputStream());
		}
		return stream;
	}
	
	private static class LogServletOutputStream extends ServletOutputStream {
		
		private final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		private ServletOutputStream output;
		
		public LogServletOutputStream(ServletOutputStream output) {
			this.output = output;
		}
		
		public byte[] getBody() {
			return baos.toByteArray(); 
		}
		
		@Override
		public void flush() throws IOException {
			super.flush();
			output.flush();
			baos.flush();
		}
		
		@Override
		public void close() throws IOException {
			super.close();
			output.close();
			baos.close();
		}
		
		@Override
		public boolean isReady() {
			return output.isReady();
		}
		
		@Override
		public void write(int b) throws IOException {
			output.write(b);
			baos.write(b);
		}
		
		@Override
		public void setWriteListener(WriteListener writeListener) {}
	}
}
