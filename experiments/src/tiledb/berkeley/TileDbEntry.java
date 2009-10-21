package tiledb.berkeley;

import java.io.Serializable;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.KeyField;
import com.sleepycat.persist.model.Persistent;
import com.sleepycat.persist.model.PrimaryKey;

@Entity
public class TileDbEntry implements Serializable {

	private static final long serialVersionUID = -1759454532213387536L;

	@PrimaryKey
	TileDbKey tileKey;

	byte[] data;
	String eTag = null;
	long timeLastModified = -1;
	long timeDownloaded = -1;
	long timeExpires = -1;

	@Persistent
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

		public TileDbKey(int zoom, int x, int y) {
			super();
			this.x = x;
			this.y = y;
			this.zoom = zoom;
		}

	}

}
