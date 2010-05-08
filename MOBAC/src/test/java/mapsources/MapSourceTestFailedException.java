package mapsources;

import java.io.IOException;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;

import org.openstreetmap.gui.jmapviewer.interfaces.MapSource;

public class MapSourceTestFailedException extends Exception {

	private static final long serialVersionUID = 1L;

	final int httpResponseCode;
	final HttpURLConnection conn;
	final URL url;
	final Class<? extends MapSource> mapSourceClass;

	public MapSourceTestFailedException(MapSource mapSource, String msg, HttpURLConnection conn)
			throws IOException {
		super(msg);
		this.mapSourceClass = mapSource.getClass();
		this.conn = conn;
		this.url = conn.getURL();
		this.httpResponseCode = conn.getResponseCode();
	}

	public MapSourceTestFailedException(MapSource mapSource, HttpURLConnection conn) throws IOException {
		this(mapSource, "", conn);
	}

	public MapSourceTestFailedException(Class<? extends MapSource> mapSourceClass, URL url,
			int httpResponseCode) {
		super();
		this.mapSourceClass = mapSourceClass;
		this.url = url;
		this.conn = null;
		this.httpResponseCode = httpResponseCode;
	}

	public int getHttpResponseCode() {
		return httpResponseCode;
	}

	@Override
	public String getMessage() {
		String msg = super.getMessage();
		msg = "MapSource test failed: " + msg + " " + mapSourceClass.getSimpleName() + " HTTP "
				+ httpResponseCode + "\n" + conn.getURL();
		if (conn != null)
			msg += "\n" + printHeaders(conn);
		return msg;
	}

	protected String printHeaders(HttpURLConnection conn) {
		StringWriter sw = new StringWriter();
		sw.append("Headers:\n");
		for (Map.Entry<String, List<String>> entry : conn.getHeaderFields().entrySet()) {
			String key = entry.getKey();
			for (String elem : entry.getValue()) {
				if (key != null)
					sw.append(key + " = ");
				sw.append(elem);
				sw.append("\n");
			}
		}
		return sw.toString();
	}

}
