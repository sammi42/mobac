package tac.tilestore;

public class TileStoreInfo {

	int tileCount;
	long storeSize;

	public TileStoreInfo(long storeSize, int tileCount) {
		super();
		this.storeSize = storeSize;
		this.tileCount = tileCount;
	}

	public int getTileCount() {
		return tileCount;
	}

	public long getStoreSize() {
		return storeSize;
	}

}
