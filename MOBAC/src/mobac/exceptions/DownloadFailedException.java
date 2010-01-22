package mobac.exceptions;

import java.io.IOException;

public class DownloadFailedException extends IOException {

	private final int httpResponseCode;
	
	public DownloadFailedException(int httpResponseCode) throws IOException {
		super("Invaild HTTP response: " + httpResponseCode);
		this.httpResponseCode = httpResponseCode;
	}

	public int getHttpResponseCode() {
		return httpResponseCode;
	}
	
}
