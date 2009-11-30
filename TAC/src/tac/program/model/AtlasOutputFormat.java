package tac.program.model;

import javax.xml.bind.annotation.XmlRootElement;

import tac.program.mapcreators.AtlasCreator;
import tac.program.mapcreators.MapCreatorAndNav;
import tac.program.mapcreators.MapCreatorBigPlanet;
import tac.program.mapcreators.MapCreatorGarminCustom;
import tac.program.mapcreators.MapCreatorGlopus;
import tac.program.mapcreators.MapCreatorMTE;
import tac.program.mapcreators.MapCreatorMTECache;
import tac.program.mapcreators.MapCreatorMaverick;
import tac.program.mapcreators.MapCreatorOSMTracker;
import tac.program.mapcreators.MapCreatorOzi;
import tac.program.mapcreators.MapCreatorTrekBuddyCustom;

@XmlRootElement
public enum AtlasOutputFormat {

	TaredAtlas("TrekBuddy tared atlas", MapCreatorTrekBuddyCustom.class), // 
	UntaredAtlas("TrekBuddy untared atlas", MapCreatorTrekBuddyCustom.class), //
	MTE("Mobile Trail Explorer", MapCreatorMTE.class), //
	MTECache("Mobile Trail Explorer Cache", MapCreatorMTECache.class), //
	AndNav("AndNav atlas format", MapCreatorAndNav.class), //
	Maverick("Maverick atlas format", MapCreatorMaverick.class), //
	OSMTracker("OSMTracker tile storage", MapCreatorOSMTracker.class), //
	BigPlanet("BigPlanet SQLite", MapCreatorBigPlanet.class), //
	OziPng("OziExplorer (PNG & MAP)", MapCreatorOzi.class), //
	Glopus("Glopus (PNG & KAL)", MapCreatorGlopus.class), // 
	GarminCustom("Garmin Custom Map (KMZ)", MapCreatorGarminCustom.class);

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
