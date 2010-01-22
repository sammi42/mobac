package mobac.exceptions;

import mobac.mapsources.MapSourcesManager;

/**
 * Encapsulates several other exceptions that may occur while performing an
 * mapsources online update.
 * 
 * @see MapSourcesManager#mapsourcesOnlineUpdate()
 */
public class MapSourcesUpdateException extends Exception {
	
	private static final long serialVersionUID = 1L;

	public MapSourcesUpdateException(String message) {
		super(message);
	}

	public MapSourcesUpdateException(Throwable cause) {
		super(cause);
	}

}
