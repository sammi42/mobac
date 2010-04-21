package mobac.program.atlascreators;

import java.awt.Dimension;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.imageio.ImageIO;

import mobac.exceptions.MapCreationException;
import mobac.mapsources.mapspace.MercatorPower2MapSpace;
import mobac.program.ProgramInfo;
import mobac.program.atlascreators.tileprovider.ConvertedRawTileProvider;
import mobac.program.interfaces.MapInterface;
import mobac.utilities.tar.TarIndex;

import org.openstreetmap.gui.jmapviewer.interfaces.MapSource;

import psyberia.api.packer.PsyFlatPackCreator;

/**
 * Creates maps using the AlpineQuestMap atlas format.
 * 
 * AQM format pack tiles in a unique file using the FlatPack format.
 * 
 * @author Camille
 */
public class AlpineQuestMap extends AtlasCreator {

	private static final String[] SCALES = new String[] { "1:512 000 000", // 00
			"1:256 000 000", // 01
			"1:128 000 000", // 02
			"1:64 000 000", // 03
			"1:32 000 000", // 04
			"1:16 000 000", // 05
			"1:8 000 000", // 06
			"1:4 000 000", // 07
			"1:2 000 000", // 08
			"1:1 000 000", // 09
			"1:512 000", // 10
			"1:256 000", // 11
			"1:128 000", // 12
			"1:64 000", // 13
			"1:32 000", // 14
			"1:16 000", // 15
			"1:8 000", // 16
			"1:4 000", // 17
			"1:2 000", // 18
			"1:1 000", // 19
			"1:512", // 20
			"1:128", // 21
			"1:64", // 22
			"1:32", // 23
			"1:16", // 24
			"1:8", // 25
			"1:4", // 26
			"1:2", // 27
			"1:1" // 28
	};

	private File strMapFileName = null;
	private File strInfFileName = null;

	private PsyFlatPackCreator packCreator = null;

	private long nbTiles = 0;
	private String strFormat = null;
	private Dimension tileSize = null;

	@Override
	public boolean testMapSource(MapSource mapSource) {
		return MercatorPower2MapSpace.INSTANCE_256.equals(mapSource.getMapSpace());
	}

	@Override
	public void initializeMap(MapInterface map, TarIndex tarTileIndex) {
		super.initializeMap(map, tarTileIndex);

		// resize images
		if (parameters != null) {
			mapDlTileProvider = new ConvertedRawTileProvider(mapDlTileProvider, parameters
					.getFormat());
			strFormat = parameters.getFormat().getDataWriter().getFileExt();
			// tileSize = parameters.getDimension();
		} else {
			strFormat = mapSource.getTileType();
			// tileSize = map.getTileSize();
		}

		// no tile resizing
		tileSize = map.getTileSize();

		if (strFormat != null)
			strFormat = strFormat.toUpperCase();

		// number of tiles for all world
		nbTiles = Math.round(Math.pow(2, zoom));

		// used files
		strMapFileName = new File(atlasDir, mapSource.getName() + "_" + zoom + "_"
				+ map.getLayer().getName() + ".AQM");
		strInfFileName = new File(atlasDir, mapSource.getName() + "_" + zoom + "_"
				+ map.getLayer().getName() + ".INF");
	}

	public void createMap() throws MapCreationException, InterruptedException {
		try {
			packCreator = new PsyFlatPackCreator(strMapFileName.getAbsolutePath());

			// metadata information at the beginning
			addInfFile(packCreator);

			// add tiles
			addTiles();

			packCreator.close();
		} catch (Exception e) {
			throw new MapCreationException(e);
		}
	}

	private final void addTiles() throws InterruptedException, MapCreationException {
		atlasProgress.initMapCreation((xMax - xMin + 1) * (yMax - yMin + 1));
		ImageIO.setUseCache(false);

		for (int x = xMin; x <= xMax; x++) {
			for (int y = yMin; y <= yMax; y++) {
				checkUserAbort();
				atlasProgress.incMapCreationProgress();

				try {
					byte[] sourceTileData = mapDlTileProvider.getTileData(x, y);
					if (sourceTileData != null) {
						/* y tiles count starts bottom in AQM */
						packCreator.add(sourceTileData, "" + x + "_" + (nbTiles - y));
					}
				} catch (IOException e) {
					throw new MapCreationException("Error writing tile image: " + e.getMessage(), e);
				}
			}
		}
	}

	private final void addInfFile(final PsyFlatPackCreator pack) throws IOException {
		// version of the AQM format (internal use)
		String strVersion = "1";

		// projection of tiles (internal use)
		String strProjection = "mercator";

		// unique identifier for a data source / zoom (internal use)
		String strID = mapSource.getName() + "_" + zoom;

		// software used to create the map (displayed to user)
		String strSoftware = ProgramInfo.getCompleteTitle();

		// date of creation (displayed to user)
		String strDate = new SimpleDateFormat("yyyy/MM/dd").format(new Date());

		// source of the map data (displayed to user)
		String strDataSource = mapSource.toString();

		// name of the person that created the map (displayed to user)
		String strCreator = "MobileAtlasCreator";

		// copyright of map data (displayed to user)
		String strCopyright = mapSource.toString();

		// name of this specific map (displayed to user)
		String strName = map.getLayer().getName();
		if (strName == null || strName.length() == 0)
			strName = "Unnamed";

		// scale of the map (displayed to user)
		String strScale = "";
		if (zoom >= 0 && zoom < SCALES.length)
			strScale = SCALES[zoom];

		// write metadata file
		FileWriter w = new FileWriter(strInfFileName);
		w.write("id         = " + strID + "\n");
		w.write("name       = " + strName + "\n");
		w.write("version    = " + strVersion + "\n");
		w.write("date       = " + strDate + "\n");
		w.write("scale      = " + strScale + "\n");
		w.write("datasource = " + strDataSource + "\n");
		w.write("creator    = " + strCreator + "\n");
		w.write("software   = " + strSoftware + "\n");
		w.write("copyright  = " + strCopyright + "\n");
		w.write("projection = " + strProjection + "\n");
		w.write("xtsize     = " + (int) tileSize.getWidth() + "\n");
		w.write("ytsize     = " + (int) tileSize.getHeight() + "\n");
		w.write("xtratio    = " + (nbTiles / 360.0) + "\n");
		w.write("ytratio    = " + (nbTiles / 360.0) + "\n");
		w.write("xtoffset   = " + (nbTiles / 2.0) + "\n");
		w.write("ytoffset   = " + (nbTiles / 2.0) + "\n");
		w.write("xtmin      = " + xMin + "\n");
		w.write("xtmax      = " + xMax + "\n");
		w.write("ytmin      = " + (nbTiles - yMax) + "\n");
		w.write("ytmax      = " + (nbTiles - yMin) + "\n");
		w.write("background = " + "#FFFFFF" + "\n");
		w.write("imgformat  = " + strFormat + "\n");
		w.flush();
		w.close();

		// add the metadata file into map
		packCreator.add(strInfFileName, "INF");

		// remove the metadata file
		strInfFileName.delete();
	}

}