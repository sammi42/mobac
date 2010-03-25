package mobac.exceptions;

import java.io.IOException;
import java.net.HttpURLConnection;

public class DownloadFailedException extends IOException {

	private final int httpResponseCode;
	private HttpURLConnection connection;

	public DownloadFailedException(HttpURLConnection connection, int httpResponseCode)
			throws IOException {
		super("Invaild HTTP response: " + httpResponseCode);
		this.connection = connection;
		this.httpResponseCode = httpResponseCode;
	}

	public int getHttpResponseCode() {
		return httpResponseCode;
	}

	@Override
	public String getMessage() {
		return super.getMessage() + "\n" + connection.getURL();
	}

}
