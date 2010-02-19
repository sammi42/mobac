package mobac.tools;

import java.util.HashMap;

import mobac.mapsources.impl.Google.GoogleMapMaker;
import mobac.mapsources.impl.Google.GoogleMapsChina;
import mobac.mapsources.impl.Google.GoogleMapsKorea;
import mobac.mapsources.impl.Microsoft.MicrosoftMapsChina;
import mobac.mapsources.impl.Microsoft.MicrosoftVirtualEarth;
import mobac.mapsources.impl.MiscMapSources.MultimapCom;
import mobac.mapsources.impl.MiscMapSources.MultimapOSUkCom;
import mobac.mapsources.impl.OsmMapSources.OpenPisteMap;
import mobac.mapsources.impl.OsmMapSources.Turaterkep;
import mobac.mapsources.impl.RegionalMapSources.AustrianMap;
import mobac.mapsources.impl.RegionalMapSources.Bergfex;
import mobac.mapsources.impl.RegionalMapSources.Cykloatlas;
import mobac.mapsources.impl.RegionalMapSources.DoCeluPL;
import mobac.mapsources.impl.RegionalMapSources.FreemapSlovakia;
import mobac.mapsources.impl.RegionalMapSources.FreemapSlovakiaHiking;
import mobac.mapsources.impl.RegionalMapSources.FreemapSlovakiaHikingHillShade;
import mobac.mapsources.impl.RegionalMapSources.HubermediaBavaria;
import mobac.mapsources.impl.RegionalMapSources.MapplusCh;
import mobac.mapsources.impl.RegionalMapSources.NearMap;
import mobac.mapsources.impl.RegionalMapSources.StatkartTopo2;
import mobac.program.model.EastNorthCoordinate;

import org.openstreetmap.gui.jmapviewer.interfaces.MapSource;

public class Cities {

	private static final HashMap<Class<? extends MapSource>, EastNorthCoordinate> TEST_COORDINATES;

	public static final EastNorthCoordinate NEY_YORK = new EastNorthCoordinate(40.75, -73.88);
	public static final EastNorthCoordinate BERLIN = new EastNorthCoordinate(52.50, 13.39);
	public static final EastNorthCoordinate MOSCOW = new EastNorthCoordinate(55.75, 37.63);
	public static final EastNorthCoordinate PRAHA = new EastNorthCoordinate(50.00, 14.41);
	public static final EastNorthCoordinate BANGALORE = new EastNorthCoordinate(12.95, 77.616667);
	public static final EastNorthCoordinate SHANGHAI = new EastNorthCoordinate(31.2333, 121.4666);
	public static final EastNorthCoordinate WARSZAWA = new EastNorthCoordinate(52.2166, 21.0333);
	public static final EastNorthCoordinate VIENNA = new EastNorthCoordinate(48.20, 16.37);
	public static final EastNorthCoordinate BRATISLAVA = new EastNorthCoordinate(48.154, 17.14);
	public static final EastNorthCoordinate SEOUL = new EastNorthCoordinate(37.55, 126.98);
	public static final EastNorthCoordinate SYDNEY = new EastNorthCoordinate(-33.8, 151.3);
	public static final EastNorthCoordinate BUDAPEST = new EastNorthCoordinate(47.47, 19.05);
	public static final EastNorthCoordinate MUNICH = new EastNorthCoordinate(48.13, 11.58);
	public static final EastNorthCoordinate OSLO = new EastNorthCoordinate(59.91, 10.75);
	public static final EastNorthCoordinate BERN = new EastNorthCoordinate(46.95, 7.45);
	public static final EastNorthCoordinate LONDON = new EastNorthCoordinate(51.51, -0.11);
	public static final EastNorthCoordinate INNSBRUCK = new EastNorthCoordinate(47.26, 11.39);

	static {
		TEST_COORDINATES = new HashMap<Class<? extends MapSource>, EastNorthCoordinate>();
		TEST_COORDINATES.put(GoogleMapMaker.class, Cities.BANGALORE);
		TEST_COORDINATES.put(Cykloatlas.class, Cities.PRAHA);
		TEST_COORDINATES.put(GoogleMapsChina.class, Cities.SHANGHAI);
		TEST_COORDINATES.put(GoogleMapsKorea.class, Cities.SEOUL);
		TEST_COORDINATES.put(MicrosoftMapsChina.class, Cities.SHANGHAI);
		TEST_COORDINATES.put(MicrosoftVirtualEarth.class, Cities.NEY_YORK);
		TEST_COORDINATES.put(MultimapCom.class, Cities.LONDON);
		TEST_COORDINATES.put(MultimapOSUkCom.class, Cities.LONDON);
		TEST_COORDINATES.put(DoCeluPL.class, Cities.WARSZAWA);
		TEST_COORDINATES.put(AustrianMap.class, Cities.VIENNA);
		TEST_COORDINATES.put(FreemapSlovakia.class, Cities.BRATISLAVA);
		TEST_COORDINATES.put(FreemapSlovakiaHiking.class, Cities.BRATISLAVA);
		TEST_COORDINATES.put(FreemapSlovakiaHikingHillShade.class, Cities.BRATISLAVA);
		TEST_COORDINATES.put(NearMap.class, Cities.SYDNEY);
		TEST_COORDINATES.put(HubermediaBavaria.class, Cities.MUNICH);
		TEST_COORDINATES.put(OpenPisteMap.class, Cities.MUNICH);
		TEST_COORDINATES.put(StatkartTopo2.class, Cities.OSLO);
		TEST_COORDINATES.put(MapplusCh.class, Cities.BERN);
		TEST_COORDINATES.put(Turaterkep.class, Cities.BUDAPEST);
		TEST_COORDINATES.put(Bergfex.class, Cities.INNSBRUCK);
	}

	public static EastNorthCoordinate getTestCoordinate(MapSource mapSource,
			EastNorthCoordinate defaultCoordinate) {
		return getTestCoordinate(mapSource.getClass(), defaultCoordinate);
	}

	public static EastNorthCoordinate getTestCoordinate(Class<? extends MapSource> mapSourceClass,
			EastNorthCoordinate defaultCoordinate) {
		EastNorthCoordinate coord = TEST_COORDINATES.get(mapSourceClass);
		if (coord != null)
			return coord;
		else
			return defaultCoordinate;
	}

}
