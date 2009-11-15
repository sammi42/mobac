package tac.program.model;

import javax.xml.bind.annotation.XmlRootElement;

import tac.program.mapcreators.MapCreator;
import tac.program.mapcreators.MapCreatorAndNav;
import tac.program.mapcreators.MapCreatorBigPlanet;
import tac.program.mapcreators.MapCreatorGarminCustom;
import tac.program.mapcreators.MapCreatorGlopus;
import tac.program.mapcreators.MapCreatorMTE;
import tac.program.mapcreators.MapCreatorMaverick;
import tac.program.mapcreators.MapCreatorOSMTracker;
import tac.program.mapcreators.MapCreatorOzi;
import tac.program.mapcreators.MapCreatorTrekBuddyCustom;

@XmlRootElement
public enum AtlasOutputFormat {

	TaredAtlas("TrekBuddy tared atlas", MapCreatorTrekBuddyCustom.class), // 
	UntaredAtlas("TrekBuddy untared atlas", MapCreatorTrekBuddyCustom.class), //
	MTE("Mobile Trail Explorer", MapCreatorMTE.class), //
	AndNav("AndNav atlas format", MapCreatorAndNav.class), //
	Maverick("Maverick atlas format", MapCreatorMaverick.class), //
	OSMTracker("OSMTracker tile storage", MapCreatorOSMTracker.class), //
	BigPlanet("BigPlanet SQLite", MapCreatorBigPlanet.class), //
	OziPng("OziExplorer (PNG & MAP)", MapCreatorOzi.class), //
	Glopus("Glopus (PNG & KAL)", MapCreatorGlopus.class), // 
	GarminCustom("Garmin Custom Map (KMZ)", MapCreatorGarminCustom.class);

	private final String displayName;
	private Class<? extends MapCreator> mapCreatorClass;

	private AtlasOutputFormat(String displayName, Class<? extends MapCreator> mapCreatorClass) {
		this.displayName = displayName;
		this.mapCreatorClass = mapCreatorClass;
	}

	public String toString() {
		return displayName;
	}

	public Class<? extends MapCreator> getMapCreatorClass() {
		return mapCreatorClass;
	}

	public MapCreator createMapCreatorInstance() throws InstantiationException,
			IllegalAccessException {
		if (mapCreatorClass == null)
			return null;
		return mapCreatorClass.newInstance();
	}
}
