package tac.program.atlascreators;

import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Set;

import javax.imageio.ImageIO;

import org.openstreetmap.gui.jmapviewer.interfaces.MapSource;

import tac.exceptions.MapCreationException;
import tac.mapsources.MultiLayerMapSource;
import tac.mapsources.mapspace.MercatorPower2MapSpace;
import tac.program.interfaces.AtlasInterface;
import tac.program.interfaces.MapInterface;
import tac.tar.TarIndex;

/**
 * Creates maps using the Mobile Trail Explorer (MTE) cache format.
 * 
 * Please note that this atlas format ignores the defined atlas structure.
 * 
 */
public class MobileTrailExplorerCache extends AtlasCreator {

	protected DataOutputStream cacheOutStream = null;
	protected long lastTileOffset = 0;
	protected Set<String> availableTileList = new HashSet<String>();

	protected SimpleMTECacheTileWriter mteCacheWriter = null;

	@Override
	public boolean testMapSource(MapSource mapSource) {
		if (mapSource instanceof MultiLayerMapSource)
			return false;

		return MercatorPower2MapSpace.INSTANCE_256.equals(mapSource.getMapSpace());
	}

	@Override
	public void startAtlasCreation(AtlasInterface atlas) throws IOException {
		super.startAtlasCreation(atlas);
		File cacheFile = new File(atlasDir, "MTEFileCache");
		OutputStream out = new BufferedOutputStream(new FileOutputStream(cacheFile), 8216);
		cacheOutStream = new DataOutputStream(out);
	}

	@Override
	public void finishAtlasCreation() throws IOException {
		super.finishAtlasCreation();
		cacheOutStream.close();
	}

	@Override
	public void initializeMap(MapInterface map, TarIndex tarTileIndex) {
		super.initializeMap(map, tarTileIndex);
	}

	public void createMap() throws MapCreationException {
		try {
			if ("png".equalsIgnoreCase(mapSource.getTileType()))
				mteCacheWriter = new SimpleMTECacheTileWriter();
			else
				// If the tile image format is not png we have to convert it
				mteCacheWriter = new PngMTECacheTileWriter();
			createTiles();
		} catch (InterruptedException e) {
			// User has aborted process
			return;
		}
	}

	@Override
	protected void createTiles() throws InterruptedException, MapCreationException {
		atlasProgress.initMapCreation((xMax - xMin + 1) * (yMax - yMin + 1));
		ImageIO.setUseCache(false);
		String mapName = map.getMapSource().getName().replaceAll(" ", "_");

		for (int x = xMin; x <= xMax; x++) {
			for (int y = yMin; y <= yMax; y++) {
				checkUserAbort();
				atlasProgress.incMapCreationProgress();
				try {
					byte[] sourceTileData = mapDlTileProvider.getTileData(x, y);
					if (sourceTileData != null)
						mteCacheWriter.writeTile(mapName, sourceTileData, x, y, zoom);
				} catch (IOException e) {
					log.error("", e);
				}
			}
		}
	}

	/**
	 * Simply writes the tileData to the specified file
	 */
	protected class SimpleMTECacheTileWriter {

		public boolean writeTile(String cache, byte[] tileData, int x, int y, int zoom)
				throws IOException {
			String url = "not used";
			String cacheKey = cache + "-" + zoom + "-" + x + "-" + y;

			if (availableTileList.contains(cacheKey))
				return false;

			cacheOutStream.writeInt(x);
			cacheOutStream.writeInt(y);
			cacheOutStream.writeInt(zoom);

			byte[] urlBytes = url.getBytes();
			cacheOutStream.writeShort(urlBytes.length);
			cacheOutStream.write(urlBytes);

			byte[] keyBytes = cacheKey.getBytes();
			cacheOutStream.writeShort(keyBytes.length);
			cacheOutStream.write(keyBytes);
			// Logger.debug(Long.toString(lastTileOffset));
			cacheOutStream.writeLong(lastTileOffset);

			lastTileOffset += 12 + // x, y and z
					2 + urlBytes.length + // strings and their lengths
					2 + keyBytes.length + 8 + // tile offset (long)
					4 + // image byte array length (int)
					tileData.length;

			cacheOutStream.writeInt(tileData.length);
			cacheOutStream.write(tileData);
			return true;
		}

	}

	/**
	 * Converts the image to be saved to png.
	 */
	protected class PngMTECacheTileWriter extends SimpleMTECacheTileWriter {

		@Override
		public boolean writeTile(String cache, byte[] tileData, int x, int y, int zoom)
				throws IOException {
			BufferedImage image = ImageIO.read(new ByteArrayInputStream(tileData));
			ByteArrayOutputStream buf = new ByteArrayOutputStream(32768);
			ImageIO.write(image, "png", buf);
			return super.writeTile(cache, buf.toByteArray(), x, y, zoom);
		}
	}

}