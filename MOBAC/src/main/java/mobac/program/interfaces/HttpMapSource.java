package mobac.program.interfaces;

import java.io.IOException;
import java.net.HttpURLConnection;

public interface HttpMapSource extends MapSource {
	/**
	 * Specifies the different mechanisms for detecting updated tiles respectively only download newer tiles than those
	 * stored locally.
	 * 
	 * <ul>
	 * <li>{@link #IfNoneMatch} Server provides ETag header entry for all tiles and <b>supports</b> conditional download
	 * via <code>If-None-Match</code> header entry.</li>
	 * <li>{@link #ETag} Server provides ETag header entry for all tiles but <b>does not support</b> conditional
	 * download via <code>If-None-Match</code> header entry.</li>
	 * <li>{@link #IfModifiedSince} Server provides Last-Modified header entry for all tiles and <b>supports</b>
	 * conditional download via <code>If-Modified-Since</code> header entry.</li>
	 * <li>{@link #LastModified} Server provides Last-Modified header entry for all tiles but <b>does not support</b>
	 * conditional download via <code>If-Modified-Since</code> header entry.</li>
	 * <li>{@link #None} The server does not support any of the listed mechanisms.</li>
	 * </ul>
	 * 
	 */
	public enum TileUpdate {
		IfNoneMatch, ETag, IfModifiedSince, LastModified, None
	}

	/**
	 * @return The supported tile update mechanism
	 * @see TileUpdate
	 */
	public TileUpdate getTileUpdate();

	/**
	 * Constructs the tile url connection. If necessary the url connection can be prepared with cookies or other http
	 * specific headers which are required by the http server.
	 * 
	 * @param zoom
	 * @param tilex
	 *            tile number on x-axis for the specified <code>zoom</code> level
	 * @param tiley
	 *            tile number on y-axis for the specified <code>zoom</code> level
	 * @return the initialized urlConnection for downloading the specified tile image
	 */
	public HttpURLConnection getTileUrlConnection(int zoom, int tilex, int tiley) throws IOException;

}
