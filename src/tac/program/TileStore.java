package tac.program;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.openstreetmap.gui.jmapviewer.interfaces.MapSource;

import tac.utilities.DeleteFileFilter;
import tac.utilities.Utilities;

public class TileStore {

	private static TileStore sObj = null;

	private String tileStorePath;

	private Logger log;

	private TileStore() {
		log = Logger.getLogger(this.getClass());
		tileStorePath = System.getProperty("user.dir") + File.separator + "tilestore"
				+ File.separator;
	}

	public static TileStore getInstance() {
		if (sObj != null)
			return sObj;
		synchronized (TileStore.class) {
			if (sObj == null)
				sObj = new TileStore();
			return sObj;
		}
	}

	private String getTilePath(int x, int y, int zoom, MapSource mapSource) {
		if (!mapSource.allowFileStore())
			return null;
		return tileStorePath + mapSource.getName() + "/" + zoom + "_" + x + "_" + y + "."
				+ mapSource.getTileType();
	}

	public File getTileFile(int x, int y, int zoom, MapSource mapSource) {
		if (!mapSource.allowFileStore())
			return null;
		return new File(getTilePath(x, y, zoom, mapSource));
	}

	public boolean copyStoredTileTo(File targetFileName, int x, int y, int zoom,
			MapSource tileSource) throws IOException {
		File sourceFile = getTileFile(x, y, zoom, tileSource);
		if (!sourceFile.exists())
			return false;
		FileInputStream fis = null;
		FileOutputStream fos = null;
		long sourceBytes = 0;
		long writtenBytes = 0;
		try {
			fis = new FileInputStream(sourceFile);
			fos = new FileOutputStream(targetFileName);
			FileChannel source = fis.getChannel();
			FileChannel destination = fos.getChannel();
			sourceBytes = source.size();
			writtenBytes = destination.transferFrom(source, 0, sourceBytes);
		} finally {
			Utilities.closeStream(fis);
			Utilities.closeStream(fos);
		}
		if (writtenBytes != sourceBytes)
			throw new IOException("Target file's size is not equal to the source file's size!");
		return true;
	}

	public boolean contains(int x, int y, int zoom, MapSource tileSource) {
		File f = getTileFile(x, y, zoom, tileSource);
		return f != null && f.exists();
	}

	public void prepareTileStore(MapSource mapSource) {
		if (!mapSource.allowFileStore())
			return;
		File tileStoreTile = getTileFile(0, 0, 0, mapSource);
		File tileStoreDir = tileStoreTile.getParentFile();
		tileStoreDir.mkdirs();
	}

	public void clearStore(MapSource tileSource) {
		File tileStore = new File(tileStorePath, tileSource.getName());

		if (tileStore.exists()) {
			DeleteFileFilter dff = new DeleteFileFilter();
			tileStore.listFiles(dff);
			tileStore.delete();
			log.debug("Tilestore " + tileSource.getName() + " cleared: " + dff);
		}
	}

	/**
	 * This method returns the amount of tiles in the store of tiles which is
	 * specified by the TileSource object.
	 * 
	 * @param tileSource
	 *            the store to calculate number of tiles in
	 * @return the amount of tiles in the specified store.
	 * @throws InterruptedException
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

	/**
	 * Returns <code>true</code> if the tile store directory of the specified
	 * {@link MapSource} exists.
	 * 
	 * @param tileSource
	 * @return
	 */
	public boolean storeExists(MapSource tileSource) {
		File tileStore = new File(tileStorePath, tileSource.getName());
		return (tileStore.isDirectory()) && (tileStore.exists());
	}

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

	public static class TileStoreInfo {

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