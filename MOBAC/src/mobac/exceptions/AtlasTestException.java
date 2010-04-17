package mobac.exceptions;

import mobac.program.interfaces.LayerInterface;
import mobac.program.interfaces.MapInterface;

public class AtlasTestException extends Exception {

	public AtlasTestException(String message, MapInterface map) {
		super(message + "\nError caused by map \"" + map.getName() + "\" on layer \""
				+ map.getLayer().getName() + "\"");
	}

	public AtlasTestException(String message, LayerInterface layer) {
		super(message + "\nError caused by layer \"" + layer.getName() + "\"");
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
