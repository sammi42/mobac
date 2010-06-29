package mobac.program.model;

import javax.xml.bind.annotation.XmlRootElement;

import mobac.program.atlascreators.AFTrack;
import mobac.program.atlascreators.AlpineQuestMap;
import mobac.program.atlascreators.AndNav;
import mobac.program.atlascreators.AtlasCreator;
import mobac.program.atlascreators.CacheBox;
import mobac.program.atlascreators.CacheWolf;
import mobac.program.atlascreators.GarminCustom;
import mobac.program.atlascreators.Glopus;
import mobac.program.atlascreators.GlopusMapFile;
import mobac.program.atlascreators.GoogleEarthOverlay;
import mobac.program.atlascreators.GpsSportsTracker;
import mobac.program.atlascreators.MagellanRmp;
import mobac.program.atlascreators.Maverick;
import mobac.program.atlascreators.MobileTrailExplorer;
import mobac.program.atlascreators.MobileTrailExplorerCache;
import mobac.program.atlascreators.OSMTracker;
import mobac.program.atlascreators.OruxMaps;
import mobac.program.atlascreators.Ozi;
import mobac.program.atlascreators.PathAway;
import mobac.program.atlascreators.RMapsSQLite;
import mobac.program.atlascreators.TTQV;
import mobac.program.atlascreators.TrekBuddyCustom;
import mobac.program.atlascreators.Ublox;

@XmlRootElement
public enum AtlasOutputFormat {

	AFTrack("AFTrack (OSZ)", AFTrack.class), //
	AlpineQuestMap("AlpineQuestMap (AQM)", AlpineQuestMap.class), //
	AndNav("AndNav atlas format", AndNav.class), //
	BigPlanet("Big Planet Tracks SQLite", RMapsSQLite.class), //
	CacheBox("CacheBox (PACK)", CacheBox.class), //
	CacheWolf("CacheWolf (WFL)", CacheWolf.class), //
	GarminCustom("Garmin Custom Map (KMZ)", GarminCustom.class), //
	Glopus("Glopus (PNG & KAL)", Glopus.class), //
	GoogleEarthRasterOverlay("Google Earth Overlay (KMZ)", GoogleEarthOverlay.class), //
	Gmf("Glopus Map File (GMF)", GlopusMapFile.class), // 
	GpsSportsTracker("GPS Sports Tracker", GpsSportsTracker.class), //
	Rmp("Magellan (RMP)", MagellanRmp.class), //
	Maverick("Maverick atlas format", Maverick.class), //
	MTE("Mobile Trail Explorer", MobileTrailExplorer.class), //
	MTECache("Mobile Trail Explorer Cache", MobileTrailExplorerCache.class), //
	OruxMaps("OruxMaps", OruxMaps.class), //
	OSMTracker("OSMTracker tile storage", OSMTracker.class), //
	OziPng("OziExplorer (PNG & MAP)", Ozi.class), //
	PathAway("PathAway tile cache", PathAway.class), //
	RMaps("RMaps SQLite", RMapsSQLite.class), //
	Ttqv("Touratech QV", TTQV.class), //
	Ublox("Ublox", Ublox.class), // 
	TaredAtlas("TrekBuddy tared atlas", TrekBuddyCustom.class), // 
	UntaredAtlas("TrekBuddy untared atlas", TrekBuddyCustom.class), // 
	// XGPS("xGPS", SQLitexGPS.class), //
	;

	private final String displayName;
	private Class<? extends AtlasCreator> atlasCreatorClass;

	private AtlasOutputFormat(String displayName, Class<? extends AtlasCreator> mapCreatorClass) {
		this.displayName = displayName;
		this.atlasCreatorClass = mapCreatorClass;
	}

	public String toString() {
		return displayName;
	}

	public Class<? extends AtlasCreator> getMapCreatorClass() {
		return atlasCreatorClass;
	}

	public AtlasCreator createAtlasCreatorInstance() {
		if (atlasCreatorClass == null)
			return null;
		try {
			return atlasCreatorClass.newInstance();
		} catch (InstantiationException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}
}
