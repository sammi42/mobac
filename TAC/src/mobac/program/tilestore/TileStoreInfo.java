package mobac.program.tilestore;

public class TileStoreInfo {

	int tileCount;
	long storeSize;

	public TileStoreInfo(long storeSize, int tileCount) {
		super();
		this.storeSize = storeSize;
		this.tileCount = tileCount;
	}

	/**
	 * @return Number of tiles stored in the tile store
	 */
	public int getTileCount() {
		return tileCount;
	}

	/**
	 * @return store size in bytes
	 */
	public long getStoreSize() {
		return storeSize;
	}

}
