package tac.tilestore.berkeleydb;

import java.io.Serializable;
import java.util.Date;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.KeyField;
import com.sleepycat.persist.model.Persistent;
import com.sleepycat.persist.model.PrimaryKey;

@Entity(version = 1)
public class TileDbEntry implements Serializable {

	private static final long serialVersionUID = -1759454532213387536L;

	@PrimaryKey
	protected TileDbKey tileKey;

	private byte[] data;
	private String eTag = null;

	private long timeDownloaded;

	private long timeLastModified;
	private long timeExpires;

	protected TileDbEntry() {
		// required for deserialization
	}

	public TileDbEntry(int x, int y, int zoom, byte[] data) {
		tileKey = new TileDbKey(x, y, zoom);
		this.data = data;
		this.timeDownloaded = System.currentTimeMillis();
	}

	public TileDbEntry(int x, int y, int zoom, byte[] data, long timeLastModified,
			long timeExpires, String eTag) {
		this(x, y, zoom, data);
		this.timeLastModified = timeLastModified;
		this.timeExpires = timeExpires;
		this.eTag = eTag;
	}

	public byte[] getData() {
		return data;
	}

	public String geteTag() {
		return eTag;
	}

	public long getTimeLastModified() {
		return timeLastModified;
	}

	public long getTimeDownloaded() {
		return timeDownloaded;
	}

	public long getTimeExpires() {
		return timeExpires;
	}

	@Override
	public String toString() {
		String tlm = (timeLastModified <= 0) ? "-" : new Date(timeLastModified).toString();
		String txp = (timeExpires <= 0) ? "-" : new Date(timeExpires).toString();
		return String.format("Tile %3d/%3d/z%2d dl[%s] lm[%s] exp[%s] eTag[%s]", tileKey.x, tileKey.y,
				tileKey.zoom, new Date(timeDownloaded), tlm, txp, eTag);
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
