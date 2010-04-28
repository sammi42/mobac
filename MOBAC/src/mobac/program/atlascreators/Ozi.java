package mobac.program.atlascreators;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

import mobac.exceptions.AtlasTestException;
import mobac.exceptions.MapCreationException;
import mobac.mapsources.mapspace.MercatorPower2MapSpace;
import mobac.program.interfaces.MapInterface;
import mobac.program.model.TileImageFormat;
import mobac.utilities.Utilities;
import mobac.utilities.imageio.PngXxlWriter;
import mobac.utilities.tar.TarIndex;

import org.openstreetmap.gui.jmapviewer.interfaces.MapSource;
import org.openstreetmap.gui.jmapviewer.interfaces.MapSpace;
import org.openstreetmap.gui.jmapviewer.interfaces.MapSpace.ProjectionCategory;

public class Ozi extends TrekBuddy {

	protected File mapDir = null;
	protected String mapName = null;

	@Override
	public boolean testMapSource(MapSource mapSource) {
		MapSpace mapSpace = mapSource.getMapSpace();
		return (mapSpace instanceof MercatorPower2MapSpace && ProjectionCategory.SPHERE
				.equals(mapSpace.getProjectionCategory()));
		// TODO supports Mercator ellipsoid?
	}

	@Override
	protected void testAtlas() throws AtlasTestException {
		performTest_AtlasTileFormat(TileImageFormat.PNG);
	}

	@Override
	public void initializeMap(MapInterface map, TarIndex tarTileIndex) {
		super.initializeMap(map, tarTileIndex);
		mapDir = new File(atlasDir, map.getLayer().getName());
		mapName = map.getName();
	}

	public void createMap() throws MapCreationException, InterruptedException {
		try {
			Utilities.mkDir(mapDir);
		} catch (IOException e) {
			throw new MapCreationException(e);
		}
		createTiles();
		writeMapFile();
	}

	@Override
	protected void writeMapFile() {
		FileOutputStream fout = null;
		try {
			fout = new FileOutputStream(new File(mapDir, mapName + ".map"));
			writeMapFile(map.getName() + ".png", fout);
		} catch (Exception e) {
			log.error("", e);
		} finally {
			Utilities.closeStream(fout);
		}
	}

	/**
	 * Writes the large picture (tile) line by line. Each line has the full
	 * width of the map and the height of one tile (256 pixels).
	 */
	@Override
	protected void createTiles() throws InterruptedException, MapCreationException {
		atlasProgress.initMapCreation((xMax - xMin + 1) * (yMax - yMin + 1));
		ImageIO.setUseCache(false);

		int width = (xMax - xMin + 1) * tileSize;
		int height = (yMax - yMin + 1) * tileSize;
		int tileLineHeight = tileSize;

		FileOutputStream fileOs = null;
		try {
			fileOs = new FileOutputStream(new File(mapDir, mapName + ".png"));
			PngXxlWriter pngWriter = new PngXxlWriter(width, height, fileOs);

			for (int y = yMin; y <= yMax; y++) {
				BufferedImage lineImage = new BufferedImage(width, tileLineHeight,
						BufferedImage.TYPE_INT_RGB);
				Graphics2D graphics = lineImage.createGraphics();
				try {
					graphics.setColor(mapSource.getBackgroundColor());
					graphics.fillRect(0, 0, width, tileLineHeight);
					int lineX = 0;
					for (int x = xMin; x <= xMax; x++) {
						checkUserAbort();
						atlasProgress.incMapCreationProgress();
						try {
							BufferedImage tile = mapDlTileProvider.getTileImage(x, y);
							if (tile != null)
								graphics.drawImage(tile, lineX, 0, Color.WHITE, null);
						} catch (IOException e) {
							log.error("", e);
						}
						lineX += tileSize;
					}
				} finally {
					graphics.dispose();
				}
				pngWriter.writeTileLine(lineImage);
			}
			pngWriter.finish();
		} catch (IOException e) {
			log.error("", e);
		} finally {
			Utilities.closeStream(fileOs);
		}
	}
}