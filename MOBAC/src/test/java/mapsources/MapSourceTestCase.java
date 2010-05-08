package mapsources;

import java.io.IOException;
import java.net.HttpURLConnection;

import junit.framework.TestCase;
import mobac.program.download.TileDownLoader;
import mobac.program.model.EastNorthCoordinate;
import mobac.program.model.Settings;
import mobac.tools.Cities;
import mobac.utilities.Utilities;

import org.openstreetmap.gui.jmapviewer.interfaces.MapSource;
import org.openstreetmap.gui.jmapviewer.interfaces.MapSpace;

public class MapSourceTestCase extends TestCase {

	private final MapSource mapSource;
	private final EastNorthCoordinate testCoordinate;

	public MapSourceTestCase(Class<? extends MapSource> mapSourceClass)
			throws InstantiationException, IllegalAccessException {
		this(mapSourceClass.newInstance());
	}

	public MapSourceTestCase(MapSource mapSource) {
		this(mapSource, Cities.getTestCoordinate(mapSource, Cities.BERLIN));
	}

	public MapSourceTestCase(MapSource mapSource, EastNorthCoordinate testCoordinate) {
		super(mapSource.getName());
		this.mapSource = mapSource;
		this.testCoordinate = testCoordinate;
	}

	public void runMapSourceTest() throws IOException, MapSourceTestFailedException {
		runTest();
	}

	@Override
	protected void runTest() throws IOException, MapSourceTestFailedException {
		int zoom = mapSource.getMaxZoom();

		MapSpace mapSpace = mapSource.getMapSpace();
		int tilex = mapSpace.cLonToX(testCoordinate.lon, zoom) / mapSpace.getTileSize();
		int tiley = mapSpace.cLatToY(testCoordinate.lat, zoom) / mapSpace.getTileSize();

		HttpURLConnection c = mapSource.getTileUrlConnection(zoom, tilex, tiley);
		c.setReadTimeout(10000);
		c.addRequestProperty("User-agent", Settings.getInstance().getUserAgent());
		c.setRequestProperty("Accept", TileDownLoader.ACCEPT);

		c.connect();
		try {
			// if (c.getResponseCode() == 302) {
			// log.debug(c.getResponseMessage());
			// }
			if (c.getResponseCode() != 200) {
				throw new MapSourceTestFailedException(mapSource, c);
			}
			byte[] imageData = Utilities.getInputBytes(c.getInputStream());
			if (imageData.length == 0)
				throw new MapSourceTestFailedException(mapSource, "Image data empty", c);
			if (Utilities.getImageDataFormat(imageData) == null) {
				throw new MapSourceTestFailedException(mapSource, "Image data of unknown format", c);
			}
			switch (mapSource.getTileUpdate()) {
			case ETag:
			case IfNoneMatch:
				if (c.getHeaderField("ETag") == null) {
					throw new MapSourceTestFailedException(mapSource,
							"No ETag present but map sources uses " + mapSource.getTileUpdate()
									+ "\n", c);
				}
				break;
			case LastModified:
				if (c.getHeaderField("Last-Modified") == null)
					throw new MapSourceTestFailedException(mapSource,
							"No Last-Modified entry present but map sources uses "
									+ mapSource.getTileUpdate() + "\n", c);
				break;
			}
		} finally {
			c.disconnect();
		}
	}
}
