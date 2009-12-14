/* *********************************************
 * Copyright: Andreas Sander
 *
 *
 * ********************************************* */

package tac.program.atlascreators.impl.rmp;

import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;

import javax.imageio.ImageIO;

import org.apache.log4j.Logger;

import tac.program.atlascreators.impl.rmp.interfaces.RmpFileEntry;
import tac.program.atlascreators.impl.rmp.rmpfile.GeneralRmpFileEntry;

/**
 * Class for building a TLM file from image and writing the file to a stream
 * 
 */
public class RmpLayer {
	private static final Logger log = Logger.getLogger(RmpLayer.class);

	private List<Tiledata> tiles;
	private byte[] TLMFile;

	/**
	 * Constructor
	 */
	public RmpLayer() {
		TLMFile = null;
		tiles = new LinkedList<Tiledata>();
	}

	/**
	 * Return the content of the A00 file as byte array
	 */
	public RmpFileEntry getA00File(String image_name) {
		return new A00Entry(image_name);
	}

	/**
	 * Return the content of the TLM file as byte array
	 */
	public RmpFileEntry getTLMFile(String image_name) {
		return new GeneralRmpFileEntry(TLMFile, image_name, "tlm");
	}

	/**
	 * Adds an image to the internal list of tiles
	 * 
	 * @param x
	 *            X-position (in RMP-Units)
	 * @param y
	 *            Y-position
	 * @param image
	 *            image
	 * @throws IOException
	 */
	public void addImage(int x, int y, BufferedImage image) throws IOException {
		log.trace(String.format("addImage(x%d,y%d,w%d,h%d)", x, y, image.getWidth(), image
				.getHeight()));
		ByteArrayOutputStream bos;
		Tiledata tld;

		/* --- Convert to the image to JPG file format --- */
		bos = new ByteArrayOutputStream(8192);
		ImageIO.write(image, "jpg", bos);

		/* --- Create tiledata --- */
		byte[] data = bos.toByteArray();

		// if (Settings.getInstance().isDevModeEnabled())
		// Utilities.saveBytesEx(String.format("E:/TritonMap/jpg/%d_%d.jpg", x,
		// y), data);

		tld = new Tiledata();
		tld.jpegFile = data;
		tld.posx = x;
		tld.posy = y;
		tld.totalOffset = 0;
		tiles.add(tld);
	}

	/**
	 * distribute the tiles over containers of max 256 tiles
	 */
	private TileContainer buildTileTree() {
		TileContainer[] container;
		TileContainer indexContainer = null;
		int containerCount;

		/*
		 * --- Calculate the number of tiles and tiles per container. 99 would
		 * be possible but we limit ourselves to 80 - That's enough ---
		 */
		int count = tiles.size();
		containerCount = count / 80;
		if (count % 80 != 0)
			containerCount++;

		int tilesPerContainer = count / containerCount;

		/* --- Create containers --- */
		container = new TileContainer[containerCount];
		for (int i = 0; i < containerCount; i++)
			container[i] = new TileContainer();

		/*
		 * --- We need an index container if there is more than one container.
		 * Container 0 is the previous of the index container ---
		 */
		if (containerCount > 1)
			indexContainer = new TileContainer(container[0]);

		/* --- Place the tiles into the container --- */
		int tile_count = 0;
		int container_number = 0;
		for (int i = 0; i < tiles.size(); i++) {
			/*
			 * --- Starting with the second container, the first element is
			 * moved to the index container ---
			 */
			if (tile_count == 0 && container_number != 0)
				indexContainer.addTile(tiles.get(i), container[container_number]);
			else
				container[container_number].addTile(tiles.get(i), null);

			/* --- Switch to next container if we reach end of container --- */
			tile_count++;
			if (tile_count == tilesPerContainer) {
				container_number++;
				tile_count = 0;

				/*
				 * --- Recalculate the number of tiles per container because of
				 * rounding issues
				 */
				if (containerCount != container_number)
					tilesPerContainer = (count - (i + 1)) / (containerCount - container_number);
			}
		}

		/*
		 * --- If we have multiple containers, then the index container is the
		 * result, otherwise the single container.
		 */
		if (indexContainer == null)
			return container[0];
		else
			return indexContainer;
	}

