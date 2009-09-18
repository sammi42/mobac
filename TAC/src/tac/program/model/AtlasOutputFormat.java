package tac.program.model;

import tac.program.mapcreators.MapCreator;

public enum AtlasOutputFormat {

	TaredAtlas("TrekBuddy tared atlas"), // 
	UntaredAtlas("TrekBuddy untared atlas"), //
	MTE("Mobile Trail Explorer"), //
	AndNav("AndNav atlas format"), //
	OSMTracker("OSMTracker tile storage"), 
	BigPlanet("BigPlanet SQLite"), //
	OziPng("OziExplorer (PNG & MAP)");

	private final String displayName;
	private Class<? extends MapCreator> mapCreatorClass;

	private AtlasOutputFormat(String displayName) {
		this.displayName = displayName;
		//this.mapCreatorClass = mapCreatorClass;
	}

	public String toString() {
		return displayName;
	}

	public Class<? extends MapCreator> getMapCreatorClass() {
		return mapCreatorClass;
	}
	
}
