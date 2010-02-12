package mobac.program.atlascreators.tileprovider;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.lang.ref.SoftReference;
import java.util.Hashtable;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.log4j.Logger;

/**
 * A tile cache with speculative loading on a separate thread. Usually this
 * decreases map generation time on multi-core systems. 
 */
public class CacheTileProvider extends FilterTileProvider {

	private Logger log = Logger.getLogger(CacheTileProvider.class);

	private static int PRELOADER_THREAD_NUM = 1;

	private Hashtable<CacheKey, SRCachedTile> cache;

	private PreLoadThread preLoader = new PreLoadThread();

	public CacheTileProvider(TileProvider tileProvider) {
		super(tileProvider);
		cache = new Hashtable<CacheKey, SRCachedTile>(500);
		preLoader.start();
	}

	@Override
	public BufferedImage getTileImage(int x, int y, int layer) throws IOException {
		SRCachedTile cachedTile = cache.get(new CacheKey(x, y, layer));
		BufferedImage image = null;
		if (cachedTile != null) {
			CachedTile tile = cachedTile.get();
			if (tile != null) {
				if (tile.loaded)
					log.trace(String.format("Cache hit: x=%d y=%d l=%d", x, y, layer));
				image = tile.getImage();
				if (!tile.nextLoadJobCreated) {
					// log.debug(String.format("Preload job added : x=%d y=%d l=%d",
					// x + 1, y, layer));
					preloadTile(new CachedTile(new CacheKey(x + 1, y, layer)));
					tile.nextLoadJobCreated = true;
				}
			}
		}
		if (image == null) {
			log.trace(String.format("Cache miss: x=%d y=%d l=%d", x, y, layer));
			// log.debug(String.format("Preload job added : x=%d y=%d l=%d", x +
			// 1, y, layer));
			preloadTile(new CachedTile(new CacheKey(x + 1, y, layer)));
			image = internalGetTileImage(x, y, layer);
		}
		return image;
	}

	protected BufferedImage internalGetTileImage(int x, int y, int layer) throws IOException {
		synchronized (tileProvider) {
			return super.getTileImage(x, y, layer);
		}
	}

	public byte[] getTileData(int layer, int x, int y) throws IOException {
		throw new RuntimeException("Not implemented");
	}

	public byte[] getTileData(int x, int y) throws IOException {
		throw new RuntimeException("Not implemented");
	}

	private void preloadTile(CachedTile tile) {
		if (cache.get(tile.key) != null)
			return;
		preLoader.queue.add(tile);
		cache.put(tile.key, new SRCachedTile(tile));
	}

	public void cleanup() {
		try {
			cache.clear();
			if (preLoader != null) {
				preLoader.interrupt();
				preLoader = null;
			}
		} catch (Throwable t) {
			log.error("", t);
		}
	}

	@Override
	protected void finalize() throws Throwable {
		cleanup();
		super.finalize();
	}

	private static class SRCachedTile extends SoftReference<CachedTile> {

		public SRCachedTile(CachedTile referent) {
			super(referent);
		}

	}

	private class PreLoadThread extends Thread {

		private LinkedBlockingQueue<CachedTile> queue = null;

		public PreLoadThread() {
			super("ImagePreLoadThread" + (PRELOADER_THREAD_NUM++));
			log.debug("Image pre-loader thread started");
			queue = new LinkedBlockingQueue<CachedTile>();
		}

		@Override
		public void run() {
			CachedTile tile;
			try {
				while (true) {
					tile = queue.take();
					if (tile != null && !tile.loaded) {
						// log.trace("Loading image async: " + tile);
						tile.loadImage();
					}
				}
			} catch (InterruptedException e) {
				log.debug("Image pre-loader thread terminated");
			}
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

		@Override
		public String toString() {
			return "CacheKey [x=" + x + ", y=" + y + ", layer=" + layer + "]";
		}

	}

	private class CachedTile {

		CacheKey key;
		private BufferedImage image;
		boolean loaded = false;
		boolean nextLoadJobCreated = false;

		public CachedTile(CacheKey key) {
			super();
			this.key = key;
			image = null;
		}

		public synchronized void loadImage() {
			try {
				image = internalGetTileImage(key.x, key.y, key.layer);
			} catch (Exception e) {
				log.error("", e);
			}
			loaded = true;
		}

		public synchronized BufferedImage getImage() {
			if (!loaded)
				loadImage();
			return image;
		}

		@Override
		public String toString() {
			return "CachedTile [key=" + key + ", loaded=" + loaded + ", nextLoadJobCreated="
					+ nextLoadJobCreated + "]";
		}

	}
}