	/**
	 * Create the TLM file from the TileContainer infos
	 * 
	 * @throws IOException
	 */
	public void buildTLMFile(double tile_width, double tile_height, double left, double right,
			double top, double bottom) throws IOException {
		ByteArrayOutputStream bos;
		int size;
		TileContainer container;

		// calculate offset of each tile in A00 file
		int totaloffset = 4;
		for (Tiledata tile : tiles) {
			tile.totalOffset = totaloffset;
			totaloffset += tile.jpegFile.length + 4;
		}

		/* --- Build the tile container --- */
		container = buildTileTree();

		/* --- Create Output file --- */
		bos = new ByteArrayOutputStream();

		/* --- header --- */
		RmpTools.writeValue(bos, 1, 4); // Start of block
		RmpTools.writeValue(bos, container.getTileCount(), 4); // Number of
		// tiles in files
		RmpTools.writeValue(bos, 256, 2); // Hor. size of tile in pixel
		RmpTools.writeValue(bos, 256, 2); // Vert. size of tile in pixel
		RmpTools.writeValue(bos, 1, 4); // Start of block
		RmpTools.writeDouble(bos, tile_height); // Height of tile in degree
		RmpTools.writeDouble(bos, tile_width); // Tile width in degree
		RmpTools.writeDouble(bos, left); // Frame
		RmpTools.writeDouble(bos, top); // Frame
		RmpTools.writeDouble(bos, right); // Frame
		RmpTools.writeDouble(bos, bottom); // Frame

		RmpTools.writeValue(bos, 0, 88); // Filler

		RmpTools.writeValue(bos, 256, 2); // Tile size ????
		RmpTools.writeValue(bos, 0, 2); // Filler

		size = 256 + 1940 + 3 * 1992;
		size += container.getContainerCount() * 1992;
		if (container.getContainerCount() != 1)
			size += 1992;
		RmpTools.writeValue(bos, size, 4); // File size

		RmpTools.writeValue(bos, 0, 96); // Filler

		RmpTools.writeValue(bos, 1, 4); // Start of block
		RmpTools.writeValue(bos, 99, 4); // Number of tiles in block
		int firstBlockOffset = 0x0f5c + ((container.getContainerCount() == 1) ? 0 : 1992);
		RmpTools.writeValue(bos, firstBlockOffset, 4); // offset for first block

		RmpTools.writeValue(bos, 0, 3920); // Filler

		/* --- Write the Tiledata --- */
		container.writeTree(bos);

		/* --- Add two empty blocks --- */
		RmpTools.writeValue(bos, 0, 1992 * 2);

		/* --- Get the file data --- */
		TLMFile = bos.toByteArray();
	}

	protected class A00Entry implements RmpFileEntry {

		protected String name;

		public A00Entry(String name) {
			super();
			this.name = name;
		}

		public String getFileExtension() {
			return "a00";
		}

		public String getFileName() {
			return name;
		}

		public void writeFileContent(OutputStream os) throws IOException {
			BufferedOutputStream bos = new BufferedOutputStream(os, 32768);
			/* --- Number of tiles --- */
			RmpTools.writeValue(bos, tiles.size(), 4);

			/* --- The tiles --- */
			for (Tiledata tile : tiles) {
				RmpTools.writeValue(bos, tile.jpegFile.length, 4);
				bos.write(tile.jpegFile);

				/* --- Remove image from tiledata to save memory --- */
				tile.jpegFile = null;
			}
			bos.flush();
		}

		@Override
		public String toString() {
			return "A00Entry";
		}

	}
}
