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

import java.util.Comparator;
import java.util.TreeSet;
import java.util.Vector;

import mobac.mapsources.impl.DebugMapSource;
import mobac.mapsources.impl.LocalhostTestSource;
import mobac.mapsources.impl.Google.GoogleEarth;
import mobac.mapsources.impl.Google.GoogleHybrid;
import mobac.mapsources.impl.Google.GoogleMapMaker;
import mobac.mapsources.impl.Google.GoogleMaps;
import mobac.mapsources.impl.Google.GoogleMapsChina;
import mobac.mapsources.impl.Google.GoogleMapsKorea;
import mobac.mapsources.impl.Google.GoogleTerrain;
import mobac.mapsources.impl.Microsoft.MicrosoftHybrid;
import mobac.mapsources.impl.Microsoft.MicrosoftMaps;
import mobac.mapsources.impl.Microsoft.MicrosoftMapsChina;
import mobac.mapsources.impl.Microsoft.MicrosoftMapsHillShade;
import mobac.mapsources.impl.Microsoft.MicrosoftOrdnanceSurveyExplorer;
import mobac.mapsources.impl.Microsoft.MicrosoftVirtualEarth;
import mobac.mapsources.impl.MiscMapSources.MultimapCom;
import mobac.mapsources.impl.MiscMapSources.MultimapOSUkCom;
import mobac.mapsources.impl.MiscMapSources.Navitel;
import mobac.mapsources.impl.MiscMapSources.OviMaps;
import mobac.mapsources.impl.MiscMapSources.YahooMaps;
import mobac.mapsources.impl.MiscMapSources.YahooMapsJapan;
import mobac.mapsources.impl.MiscMapSources.YahooMapsTaiwan;
import mobac.mapsources.impl.MiscMapSources.YandexMap;
import mobac.mapsources.impl.MiscMapSources.YandexSat;
import mobac.mapsources.impl.OsmMapSources.CycleMap;
import mobac.mapsources.impl.OsmMapSources.Hikebikemap;
import mobac.mapsources.impl.OsmMapSources.Mapnik;
import mobac.mapsources.impl.OsmMapSources.OpenPisteMap;
import mobac.mapsources.impl.OsmMapSources.OpenSeaMap;
import mobac.mapsources.impl.OsmMapSources.OsmHikingMap;
import mobac.mapsources.impl.OsmMapSources.OsmHikingMapWithBase;
import mobac.mapsources.impl.OsmMapSources.OsmHikingMapWithRelief;
import mobac.mapsources.impl.OsmMapSources.OsmHikingMapWithReliefBase;
import mobac.mapsources.impl.OsmMapSources.OsmPublicTransport;
import mobac.mapsources.impl.OsmMapSources.TilesAtHome;
import mobac.mapsources.impl.OsmMapSources.Turaterkep;
import mobac.mapsources.impl.RegionalMapSources.AeroChartsIFR;
import mobac.mapsources.impl.RegionalMapSources.AeroChartsIFRH;
import mobac.mapsources.impl.RegionalMapSources.AeroChartsVFR;
import mobac.mapsources.impl.RegionalMapSources.Bergfex;
import mobac.mapsources.impl.RegionalMapSources.Cykloatlas;
import mobac.mapsources.impl.RegionalMapSources.CykloatlasWithRelief;
import mobac.mapsources.impl.RegionalMapSources.DoCeluPL;
import mobac.mapsources.impl.RegionalMapSources.EmapiPl;
import mobac.mapsources.impl.RegionalMapSources.EniroComAerial;
import mobac.mapsources.impl.RegionalMapSources.EniroComMap;
import mobac.mapsources.impl.RegionalMapSources.EniroComNautical;
import mobac.mapsources.impl.RegionalMapSources.FreemapSlovakia;
import mobac.mapsources.impl.RegionalMapSources.FreemapSlovakiaCycling;
import mobac.mapsources.impl.RegionalMapSources.FreemapSlovakiaHiking;
import mobac.mapsources.impl.RegionalMapSources.HubermediaBavaria;
import mobac.mapsources.impl.RegionalMapSources.MapplusCh;
import mobac.mapsources.impl.RegionalMapSources.MyTopo;
import mobac.mapsources.impl.RegionalMapSources.NearMap;
import mobac.mapsources.impl.RegionalMapSources.NzTopoMaps;
import mobac.mapsources.impl.RegionalMapSources.OutdooractiveAustria;
import mobac.mapsources.impl.RegionalMapSources.OutdooractiveGermany;
import mobac.mapsources.impl.RegionalMapSources.OutdooractiveSouthTyrol;
import mobac.mapsources.impl.RegionalMapSources.Sigpac;
import mobac.mapsources.impl.RegionalMapSources.StatkartSjoHovedkart2;
import mobac.mapsources.impl.RegionalMapSources.StatkartTopo2;
import mobac.mapsources.impl.RegionalMapSources.StatkartToporaster2;
import mobac.mapsources.impl.RegionalMapSources.UmpWawPl;
import mobac.mapsources.impl.WmsSources.TerraserverUSA;
import mobac.program.interfaces.MapSource;
import mobac.program.model.Settings;
import mobac.program.model.TileImageType;

