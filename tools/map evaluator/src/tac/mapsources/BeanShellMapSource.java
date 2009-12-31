package tac.mapsources;

import java.io.IOException;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import bsh.EvalError;
import bsh.Interpreter;

public class BeanShellMapSource extends AbstractMapSource {

	private static int NUM = 0;

	private Logger log = Logger.getLogger(BeanShellMapSource.class);

	private final String code;

	private final List<String> cookies;

	public BeanShellMapSource(String code) {
		super("TestMapSource" + Integer.toString(NUM++), 0, 20, "");
		this.code = code;
		cookies = new ArrayList<String>();
	}

	@Override
	public boolean allowFileStore() {
		return false;
	}

	public List<String> getCookies() {
		return cookies;
	}

	@Override
	public HttpURLConnection getTileUrlConnection(int zoom, int tilex, int tiley)
			throws IOException {
		HttpURLConnection conn = super.getTileUrlConnection(zoom, tilex, tiley);
		StringWriter cookieProp = new StringWriter();
		boolean first = true;
		for (String cookie : cookies) {
			if (first)
				first = false;
			else
				cookieProp.write("; ");
			cookieProp.write(cookie);
		}
		conn.setRequestProperty("Cookie", cookieProp.toString());
		return conn;
	}

	@Override
	public String getTileUrl(int zoom, int tilex, int tiley) {
		try {
			return evalTileUrl(zoom, tilex, tiley);
		} catch (EvalError e) {
			log.error("", e);
			return null;
		}
	}

	public String evalTileUrl(int zoom, int tilex, int tiley) throws EvalError {
		Interpreter i = new Interpreter();
		i.eval(code);
		return (String) i.eval(String.format("getTileUrl(%d,%d,%d);", zoom, tilex, tiley));
	}
}
