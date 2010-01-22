package mobac.exceptions;

import mobac.program.interfaces.MapInterface;

public class AtlasTestException extends Exception {

	public AtlasTestException(String message, MapInterface map) {
		super(message + " (caused by map " + map.getName() + ")");
	}

	public AtlasTestException(String message) {
		super(message);
	}

	public AtlasTestException(Throwable cause) {
		super(cause);
	}

	public AtlasTestException(String message, Throwable cause) {
		super(message, cause);
	}

}