public class DefaultMapSourcesManager extends MapSourcesManager {

	public static final MapSource DEFAULT = new Mapnik();
	private MapSource[] MAP_SOURCES;

	private static MapSource LOCALHOST_TEST_MAPSOURCE = new LocalhostTestSource("Localhost", TileImageType.PNG);
	private static MapSource DEBUG_TEST_MAPSOURCE = new DebugMapSource();

	static {
		MapSourcesUpdater.loadMapSourceProperties();
	}

	public DefaultMapSourcesManager() {
		MAP_SOURCES = new MapSource[] { //
		//
				new GoogleMaps(), // 
				new GoogleMapMaker(), //
				new GoogleMapsChina(), // 
				new GoogleMapsKorea(), // 
				new GoogleEarth(), //
				new GoogleHybrid(), // 
				new GoogleTerrain(), // 
				new YahooMaps(), //
				new YahooMapsJapan(), // 
				new YahooMapsTaiwan(), //
				DEFAULT, //
				new TilesAtHome(), // 
				new CycleMap(), // 
				new OsmHikingMap(), // 
				new OsmHikingMapWithBase(), //
				new OsmHikingMapWithRelief(), // 
				new OsmHikingMapWithReliefBase(), //
				new OpenSeaMap(), //
				new Hikebikemap(), //
				new OsmPublicTransport(), // 
				new OpenPisteMap(), //
				new MicrosoftMaps(), // 
				new MicrosoftMapsHillShade(),//  
				new MicrosoftMapsChina(),// 
				new MicrosoftVirtualEarth(),// 
				new MicrosoftHybrid(), // 
				new OviMaps(), // 
				new OutdooractiveGermany(),// 
				new OutdooractiveAustria(),//  
				new OutdooractiveSouthTyrol(), // 
				new MultimapCom(), // 
				new MultimapOSUkCom(),// 
				new Cykloatlas(), // 
				new CykloatlasWithRelief(), // 
				new TerraserverUSA(), // 
				new MyTopo(), // 
				new UmpWawPl(),// 
				new DoCeluPL(), // 
				new EmapiPl(), // 
				new Bergfex(), // 
				new FreemapSlovakia(), // 
				new FreemapSlovakiaHiking(),// 
				new FreemapSlovakiaCycling(), // 
				new Turaterkep(), // 
				new NearMap(), // 
				new HubermediaBavaria(),// 
				new StatkartTopo2(), // 
				new StatkartToporaster2(), // 
				new StatkartSjoHovedkart2(), // 
				new EniroComMap(), // 
				new EniroComAerial(), // 
				new EniroComNautical(), // 
				new MapplusCh(), // 
				new YandexMap(), // 
				new YandexSat(), // 
				new Navitel(), // 
				new MicrosoftOrdnanceSurveyExplorer(), // 
				new AeroChartsVFR(), // 
				new AeroChartsIFR(),// 
				new AeroChartsIFRH(), // 
				new Sigpac(), // 
				new NzTopoMaps() // 
		};
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
		return DEFAULT;
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
