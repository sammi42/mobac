/* *********************************************
 * Copyright: Andreas Sander
 *
 *
 * ********************************************* */

package rmp.rmpfile;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import javax.imageio.ImageIO;

import org.apache.log4j.Logger;

import rmp.interfaces.CalibratedImage;
import rmp.interfaces.RmpFileEntry;
import rmp.rmpfile.entries.GeneralRmpFileEntry;
import rmp.rmpmaker.BoundingRect;
import tac.exceptions.MapCreationException;
import tac.program.model.Settings;
import tac.utilities.Utilities;

/**
 * Class for building a TLM file from image and writing the file to a stream
 * 
 */
public class RmpLayer {
	private static final Logger log = Logger.getLogger(RmpLayer.class);

	private List<Tiledata> tiles;
	private byte[] A00File;
	private byte[] TLMFile;

	/**
	 * Constructor
	 */
	public RmpLayer() {
		A00File = null;
		TLMFile = null;
		tiles = new LinkedList<Tiledata>();
	}

	/**
	 * Return the content of the A00 file as byte array
	 */
	public RmpFileEntry getA00File(String image_name) {
		return new GeneralRmpFileEntry(A00File, image_name, "a00");
	}

	/**
	 * Return the content of the TLM file as byte array
	 */
	public RmpFileEntry getTLMFile(String image_name) {
		return new GeneralRmpFileEntry(TLMFile, image_name, "tlm");
	}

	/**
	 * Create a new instance of a TLM file and fill it with the data of a
	 * calibrated image
	 * 
	 * @param si
	 *            image to get data from
	 * @param layer
	 *            Layer number - for status output only
	 * @return TLM instance
	 * @throws InterruptedException
	 * @throws MapCreationException
	 */
	public static RmpLayer createFromImage(CalibratedImage si, int layer)
			throws InterruptedException, MapCreationException {
		log.debug(String.format("createFromSimpleImage(\n\t%s\n\t%d)", si, layer));

		RmpLayer result;
		double tile_width, tile_height;
		BoundingRect rect;
		BufferedImage img;
		int count = 0;

		/* --- Create instance --- */
		result = new RmpLayer();

		/* --- Get the coordinate space of the image --- */
		rect = si.getBoundingRect();

		/* --- Calculate tile dimensions --- */
		tile_width = (rect.getEast() - rect.getWest()) * 256 / si.getImageWidth();
		tile_height = (rect.getSouth() - rect.getNorth()) * 256 / si.getImageHeight();

		/*
		 * --- Check the theoretical maximum of horizontal and vertical position
		 * ---
		 */
		double pos_hor = 360 / tile_width;
		double pos_ver = 180 / tile_height;
		if (pos_hor > 0xFFFF || pos_ver >= 0xFFFF)
			throw new MapCreationException(
					"Map resolution too high - please select a lower zoom level");

		/* --- Calculate the positions of the upper left tile --- */
		int x_start = (int) Math.floor((rect.getWest() + 180) / tile_width);
		int y_start = (int) Math.floor((rect.getNorth() + 90) / tile_height);

		/* --- Create the tiles --- */
		for (int x = x_start; x * tile_width - 180 < rect.getEast(); x++) {
			for (int y = y_start; y * tile_height - 90 < rect.getSouth(); y++) {
				count++;
				if (Thread.currentThread().isInterrupted())
					throw new InterruptedException();
				if (log.isTraceEnabled())
					log.trace(String.format("Create tile %d layer=%d", count, layer));

				/* --- Create tile --- */
				img = si.getSubImage(new BoundingRect(y * tile_height - 90, (y + 1) * tile_height
						- 90, x * tile_width - 180, (x + 1) * tile_width - 180), 256, 256);
				result.addImage((int) x, (int) y, img);
			}
		}

		/* --- Build A00 file --- */
		result.buildA00File();

		/* --- Build the TLM file --- */
		result.buildTLMFile(tile_width, tile_height, rect.getWest(), rect.getEast(), rect
				.getNorth(), rect.getSouth());

		return result;
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
	 */
	private void addImage(int x, int y, BufferedImage image) {
		log.trace(String.format("addImage(x%d,y%d,w%d,h%d)", x, y, image.getWidth(), image
				.getHeight()));
		ByteArrayOutputStream bos;
		Tiledata tld;

		/* --- Convert to the image to JPG file format --- */
		bos = new ByteArrayOutputStream(8192);
		try {
			ImageIO.write(image, "jpg", bos);
		} catch (IOException e) {
			log.error("", e);
		}

		/* --- Create tiledata --- */
		byte[] data = bos.toByteArray();

		// TODO: Remove
		if (Settings.getInstance().isDevModeEnabled())
			Utilities.saveBytesEx(String.format("E:/TritonMap/jpg/%d_%d.jpg", x, y), data);

		tld = new Tiledata();
		tld.jpegFile = data;
		tld.posx = x;
		tld.posy = y;
		tld.totalOffset = 0;
		tiles.add(tld);
	}

	/**
	 * Builds A00 file format. This deletes the image from the tiledata
	 * therefore this operation cannot be repeated. It also sets the offset
	 * component in the tiledata which means that this function must be called
	 * before building the tile tree
	 */
	private void buildA00File() {
		ByteArrayOutputStream bos;
		int totaloffset = 4;
		int i;
		Tiledata tile;

		/* --- Create stream to write to --- */
		bos = new ByteArrayOutputStream(65536);

		try {
			/* --- Number of tiles --- */
			RmpTools.writeValue(bos, tiles.size(), 4);

			/* --- The tiles --- */
			for (i = 0; i < tiles.size(); i++) {
				/* --- Write tile into stream and calculate offset --- */
				tile = tiles.get(i);
				tile.totalOffset = totaloffset;
				totaloffset += tile.jpegFile.length + 4;
				RmpTools.writeValue(bos, tile.jpegFile.length, 4);
				bos.write(tile.jpegFile);

				/* --- Remove image from tiledata to save memory --- */
				tile.jpegFile = null;
			}

			/* --- Get the whole A00 file as byte array --- */
			A00File = bos.toByteArray();
			log.debug("A00File size: " + A00File.length);
		} catch (IOException e) {
			log.error("", e);
		}
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
	 */
	private void buildTLMFile(double tile_width, double tile_height, double left, double right,
			double top, double bottom) {
		ByteArrayOutputStream bos;
		int size;
		TileContainer container;

		/* --- Build the tile container --- */
		container = buildTileTree();

		/* --- Create Output file --- */
		bos = new ByteArrayOutputStream();

		try {
			/* --- header --- */
			RmpTools.writeValue(bos, 1, 4); // Start of block
			RmpTools.writeValue(bos, container.getTileCount(), 4); // Number of
			// tiles in
			// files
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
			RmpTools.writeValue(bos, 0x0f5c + ((container.getContainerCount() == 1) ? 0 : 1992), 4); // offset
			// for
			// first
			// block

			RmpTools.writeValue(bos, 0, 3920); // Filler

			/* --- Write the Tiledata --- */
			container.writeTree(bos);

			/* --- Add two empty blocks --- */
			RmpTools.writeValue(bos, 0, 1992 * 2);

			/* --- Get the file data --- */
			TLMFile = bos.toByteArray();
		} catch (IOException e) {
			log.error("", e);
		}
	}
}
