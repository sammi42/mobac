package tac.program.atlascreators.tileprovider;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.lang.ref.SoftReference;
import java.util.Hashtable;

public class CacheTileProvider extends FilterTileProvider {

	private Hashtable<CacheKey, SRCachedTile> cache;

	public CacheTileProvider(TileProvider tileProvider) {
		super(tileProvider);
		cache = new Hashtable<CacheKey, SRCachedTile>(500);
	}

	@Override
	public BufferedImage getTileImage(int x, int y, int layer) throws IOException {
		SRCachedTile cachedTile = cache.get(new CacheKey(x, y, layer));
		if (cachedTile != null) {
			CachedTile tile = cachedTile.get();
			if (tile != null)
				return tile.getImage();
		}
		BufferedImage image = super.getTileImage(x, y, layer);
		return image;
	}

	protected BufferedImage internalGetTileImage(int x, int y, int layer) throws IOException {
		return super.getTileImage(x, y, layer);
	}

	public byte[] getTileData(int layer, int x, int y) throws IOException {
		throw new RuntimeException("Not implemented");
	}

	public byte[] getTileData(int x, int y) throws IOException {
		throw new RuntimeException("Not implemented");
	}

	private static class SRCachedTile extends SoftReference<CachedTile> {

		public SRCachedTile(CachedTile referent) {
			super(referent);
		}

	}

	private static class CacheKey {
		int x;
		int y;
		int layer;

		public CacheKey(int x, int y, int layer) {
			super();
			this.x = x;
			this.y = y;
			this.layer = layer;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + layer;
			result = prime * result + x;
			result = prime * result + y;
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			CacheKey other = (CacheKey) obj;
			if (layer != other.layer)
				return false;
			if (x != other.x)
				return false;
			if (y != other.y)
				return false;
			return true;
		}
		
	}

	private class CachedTile {
		int x;
		int y;
		int layer;
		private BufferedImage image;

		public CachedTile(int x, int y, int layer) {
			super();
			this.x = x;
			this.y = y;
			this.layer = layer;
			image = null;
		}

		public synchronized void loadImage() {
			try {
				image = internalGetTileImage(x, y, layer);
			} catch (IOException e) {
				//
			}
		}

		public synchronized BufferedImage getImage() {
			return image;
		}
		
	}
}
