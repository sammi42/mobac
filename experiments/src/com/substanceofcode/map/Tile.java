package com.substanceofcode.map;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.io.UTFDataFormatException;

/**
 * Stores the Meta-data relating to a single map tile, and it's retrieval status
 * 
 * @author gjones
 * 
 */

public class Tile implements Serializable {
	
	public int x = 0;

	public int y = 0;

	public int z = 0;

	public String url = null;

	public String cacheKey = null;

	// Ideally we don't want to have to store this along with the Image object,
	// as it is cleaner to store an Image created from this byteArray.
	// However, we also want to write the tile to the filesystem
	// Which will require either writing a PngImageWriter, or, outputting this
	// byte [] ;-).
	// The downside is that this could make Tiles too big, so perhaps we should
	// delete
	// this once the tile has been written correctly to the filesystem?
	// or maybe creating the image each time is quick enough to avoid storing
	// it?
	private byte[] imageByteArray = null;

	public long offset = 0;
	private static long lastTileOffset = 0;//
	public static long totalOffset = 0;

	public static final String MIMETYPE = "Tile"; // used by the filesystem

	// api to distinguish 'file' types

	public Tile(int x, int y, int z, String url, String dir, String file, String storename) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.url = url;

		this.cacheKey = storename + "-" + z + "-" + x + "-" + y;
	}

	public Tile() {

	}

	/**
	 * Instantiate a Track from a DataInputStream
	 */
	public Tile(DataInputStream dis) throws Exception {
		this.unserialize(dis);
	}

	public void setImageByteArray(byte[] in) {
		this.imageByteArray = in;
	}

	public byte[] getImageByteArray() {
		return this.imageByteArray;
	}

	public void clearImageByteArray() {
		this.imageByteArray = null;
	}

	public String getMimeType() {
		return MIMETYPE;
	}

	/**
	 * serialize the tile to the filesystem. This version takes an offset.
	 * 
	 * @param dos
	 * @param offset
	 * @throws IOException
	 */
	public void serialize(DataOutputStream dos, long offset) throws IOException {
		lastTileOffset = offset;
		serialize(dos);
	}

	public void serialize(DataOutputStream dos) throws IOException {
		// Logger.debug("Tile serialize called");
		dos.writeInt(x);
		dos.writeInt(y);
		dos.writeInt(z);
		// Write the length of the string byte array as a short followed by the
		// bytes of the strings
		byte[] urlBytes = url.getBytes();
		dos.writeShort(urlBytes.length);
		dos.write(urlBytes);

		byte[] keyBytes = cacheKey.getBytes();
		dos.writeShort(keyBytes.length);
		dos.write(keyBytes);

		Logger.debug("lastTileOffset=" + lastTileOffset);
		dos.writeLong(lastTileOffset);

		lastTileOffset += 12 + // x, y and z
				2 + urlBytes.length + // strings and their lengths
				2 + keyBytes.length + 8 + // tile offset (long)
				4 + // image byte array length (int)
				imageByteArray.length;

		dos.writeInt(imageByteArray.length);
		dos.write(imageByteArray);
		// We won't save the image, we can regenerate it from the byte array
		// if needed
	}

	public void unserialize(DataInputStream dis) throws IOException {
		// Logger.debug("Tile unserialize called");
		try {
			x = dis.readInt();
			y = dis.readInt();
			z = dis.readInt();

			// Read strings. First the length and then the data
			short len = dis.readShort();
			byte[] bytes = new byte[len];
			dis.read(bytes, 0, len);
			url = new String(bytes);

			len = dis.readShort();
			bytes = new byte[len];
			dis.read(bytes, 0, len);
			cacheKey = new String(bytes);

			offset = dis.readLong();

			int arrayLength = dis.readInt();
			imageByteArray = new byte[arrayLength];
			dis.read(imageByteArray, 0, arrayLength);
		} catch (NegativeArraySizeException nase) {
			// Caused by a cockup in the serialized file
			// if we get one of these the best we can do is
			// discard this tile and nullify the rest of the stream

			nase.printStackTrace();
		} catch (UTFDataFormatException udfe) {
			Logger.error("Caught UTFDataFormatException:" + "Error was " + udfe.getMessage());
			try {
				Logger.error("x=" + x);
				Logger.error("y=" + y);
				Logger.error("z=" + z);
				Logger.error("url=" + url);
				Logger.error("cacheKey=" + cacheKey);

			} catch (Exception e) {
				// ignore
			} finally {

			}
		}
	}

	public static Tile getTile(DataInputStream in) throws IOException {
		Tile tempTile = new Tile();
		tempTile.unserialize(in);
		return tempTile;
	}

}


