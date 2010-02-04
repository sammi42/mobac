package mobac.mapsources;

import java.util.TreeSet;
import java.util.Vector;

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
import mobac.mapsources.impl.Microsoft.MicrosoftVirtualEarth;
import mobac.mapsources.impl.MiscMapSources.MultimapCom;
import mobac.mapsources.impl.MiscMapSources.MultimapOSUkCom;
import mobac.mapsources.impl.MiscMapSources.OviMaps;
import mobac.mapsources.impl.MiscMapSources.YahooMaps;
import mobac.mapsources.impl.OsmMapSources.CycleMap;
import mobac.mapsources.impl.OsmMapSources.Mapnik;
import mobac.mapsources.impl.OsmMapSources.OpenPisteMap;
import mobac.mapsources.impl.OsmMapSources.OsmHikingMap;
import mobac.mapsources.impl.OsmMapSources.OsmHikingMapWithBase;
import mobac.mapsources.impl.OsmMapSources.OsmHikingMapWithRelief;
import mobac.mapsources.impl.OsmMapSources.OsmPublicTransport;
import mobac.mapsources.impl.OsmMapSources.TilesAtHome;
import mobac.mapsources.impl.OsmMapSources.Turaterkep;
import mobac.mapsources.impl.RegionalMapSources.AustrianMap;
import mobac.mapsources.impl.RegionalMapSources.Bergfex;
import mobac.mapsources.impl.RegionalMapSources.Cykloatlas;
import mobac.mapsources.impl.RegionalMapSources.DoCeluPL;
import mobac.mapsources.impl.RegionalMapSources.EmapaPl;
import mobac.mapsources.impl.RegionalMapSources.EniroComAerial;
import mobac.mapsources.impl.RegionalMapSources.EniroComMap;
import mobac.mapsources.impl.RegionalMapSources.EniroComNautical;
import mobac.mapsources.impl.RegionalMapSources.FreemapSlovakia;
import mobac.mapsources.impl.RegionalMapSources.FreemapSlovakiaHiking;
import mobac.mapsources.impl.RegionalMapSources.FreemapSlovakiaHikingHillShade;
import mobac.mapsources.impl.RegionalMapSources.HubermediaBavaria;
import mobac.mapsources.impl.RegionalMapSources.MapplusCh;
import mobac.mapsources.impl.RegionalMapSources.NearMap;
import mobac.mapsources.impl.RegionalMapSources.OutdooractiveAustria;
import mobac.mapsources.impl.RegionalMapSources.OutdooractiveGermany;
import mobac.mapsources.impl.RegionalMapSources.OutdooractiveSouthTyrol;
import mobac.mapsources.impl.RegionalMapSources.StatkartTopo2;
import mobac.mapsources.impl.RegionalMapSources.StatkartToporaster2;
import mobac.mapsources.impl.RegionalMapSources.UmpWawPl;
import mobac.mapsources.impl.WmsSources.TerraserverUSA;
import mobac.program.model.Settings;

import org.openstreetmap.gui.jmapviewer.interfaces.MapSource;

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
				new OsmHikingMapWithBase(), new OsmHikingMapWithRelief(), new OsmPublicTransport(),
				new OpenPisteMap(), new MicrosoftMaps(), new MicrosoftMapsChina(),
				new MicrosoftVirtualEarth(), new MicrosoftHybrid(), new OviMaps(),
				new OutdooractiveGermany(), new OutdooractiveAustria(),
				new OutdooractiveSouthTyrol(), new MultimapCom(), new MultimapOSUkCom(),
				new Cykloatlas(), new TerraserverUSA(), new UmpWawPl(), new DoCeluPL(),
				new EmapaPl(), new AustrianMap(), new Bergfex(), new FreemapSlovakia(),
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
