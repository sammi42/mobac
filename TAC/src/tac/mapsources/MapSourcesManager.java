package tac.mapsources;

import java.util.TreeSet;
import java.util.Vector;

import org.openstreetmap.gui.jmapviewer.interfaces.MapSource;

import tac.mapsources.impl.LocalhostTestSource;
import tac.mapsources.impl.Google.GoogleEarth;
import tac.mapsources.impl.Google.GoogleHybrid;
import tac.mapsources.impl.Google.GoogleMapMaker;
import tac.mapsources.impl.Google.GoogleMaps;
import tac.mapsources.impl.Google.GoogleMapsChina;
import tac.mapsources.impl.Google.GoogleMapsKorea;
import tac.mapsources.impl.Google.GoogleTerrain;
import tac.mapsources.impl.Microsoft.MicrosoftHybrid;
import tac.mapsources.impl.Microsoft.MicrosoftMaps;
import tac.mapsources.impl.Microsoft.MicrosoftMapsChina;
import tac.mapsources.impl.Microsoft.MicrosoftVirtualEarth;
import tac.mapsources.impl.MiscMapSources.MultimapCom;
import tac.mapsources.impl.MiscMapSources.MultimapOSUkCom;
import tac.mapsources.impl.MiscMapSources.OviMaps;
import tac.mapsources.impl.MiscMapSources.YahooMaps;
import tac.mapsources.impl.OsmMapSources.CycleMap;
import tac.mapsources.impl.OsmMapSources.Mapnik;
import tac.mapsources.impl.OsmMapSources.OpenPisteMap;
import tac.mapsources.impl.OsmMapSources.OsmHikingMap;
import tac.mapsources.impl.OsmMapSources.OsmHikingMapWithRelief;
import tac.mapsources.impl.OsmMapSources.OsmPublicTransport;
import tac.mapsources.impl.OsmMapSources.TilesAtHome;
import tac.mapsources.impl.OsmMapSources.Turaterkep;
import tac.mapsources.impl.RegionalMapSources.AustrianMap;
import tac.mapsources.impl.RegionalMapSources.Cykloatlas;
import tac.mapsources.impl.RegionalMapSources.DoCeluPL;
import tac.mapsources.impl.RegionalMapSources.EmapaPl;
import tac.mapsources.impl.RegionalMapSources.EniroComAerial;
import tac.mapsources.impl.RegionalMapSources.EniroComMap;
import tac.mapsources.impl.RegionalMapSources.EniroComNautical;
import tac.mapsources.impl.RegionalMapSources.FreemapSlovakia;
import tac.mapsources.impl.RegionalMapSources.FreemapSlovakiaHiking;
import tac.mapsources.impl.RegionalMapSources.FreemapSlovakiaHikingHillShade;
import tac.mapsources.impl.RegionalMapSources.HubermediaBavaria;
import tac.mapsources.impl.RegionalMapSources.MapplusCh;
import tac.mapsources.impl.RegionalMapSources.NearMap;
import tac.mapsources.impl.RegionalMapSources.OutdooractiveAustria;
import tac.mapsources.impl.RegionalMapSources.OutdooractiveGermany;
import tac.mapsources.impl.RegionalMapSources.OutdooractiveSouthTyrol;
import tac.mapsources.impl.RegionalMapSources.StatkartTopo2;
import tac.mapsources.impl.RegionalMapSources.StatkartToporaster2;
import tac.mapsources.impl.RegionalMapSources.UmpWawPl;
import tac.mapsources.impl.WmsSources.OsmWms;
import tac.mapsources.impl.WmsSources.TerraserverUSA;
import tac.program.model.Settings;

public class MapSourcesManager {

	public static final MapSource DEFAULT = new Mapnik();
	private static MapSource[] MAP_SOURCES;

