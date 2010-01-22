package mobac.exceptions;

import mobac.program.AtlasThread;

/**
 * Thrown in {@link AtlasThread#createMap(mobac.program.interfaces.MapInterface)}
 * if the user chose to skip that map because of download problems.
 */
public class MapDownloadSkippedException extends Exception {

	private static final long serialVersionUID = 1L;

	public MapDownloadSkippedException() {
		super();
	}

}
