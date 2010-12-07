/*******************************************************************************
 * Copyright (c) MOBAC developers
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package mobac.mapsources;

import java.io.File;
import java.io.IOException;
import java.security.cert.CertificateException;
import java.util.Comparator;
import java.util.TreeSet;
import java.util.Vector;

import mobac.mapsources.impl.DebugMapSource;
import mobac.mapsources.impl.LocalhostTestSource;
import mobac.program.interfaces.MapSource;
import mobac.program.mappack.MapPackManager;
import mobac.program.model.Settings;
import mobac.program.model.TileImageType;

public class DefaultMapSourcesManager extends MapSourcesManager {

	//public static final MapSource DEFAULT = new Mapnik();
	private MapSource[] MAP_SOURCES;

	private static MapSource LOCALHOST_TEST_MAPSOURCE = new LocalhostTestSource("Localhost", TileImageType.PNG);
	private static MapSource DEBUG_TEST_MAPSOURCE = new DebugMapSource();

	static {
		MapSourcesUpdater.loadMapSourceProperties();
	}

	public DefaultMapSourcesManager() {
		try {
			MapPackManager mpm = new MapPackManager(new File("mapsources"));
			mpm.loadMapPacks();
			MAP_SOURCES = mpm.getMapSources();
		} catch (IOException e) {
			throw new RuntimeException(e);
		} catch (CertificateException e) {
			throw new RuntimeException(e);
		}
		// MAP_SOURCES = new MapSource[] { //
		// //
		// new GoogleMaps(), //
		// new GoogleMapMaker(), //
		// new GoogleMapsChina(), //
		// new GoogleMapsKorea(), //
		// new GoogleEarth(), //
		// new GoogleHybrid(), //
		// new GoogleTerrain(), //
		// new YahooMaps(), //
		// new YahooMapsJapan(), //
		// new YahooMapsTaiwan(), //
		// DEFAULT, //
		// new TilesAtHome(), //
		// new CycleMap(), //
		// new OsmHikingMap(), //
		// new OsmHikingMapWithBase(), //
		// new OsmHikingMapWithRelief(), //
		// new OsmHikingMapWithReliefBase(), //
		// new OpenSeaMap(), //
		// new Hikebikemap(), //
		// new OsmPublicTransport(), //
		// new OpenPisteMap(), //
		// new MicrosoftMaps(), //
		// new MicrosoftMapsHillShade(),//
		// new MicrosoftMapsChina(),//
		// new MicrosoftVirtualEarth(),//
		// new MicrosoftHybrid(), //
		// new OviMaps(), //
		// new OutdooractiveGermany(),//
		// new OutdooractiveAustria(),//
		// new OutdooractiveSouthTyrol(), //
		// new MultimapCom(), //
		// new MultimapOSUkCom(),//
		// new Cykloatlas(), //
		// new CykloatlasWithRelief(), //
		// new TerraserverUSA(), //
		// new MyTopo(), //
		// new UmpWawPl(),//
		// new DoCeluPL(), //
		// new EmapiPl(), //
		// new Bergfex(), //
		// new FreemapSlovakia(), //
		// new FreemapSlovakiaHiking(),//
		// new FreemapSlovakiaCycling(), //
		// new Turaterkep(), //
		// new NearMap(), //
		// new HubermediaBavaria(),//
		// new StatkartTopo2(), //
		// new StatkartToporaster2(), //
		// new StatkartSjoHovedkart2(), //
		// new EniroComMap(), //
		// new EniroComAerial(), //
		// new EniroComNautical(), //
		// new MapplusCh(), //
		// new YandexMap(), //
		// new YandexSat(), //
		// new Navitel(), //
		// new MicrosoftOrdnanceSurveyExplorer(), //
		// new AeroChartsVFR(), //
		// new AeroChartsIFR(),//
		// new AeroChartsIFRH(), //
		// new Sigpac(), //
		// new NzTopoMaps(), //
		// new Topomapper() //
		// };
	}

	public static void initialize() {
		INSTANCE = new DefaultMapSourcesManager();
	}

	@Override
	public Vector<MapSource> getAllMapSources() {
		Vector<MapSource> mapSources = new Vector<MapSource>();
		if (Settings.getInstance().isDevModeEnabled()) {
			mapSources.add(LOCALHOST_TEST_MAPSOURCE);
			mapSources.add(DEBUG_TEST_MAPSOURCE);
		}
		for (MapSource ms : MAP_SOURCES)
			mapSources.add(ms);
		for (MapSource ms : Settings.getInstance().customMapSources)
			mapSources.add(ms);
		return mapSources;
	}

	@Override
	public Vector<MapSource> getAllLayerMapSources() {
		Vector<MapSource> all = getAllMapSources();
		TreeSet<MapSource> uniqueSources = new TreeSet<MapSource>(new Comparator<MapSource>() {

			public int compare(MapSource o1, MapSource o2) {
				return o1.getName().compareTo(o2.getName());
			}

		});
		for (MapSource ms : all) {
			if (ms instanceof AbstractMultiLayerMapSource) {
				for (MapSource lms : ((AbstractMultiLayerMapSource) ms)) {
					uniqueSources.add(lms);
				}
			} else
				uniqueSources.add(ms);
		}
		Vector<MapSource> result = new Vector<MapSource>(uniqueSources);
		return result;
	}

	@Override
	public Vector<MapSource> getEnabledMapSources() {
		Vector<MapSource> mapSources = new Vector<MapSource>();
		if (Settings.getInstance().isDevModeEnabled()) {
			mapSources.add(LOCALHOST_TEST_MAPSOURCE);
			mapSources.add(DEBUG_TEST_MAPSOURCE);
		}
		TreeSet<String> disabledMapSources = new TreeSet<String>(Settings.getInstance().getDisabledMapSources());
		for (MapSource ms : MAP_SOURCES) {
			if (!disabledMapSources.contains(ms.getName()))
				mapSources.add(ms);
		}
		for (MapSource ms : Settings.getInstance().customMapSources)
			mapSources.add(ms);
		return mapSources;
	}

	@Override
	public MapSource getDefaultMapSource() {
		return getSourceByName("Mapnik");//DEFAULT;
	}

	@Override
	public MapSource getSourceByName(String name) {
		for (MapSource ms : MAP_SOURCES) {
			if (ms.getName().equals(name))
				return ms;
		}
		for (MapSource ms : Settings.getInstance().customMapSources) {
			if (ms.getName().equals(name))
				return ms;
		}
		if (Settings.getInstance().isDevModeEnabled()) {
			if (LOCALHOST_TEST_MAPSOURCE.getName().equals(name))
				return LOCALHOST_TEST_MAPSOURCE;
			if (DEBUG_TEST_MAPSOURCE.getName().equals(name))
				return DEBUG_TEST_MAPSOURCE;
		}
		return null;
	}

}
