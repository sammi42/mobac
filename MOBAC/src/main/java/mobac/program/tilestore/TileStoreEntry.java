package mobac.program.tilestore;

public interface TileStoreEntry {

	public int getX();

	public int getY();

	public int getZoom();

	/**
	 * This function does never return a <code>null</code> value!
	 * 
	 * @return tile data
	 */
	public byte[] getData();

	/**
	 * The time and date in UTC when this map tile has been downloaded
	 * respectively has been checked the last time via HTTP If-None-Match,
	 * If-Modified-Since or a HTTP HEAD request.
	 * 
	 * @return Time in UTC
	 */
	public long getTimeDownloaded();

	public void update(long timeExpires);
	
	/**
	 * 
	 * @return Last modification time in UTC or <code>0</code> if not supported
	 *         by the server
	 */
	public long getTimeLastModified();

	/**
	 * 
	 * @return Expiration time in UTC or <code>0</code> if not supported by the
	 *         server
	 */
	public long getTimeExpires();

	/**
	 * The eTag contained in the HTTP answer on the last download/check request.
	 * 
	 * @return eTag or <code>null</code> if not supported by the server
	 */
	public String geteTag();

}
