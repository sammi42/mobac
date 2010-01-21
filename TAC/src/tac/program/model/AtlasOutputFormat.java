package tac.program.model;

import javax.xml.bind.annotation.XmlRootElement;

import tac.program.atlascreators.AndNav;
import tac.program.atlascreators.AtlasCreator;
import tac.program.atlascreators.BigPlanetSql;
import tac.program.atlascreators.CacheBox;
import tac.program.atlascreators.CacheWolf;
import tac.program.atlascreators.GarminCustom;
import tac.program.atlascreators.Glopus;
import tac.program.atlascreators.MagellanRmp;
import tac.program.atlascreators.Maverick;
import tac.program.atlascreators.MobileTrailExplorer;
import tac.program.atlascreators.MobileTrailExplorerCache;
import tac.program.atlascreators.OSMTracker;
import tac.program.atlascreators.Ozi;
import tac.program.atlascreators.TTQV;
import tac.program.atlascreators.TrekBuddyCustom;

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
	CacheBox("CacheBox (PACK)", CacheBox.class);

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
