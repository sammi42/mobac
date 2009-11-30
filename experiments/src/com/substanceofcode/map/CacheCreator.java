package com.substanceofcode.map;

/*
 * CacheCreator.java
 * 
 * Copyright 2008 Gareth Jones
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 */
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class CacheCreator {

	private final String cache;
	private final File outdir;
	private final File indir;

	private long lastTileOffset = 0l;
	private Map<String, Long> availableTileList = new HashMap<String, Long>();

	CacheCreator(String cache, File outdir, File indir) {
		this.cache = cache;
		this.outdir = outdir;
		this.indir = indir;
	}

	public void run() throws IOException {
		boolean append = true;

		File o = new File(outdir, "MTEFileCache");
		if (o.canRead()) {
			try {
				initializeCache(o);
			} catch (Exception e) {
				Logger.error("Unable to read cache file: " + e.getMessage());
				System.exit(1);
			}
		}

		OutputStream os = new FileOutputStream(o, append);
		DataOutputStream dos = new DataOutputStream(os);
		List<String> fileList = new LinkedList<String>();
		int x = 0, y = 0, z = 0;
		// List all the tiles, assuming the folder hierarchy is zoom/lat/lon.png
		for (File zoomDir : indir.listFiles(new DirFileFilter())) {
			for (File xDir : zoomDir.listFiles(new DirFileFilter())) {
				File[] files = xDir.listFiles(new FilenameFilter() {
					public boolean accept(File dir, String name) {
						return name.toLowerCase().endsWith(".png");
					}
				});
				for (File imageFile : files) {
					x = Integer.parseInt(xDir.getName());
					z = Integer.parseInt(zoomDir.getName());

					String yName = imageFile.getName();
					// remove ".png"
					yName = yName.substring(0, yName.length() - 4);
					y = Integer.parseInt(yName);
					InputStream is = new FileInputStream(imageFile);
					maybeWriteToCache(dos, cache, is, x, y, z);
					is.close();
					fileList.add(imageFile.getAbsolutePath());
				}
			}
		}

		dos.close();

		// print the list
		for (String string : fileList) {
			System.out.println(string);
		}

	}

	/**
	 * Read any existing cache file so we don't add duplicate tiles to it
	 * 
	 * @param o
	 * @throws IOException
	 */
	private void initializeCache(File o) throws IOException {

		Logger.debug("Reading existing FileCache...");

		DataInputStream streamIn;

		streamIn = new DataInputStream(new FileInputStream(o));

		for (;;) {
			// There's no way of detecting the end of the stream
			// short of getting an IOexception
			try {
				Tile t = Tile.getTile(streamIn);
				Logger.debug("Found tile " + t.cacheKey + ", at offset " + t.offset);
				availableTileList.put(t.cacheKey, new Long(t.offset));
			} catch (EOFException e) {
				Logger.debug("EOF");
				break;
			}
		}

		Logger.debug("FILE: read " + availableTileList.size() + " tiles");
		streamIn.close();
		streamIn = null;
	}

	private boolean maybeWriteToCache(DataOutputStream dos, String cache, InputStream is, int x, int y, int z)
			throws IOException {

		String url = "not used";
		String cacheKey = cache + "-" + z + "-" + x + "-" + y;

		if (checkCache(cacheKey)) {
			Logger.debug("Not writing " + cacheKey + " to filecache, it's already there");
			return false;
		}

		byte[] imageByteArray = MapUtils.parseInputStream(is);

		dos.writeInt(x);
		dos.writeInt(y);
		dos.writeInt(z);

		byte[] urlBytes = url.getBytes();
		dos.writeShort(urlBytes.length);
		dos.write(urlBytes);

		byte[] keyBytes = cacheKey.getBytes();
		dos.writeShort(keyBytes.length);
		dos.write(keyBytes);
		// Logger.debug(Long.toString(lastTileOffset));
		dos.writeLong(lastTileOffset);

		lastTileOffset += 12 + // x, y and z
				2 + urlBytes.length + // strings and their lengths
				2 + keyBytes.length + 8 + // tile offset (long)
				4 + // image byte array length (int)
				imageByteArray.length;

		dos.writeInt(imageByteArray.length);
		dos.write(imageByteArray);
		return true;
	}

	public boolean checkCache(String name) {
		return availableTileList.containsKey(name);
	}

	public static class DirFileFilter implements FileFilter {

		@Override
		public boolean accept(File f) {
			return f.isDirectory();
		}

	}
}
