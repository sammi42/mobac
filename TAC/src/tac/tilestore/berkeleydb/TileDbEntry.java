package tac.tilestore.berkeleydb;

import java.io.Serializable;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.KeyField;
import com.sleepycat.persist.model.Persistent;
import com.sleepycat.persist.model.PrimaryKey;

@Entity(version = 1)
public class TileDbEntry implements Serializable {

	private static final long serialVersionUID = -1759454532213387536L;

	@PrimaryKey
	TileDbKey tileKey;

	byte[] data;
	String eTag = null;
	long timeLastModified = -1;
	long timeDownloaded = -1;
	long timeExpires = -1;

	protected TileDbEntry() {
		// required for deserialization
	}

	public TileDbEntry(int x, int y, int zoom) {
		tileKey = new TileDbKey(x, y, zoom);
	}

	@Persistent(version = 1)
	public static class TileDbKey implements Serializable {

		private static final long serialVersionUID = 6037918679593107685L;

		@KeyField(1)
		public int zoom;
		@KeyField(2)
		public int x;
		@KeyField(3)
		public int y;

		protected TileDbKey() {
		}

		public TileDbKey(int x, int y, int zoom) {
			super();
			this.x = x;
			this.y = y;
			this.zoom = zoom;
		}

	}

}
