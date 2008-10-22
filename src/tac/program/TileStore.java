package tac.program;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openstreetmap.gui.jmapviewer.interfaces.TileSource;

import tac.utilities.Utilities;

public class TileStore {

	private static TileStore sObj = null;

	private String tileStorePath;

	private TileStore() {
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

	private String getTilePath(int x, int y, int zoom, TileSource tileSource) {
		return tileStorePath + tileSource.getName() + "/" + zoom + "_" + x + "_" + y + "."
				+ tileSource.getTileType();
	}

	public File getTileFile(int x, int y, int zoom, TileSource tileSource) {
		return new File(getTilePath(x, y, zoom, tileSource));
	}

	public boolean copyStoredTileTo(File targetFileName, int x, int y, int zoom,
			TileSource tileSource) throws IOException {
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

	public boolean contains(int x, int y, int zoom, TileSource tileSource) {
		File f = getTileFile(x, y, zoom, tileSource);
		return f.exists();
	}
	
	public long getStoreSize(TileSource tileSource) {
		long size = 0;
		
		File tileStore = new File(tileStorePath + tileSource.getName());
				
		if(tileStore.exists()) {
			for (File f : tileStore.listFiles()) {
				size += f.length();
			}
			return size;
		} else {
			return size;
		}
	}
	
	public void clearStore(TileSource tileSource) {
		File tileStore = new File(tileStorePath + tileSource.getName());
				
		if(tileStore.exists()) {
			
			File [] files = tileStore.listFiles(); 
			if (files.length > 0) {
	
				boolean deleted = false;
	
				for (int i = 0; i < files.length; i++ ) {
					while(!deleted) {
						deleted = files[i].delete();
					}
					deleted = false;
				}
			}
		}
	}

	/**
	 * This method returns the amount of tiles in the store of tiles which is
	 * specified by the TileSource object.
	 * 
	 * @param tileSource
	 *            the store to calculate number of tiles in
	 * @return the amount of tiles in the specified store.
	 */
	public int getNrOfTiles(TileSource tileSource) {
		File tileStore = new File(tileStorePath, tileSource.getName());
		if (tileStore.exists()) {
			return tileStore.list(new TileFilter(tileSource)).length;
		} else {
			return 0;
		}
	}

	/**
	 * Filters out all files stored in the tile cache that does not represent a
	 * tile. Filtering is based on file name pattern
	 * <code>[number]_[number]_[number].[file extension]</code>
	 */
	protected static class TileFilter implements FilenameFilter {

		private Pattern p;

		public TileFilter(TileSource tileSource) {
			String fileExt = tileSource.getTileType();
			p = Pattern.compile("\\d+_\\d+_\\d+." + fileExt);
		}

		public boolean accept(File dir, String name) {
			Matcher m = p.matcher(name);
			return m.find();
		}
	}
}