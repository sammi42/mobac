package tac.tilestore;

import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openstreetmap.gui.jmapviewer.interfaces.MapSource;

import tac.utilities.Utilities;
import tac.utilities.file.DeleteFileFilter;

/**
 * The old file-base tile store implementation.
 */
public class FileTileStore extends TileStore {

	protected String tileStorePath;

	protected FileTileStore() {
		super();
		tileStorePath = tileStoreDir.getPath();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see tac.tilestore.TileStore#putTileData(byte[], int, int, int,
	 * org.openstreetmap.gui.jmapviewer.interfaces.MapSource)
	 */
	public void putTileData(byte[] tileData, int x, int y, int zoom, MapSource mapSource)
			throws IOException {
		if (!mapSource.allowFileStore())
			return;
		File f = getTileFile(x, y, zoom, mapSource);
		FileOutputStream fo = null;
		try {
			fo = new FileOutputStream(f, false);
			fo.write(tileData);
		} finally {
			Utilities.closeStream(fo);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see tac.tilestore.TileStore#getTileData(int, int, int,
	 * org.openstreetmap.gui.jmapviewer.interfaces.MapSource)
	 */
	public byte[] getTileData(int x, int y, int zoom, MapSource mapSource) {
		if (!mapSource.allowFileStore())
			return null;
		File f = getTileFile(x, y, zoom, mapSource);
		if (f == null || !f.isFile())
			return null;
		try {
			return Utilities.getFileBytes(f);
		} catch (IOException e) {
			log.error("", e);
			return null;
		}
	}

	private String getTilePath(int x, int y, int zoom, MapSource mapSource) {
		if (!mapSource.allowFileStore())
			return null;
		return tileStorePath + mapSource.getName() + "/" + zoom + "_" + x + "_" + y + "."
				+ mapSource.getTileType();
	}

	private File getTileFile(int x, int y, int zoom, MapSource mapSource) {
		if (!mapSource.allowFileStore())
			return null;
		return new File(getTilePath(x, y, zoom, mapSource));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see tac.tilestore.TileStore#contains(int, int, int,
	 * org.openstreetmap.gui.jmapviewer.interfaces.MapSource)
	 */
	public boolean contains(int x, int y, int zoom, MapSource tileSource) {
		File f = getTileFile(x, y, zoom, tileSource);
		return f != null && f.exists();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * tac.tilestore.TileStore#prepareTileStore(org.openstreetmap.gui.jmapviewer
	 * .interfaces.MapSource)
	 */
	public void prepareTileStore(MapSource mapSource) {
		if (!mapSource.allowFileStore())
			return;
		File tileStoreTile = getTileFile(0, 0, 0, mapSource);
		File tileStoreDir = tileStoreTile.getParentFile();
		tileStoreDir.mkdirs();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seetac.tilestore.TileStore#clearStore(org.openstreetmap.gui.jmapviewer.
	 * interfaces.MapSource)
	 */
	public void clearStore(MapSource mapSource) {
		File tileStore = new File(tileStorePath, mapSource.getName());

		if (tileStore.exists()) {
			DeleteFileFilter dff = new DeleteFileFilter();
			tileStore.listFiles(dff);
			tileStore.delete();
			log.debug("Tilestore " + mapSource.getName() + " cleared: " + dff);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * tac.tilestore.TileStore#getNrOfTiles(org.openstreetmap.gui.jmapviewer
	 * .interfaces.MapSource)
	 */
	public int getNrOfTiles(MapSource tileSource) throws InterruptedException {
		File tileStore = new File(tileStorePath, tileSource.getName());
		if (tileStore.exists()) {
			TileStoreInfoFilter tsif = new TileStoreInfoFilter(tileSource);
			try {
				tileStore.listFiles(tsif);
			} catch (RuntimeException e) {
				throw new InterruptedException();
			}
			return tsif.getCount();
		} else {
			return 0;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * tac.tilestore.TileStore#getStoreSize(org.openstreetmap.gui.jmapviewer
	 * .interfaces.MapSource)
	 */
	public long getStoreSize(MapSource tileSource) throws InterruptedException {
		File tileStore = new File(tileStorePath, tileSource.getName());
		if (tileStore.exists()) {
			TileStoreInfoFilter tsif = new TileStoreInfoFilter(tileSource);
			try {
				tileStore.listFiles(tsif);
			} catch (RuntimeException e) {
				throw new InterruptedException();
			}
			return tsif.getSize();
		} else {
			return 0;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * tac.tilestore.TileStore#storeExists(org.openstreetmap.gui.jmapviewer.
	 * interfaces.MapSource)
	 */
	public boolean storeExists(MapSource tileSource) {
		File tileStore = new File(tileStorePath, tileSource.getName());
		return (tileStore.isDirectory()) && (tileStore.exists());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * tac.tilestore.TileStore#getStoreInfo(org.openstreetmap.gui.jmapviewer
	 * .interfaces.MapSource)
	 */
	public TileStoreInfo getStoreInfo(MapSource tileSource) throws InterruptedException {
		File tileStore = new File(tileStorePath, tileSource.getName());
		if (tileStore.exists()) {
			TileStoreInfoFilter tsif = new TileStoreInfoFilter(tileSource);
			try {
				tileStore.listFiles(tsif);
			} catch (RuntimeException e) {
				throw new InterruptedException();
			}
			return new TileStoreInfo(tsif.getSize(), tsif.getCount());
		} else {
			return new TileStoreInfo(0, 0);
		}
	}

	/**
	 * Counts all tiles stored in the specified tile cache. Files in the
	 * directory that are not tiles are ignored. Filtering is based on file name
	 * pattern <code>[number]_[number]_[number].[file extension]</code>
	 */
	protected static class TileStoreInfoFilter implements FileFilter {

		private Pattern p;

		long size = 0;
		int count = 0;

		TileStoreInfoFilter(MapSource tileSource) {
			String fileExt = tileSource.getTileType();
			p = Pattern.compile("\\d+_\\d+_\\d+." + fileExt);
		}

		public boolean accept(File f) {
			if (f.isDirectory())
				return false;
			Utilities.checkForInterruptionRt();
			String name = f.getName();
			Matcher m = p.matcher(name);
			if (!m.matches())
				return false;
			size += f.length();
			count++;
			return false;
		}

		public long getSize() {
			return size;
		}

		public int getCount() {
			return count;
		}

	}
}