	private static MapSource LOCALHOST_TEST_MAPSOURCE_STORE_ON = new LocalhostTestSource(
			"Localhost (stored)", true);
	private static MapSource LOCALHOST_TEST_MAPSOURCE_STORE_OFF = new LocalhostTestSource(
			"Localhost (unstored)", false);

	static {
		MapSourcesUpdater.loadMapSourceProperties();
		MAP_SOURCES = new MapSource[] { //
				//
				new GoogleMaps(), new GoogleMapMaker(), new GoogleMapsChina(),
				new GoogleMapsKorea(), new GoogleEarth(), new GoogleHybrid(), new GoogleTerrain(),
				new YahooMaps(), DEFAULT, new TilesAtHome(), new CycleMap(), new OsmHikingMap(),
				new OsmHikingMapWithRelief(), new OsmPublicTransport(), new OpenPisteMap(),
				new MicrosoftMaps(), new MicrosoftMapsChina(), new MicrosoftVirtualEarth(),
				new MicrosoftHybrid(), new OviMaps(), new OutdooractiveGermany(), new OutdooractiveAustria(),
				new OutdooractiveSouthTyrol(), new MultimapCom(), new MultimapOSUkCom(),
				new OsmWms(), new Cykloatlas(), new TerraserverUSA(), new UmpWawPl(), new DoCeluPL(),
				new EmapaPl(), new AustrianMap(), new FreemapSlovakia(),
				new FreemapSlovakiaHiking(), new FreemapSlovakiaHikingHillShade(),
				new Turaterkep(), new NearMap(), new HubermediaBavaria(), new StatkartTopo2(),
				new StatkartToporaster2(), new EniroComMap(), new EniroComAerial(),
				new EniroComNautical(), new MapplusCh() };
	}

	public static Vector<MapSource> getAllMapSources() {
		Vector<MapSource> mapSources = new Vector<MapSource>();
		if (Settings.getInstance().isDevModeEnabled()) {
			mapSources.add(LOCALHOST_TEST_MAPSOURCE_STORE_OFF);
			mapSources.add(LOCALHOST_TEST_MAPSOURCE_STORE_ON);
		}
		for (MapSource ms : MAP_SOURCES)
			mapSources.add(ms);
		for (MapSource ms : Settings.getInstance().customMapSources)
			mapSources.add(ms);
		return mapSources;
	}

	public static Vector<MapSource> getEnabledMapSources() {
		Vector<MapSource> mapSources = new Vector<MapSource>();
		if (Settings.getInstance().isDevModeEnabled()) {
			mapSources.add(LOCALHOST_TEST_MAPSOURCE_STORE_OFF);
			mapSources.add(LOCALHOST_TEST_MAPSOURCE_STORE_ON);
		}
		TreeSet<String> disabledMapSources = new TreeSet<String>(Settings.getInstance()
				.getDisabledMapSources());
		for (MapSource ms : MAP_SOURCES) {
			if (!disabledMapSources.contains(ms.getName()))
				mapSources.add(ms);
		}
		for (MapSource ms : Settings.getInstance().customMapSources)
			mapSources.add(ms);
		return mapSources;
	}

	public static String getDefaultMapSourceName() {
		return DEFAULT.getName();
	}

	public static MapSource getSourceByName(String name) {
		for (MapSource ms : MAP_SOURCES) {
			if (ms.getName().equals(name))
				return ms;
		}
		for (MapSource ms : Settings.getInstance().customMapSources) {
			if (ms.getName().equals(name))
				return ms;
		}
		if (Settings.getInstance().isDevModeEnabled()) {
			if (LOCALHOST_TEST_MAPSOURCE_STORE_ON.getName().equals(name))
				return LOCALHOST_TEST_MAPSOURCE_STORE_ON;
			if (LOCALHOST_TEST_MAPSOURCE_STORE_OFF.getName().equals(name))
				return LOCALHOST_TEST_MAPSOURCE_STORE_OFF;
		}
		return null;
	}

}
