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
import mobac.mapsources.impl.MultimapCom;
import mobac.mapsources.impl.SimpleMapSource;
import mobac.mapsources.impl.WmsSources.TerraserverUSA;
import mobac.mapsources.mappacks.bing.MicrosoftHybrid;
import mobac.mapsources.mappacks.bing.MicrosoftMaps;
import mobac.mapsources.mappacks.bing.MicrosoftMapsChina;
import mobac.mapsources.mappacks.bing.MicrosoftMapsHillShade;
import mobac.mapsources.mappacks.bing.MicrosoftVirtualEarth;
import mobac.mapsources.mappacks.google.GoogleEarth;
import mobac.mapsources.mappacks.google.GoogleHybrid;
import mobac.mapsources.mappacks.google.GoogleMapMaker;
import mobac.mapsources.mappacks.google.GoogleMaps;
import mobac.mapsources.mappacks.google.GoogleMapsChina;
import mobac.mapsources.mappacks.google.GoogleMapsKorea;
import mobac.mapsources.mappacks.google.GoogleTerrain;
import mobac.mapsources.mappacks.misc_worldwide.OviMaps;
import mobac.mapsources.mappacks.misc_worldwide.Topomapper;
import mobac.mapsources.mappacks.misc_worldwide.YahooMaps;
import mobac.mapsources.mappacks.openstreetmap.CycleMap;
import mobac.mapsources.mappacks.openstreetmap.Mapnik;
import mobac.mapsources.mappacks.openstreetmap.TilesAtHome;
import mobac.mapsources.mappacks.openstreetmap.OsmMapSources.Hikebikemap;
import mobac.mapsources.mappacks.openstreetmap.OsmMapSources.OpenPisteMap;
import mobac.mapsources.mappacks.openstreetmap.OsmMapSources.OpenSeaMap;
import mobac.mapsources.mappacks.openstreetmap.OsmMapSources.OsmHikingMap;
import mobac.mapsources.mappacks.openstreetmap.OsmMapSources.OsmHikingMapWithBase;
import mobac.mapsources.mappacks.openstreetmap.OsmMapSources.OsmHikingMapWithRelief;
import mobac.mapsources.mappacks.openstreetmap.OsmMapSources.OsmHikingMapWithReliefBase;
import mobac.mapsources.mappacks.openstreetmap.OsmMapSources.OsmPublicTransport;
import mobac.mapsources.mappacks.openstreetmap.OsmMapSources.Turaterkep;
import mobac.mapsources.mappacks.region_america_north.AeroChartsIFR;
import mobac.mapsources.mappacks.region_america_north.AeroChartsIFRH;
import mobac.mapsources.mappacks.region_america_north.AeroChartsVFR;
import mobac.mapsources.mappacks.region_america_north.MyTopo;
import mobac.mapsources.mappacks.region_asia.YahooMapsJapan;
import mobac.mapsources.mappacks.region_asia.YahooMapsTaiwan;
import mobac.mapsources.mappacks.region_europe_dach.Bergfex;
import mobac.mapsources.mappacks.region_europe_dach.HubermediaBavaria;
import mobac.mapsources.mappacks.region_europe_dach.MapplusCh;
import mobac.mapsources.mappacks.region_europe_dach.OutdooractiveAustria;
import mobac.mapsources.mappacks.region_europe_dach.OutdooractiveGermany;
import mobac.mapsources.mappacks.region_europe_dach.OutdooractiveSouthTyrol;
import mobac.mapsources.mappacks.region_europe_east.Cykloatlas;
import mobac.mapsources.mappacks.region_europe_east.CykloatlasWithRelief;
import mobac.mapsources.mappacks.region_europe_east.DoCeluPL;
import mobac.mapsources.mappacks.region_europe_east.EmapiPl;
import mobac.mapsources.mappacks.region_europe_east.FreemapSlovakia;
import mobac.mapsources.mappacks.region_europe_east.FreemapSlovakiaCycling;
import mobac.mapsources.mappacks.region_europe_east.FreemapSlovakiaHiking;
import mobac.mapsources.mappacks.region_europe_east.Navitel;
import mobac.mapsources.mappacks.region_europe_east.UmpWawPl;
import mobac.mapsources.mappacks.region_europe_east.YandexMap;
import mobac.mapsources.mappacks.region_europe_east.YandexSat;
import mobac.mapsources.mappacks.region_europe_north.EniroComAerial;
import mobac.mapsources.mappacks.region_europe_north.EniroComMap;
import mobac.mapsources.mappacks.region_europe_north.EniroComNautical;
import mobac.mapsources.mappacks.region_europe_north.StatkartSjoHovedkart2;
import mobac.mapsources.mappacks.region_europe_north.StatkartTopo2;
import mobac.mapsources.mappacks.region_europe_north.StatkartToporaster2;
import mobac.mapsources.mappacks.region_europe_south.Sigpac;
import mobac.mapsources.mappacks.region_europe_west.MicrosoftOrdnanceSurveyExplorer;
import mobac.mapsources.mappacks.region_europe_west.MultimapOSUkCom;
import mobac.mapsources.mappacks.region_oceania.NearMap;
import mobac.mapsources.mappacks.region_oceania.NzTopoMaps;
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
				new SimpleMapSource(),
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
				new NzTopoMaps(), //
				new Topomapper() //
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
