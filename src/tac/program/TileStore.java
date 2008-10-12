package tac.program;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

import org.openstreetmap.gui.jmapviewer.interfaces.TileSource;

import tac.utilities.Utilities;

public class TileStore {

	private static TileStore sObj = null;

	private String tileStorePath;

	private TileStore() {
		tileStorePath = System.getProperty("user.dir") + "/tilestore/";
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
		return tileStorePath + tileSource + "/" + zoom + "_" + x + "_" + y + "."
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
		long sourceBytes =0;
		long writtenBytes=0;
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

}