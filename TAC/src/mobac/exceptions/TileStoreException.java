package mobac.exceptions;

import com.sleepycat.je.DatabaseException;

public class TileStoreException extends DatabaseException {

	public TileStoreException(Throwable t) {
		super(t);
	}

	public TileStoreException(String message) {
		super(message);
	}

	public TileStoreException(String message, Throwable t) {
		super(message, t);
	}
	
}
