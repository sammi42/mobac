package mobac.program.model;

import javax.xml.bind.annotation.XmlRootElement;

import mobac.program.atlascreators.AndNav;
import mobac.program.atlascreators.AtlasCreator;
import mobac.program.atlascreators.BigPlanetSql;
import mobac.program.atlascreators.CacheBox;
import mobac.program.atlascreators.CacheWolf;
import mobac.program.atlascreators.GarminCustom;
import mobac.program.atlascreators.Glopus;
import mobac.program.atlascreators.MagellanRmp;
import mobac.program.atlascreators.Maverick;
import mobac.program.atlascreators.MobileTrailExplorer;
import mobac.program.atlascreators.MobileTrailExplorerCache;
import mobac.program.atlascreators.OSMTracker;
import mobac.program.atlascreators.OruxMaps;
import mobac.program.atlascreators.Ozi;
import mobac.program.atlascreators.TTQV;
import mobac.program.atlascreators.TrekBuddyCustom;


@XmlRootElement
public enum AtlasOutputFormat {

	TaredAtlas("TrekBuddy tared atlas", TrekBuddyCustom.class), // 
	UntaredAtlas("TrekBuddy untared atlas", TrekBuddyCustom.class), //
	MTE("Mobile Trail Explorer", MobileTrailExplorer.class), //
	MTECache("Mobile Trail Explorer Cache", MobileTrailExplorerCache.class), //
	AndNav("AndNav atlas format", AndNav.class), //
	Maverick("Maverick atlas format", Maverick.class), //
	OSMTracker("OSMTracker tile storage", OSMTracker.class), //
	BigPlanet("BigPlanet SQLite", BigPlanetSql.class), //
	OziPng("OziExplorer (PNG & MAP)", Ozi.class), //
	Glopus("Glopus (PNG & KAL)", Glopus.class), // 
	GarminCustom("Garmin Custom Map (KMZ)", GarminCustom.class), //
	Rmp("Magellan (RMP)", MagellanRmp.class), //
	Ttqv("Touratech QV", TTQV.class), //
	CacheWolf("CacheWolf (WFL)", CacheWolf.class), //
	CacheBox("CacheBox (PACK)", CacheBox.class),
	OruxMaps("Android OruxMaps", OruxMaps.class);

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
