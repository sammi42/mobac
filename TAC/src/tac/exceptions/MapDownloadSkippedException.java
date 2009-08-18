package tac.exceptions;

import tac.program.AtlasThread;

/**
 * Thrown in {@link AtlasThread#createMap(tac.program.interfaces.MapInterface)}
 * if the user chose to skip that map because of download problems.
 */
public class MapDownloadSkippedException extends Exception {

	public MapDownloadSkippedException() {
		super();
	}

}
