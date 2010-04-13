package mobac.program.atlascreators;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import mobac.exceptions.AtlasTestException;
import mobac.exceptions.MapCreationException;
import mobac.mapsources.mapspace.MercatorPower2MapSpace;
import mobac.program.atlascreators.impl.MapTileWriter;
import mobac.program.interfaces.AtlasInterface;
import mobac.program.interfaces.LayerInterface;
import mobac.program.interfaces.MapInterface;
import mobac.program.model.TileImageParameters;
import mobac.utilities.Utilities;
import mobac.utilities.tar.TarIndex;

import org.openstreetmap.gui.jmapviewer.interfaces.MapSource;
import org.openstreetmap.gui.jmapviewer.interfaces.MapSpace;
import org.openstreetmap.gui.jmapviewer.interfaces.MapSpace.ProjectionCategory;

/**
 * 
 * Implementation incomplete
 * 
 * TODO: Implement
 *
 */
public class AFTrack extends TrekBuddyCustom {

	public AFTrack() {

	}

	public boolean testMapSource(MapSource mapSource) {
		MapSpace mapSpace = mapSource.getMapSpace();
		return (mapSpace instanceof MercatorPower2MapSpace && ProjectionCategory.SPHERE
				.equals(mapSpace.getProjectionCategory()));
	}

	public void startAtlasCreation(AtlasInterface atlas) throws IOException, InterruptedException,
			AtlasTestException {
		super.startAtlasCreation(atlas);
		for (LayerInterface layer : atlas) {
			for (MapInterface map : layer) {
				TileImageParameters param = map.getParameters();
				if (param == null)
					continue;
				if (param.getHeight() != 256 || param.getWidth() != 256)
					throw new AtlasTestException("Custom tile size is not supported by this atlas");
			}
		}
	}

	@Override
	public void initializeMap(MapInterface map, TarIndex tarTileIndex) {
		super.initializeMap(map, tarTileIndex);
	}

	@Override
	public void createMap() throws MapCreationException, InterruptedException {
		try {
			mapTileWriter = new OszTileWriter();
		} catch (FileNotFoundException e) {
			throw new MapCreationException(e);
		}
	}

	private class OszTileWriter implements MapTileWriter {

		ZipOutputStream zipStream;
		FileOutputStream out;

		public OszTileWriter() throws FileNotFoundException {
			super();
			out = new FileOutputStream("test.zip");
			zipStream = new ZipOutputStream(out);
		}

		public void writeTile(int tilex, int tiley, String tileType, byte[] tileData)
				throws IOException {
			ZipEntry entry = new ZipEntry("test");

			zipStream.putNextEntry(entry);
			zipStream.write(tileData);
			zipStream.closeEntry();
		}

		public void finalizeMap() {
			Utilities.closeStream(zipStream);
		}

	}

}
