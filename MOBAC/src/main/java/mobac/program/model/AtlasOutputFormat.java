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
package mobac.program.model;

import javax.xml.bind.annotation.XmlRootElement;

import mobac.program.atlascreators.AFTrack;
import mobac.program.atlascreators.AlpineQuestMap;
import mobac.program.atlascreators.AndNav;
import mobac.program.atlascreators.AtlasCreator;
import mobac.program.atlascreators.BackCountryNavigator;
import mobac.program.atlascreators.CacheBox;
import mobac.program.atlascreators.CacheWolf;
import mobac.program.atlascreators.GarminCustom;
import mobac.program.atlascreators.Glopus;
import mobac.program.atlascreators.GlopusMapFile;
import mobac.program.atlascreators.GoogleEarthOverlay;
import mobac.program.atlascreators.GpsSportsTracker;
import mobac.program.atlascreators.MagellanRmp;
import mobac.program.atlascreators.Maplorer;
import mobac.program.atlascreators.Maverick;
import mobac.program.atlascreators.MobileTrailExplorer;
import mobac.program.atlascreators.MobileTrailExplorerCache;
import mobac.program.atlascreators.NaviComputer;
import mobac.program.atlascreators.OSMAND;
import mobac.program.atlascreators.OSMTracker;
import mobac.program.atlascreators.OruxMaps;
import mobac.program.atlascreators.OruxMapsSqlite;
import mobac.program.atlascreators.Osmdroid;
import mobac.program.atlascreators.Ozi;
import mobac.program.atlascreators.PathAway;
import mobac.program.atlascreators.RMapsSQLite;
import mobac.program.atlascreators.RunGPSAtlas;
import mobac.program.atlascreators.SportsTracker;
import mobac.program.atlascreators.TTQV;
import mobac.program.atlascreators.TrekBuddyCustom;
import mobac.program.atlascreators.Ublox;

@XmlRootElement
public enum AtlasOutputFormat {

	AFTrack("AFTrack (OSZ)", AFTrack.class), //
	AlpineQuestMap("AlpineQuestMap (AQM)", AlpineQuestMap.class), //
	AndNav("AndNav atlas format", AndNav.class), //
	BackCountryNavigator("BackCountry Navigator", BackCountryNavigator.class), //
	BigPlanet("Big Planet Tracks SQLite", RMapsSQLite.class), //
	CacheBox("CacheBox (PACK)", CacheBox.class), //
	CacheWolf("CacheWolf (WFL)", CacheWolf.class), //
	GarminCustom("Garmin Custom Map (KMZ)", GarminCustom.class), //
	Glopus("Glopus (PNG & KAL)", Glopus.class), //
	GoogleEarthRasterOverlay("Google Earth Overlay (KMZ)", GoogleEarthOverlay.class), //
	Gmf("Glopus Map File (GMF)", GlopusMapFile.class), // 
	GpsSportsTracker("GPS Sports Tracker", GpsSportsTracker.class), //
	Rmp("Magellan (RMP)", MagellanRmp.class), //
	Maplorer("Maplorer atlas format", Maplorer.class), //
	Maverick("Maverick atlas format", Maverick.class), //
	MTE("Mobile Trail Explorer", MobileTrailExplorer.class), //
	MTECache("Mobile Trail Explorer Cache", MobileTrailExplorerCache.class), //
	NaviComputer("NaviComputer (NMAP)", NaviComputer.class), //
	OruxMaps("OruxMaps", OruxMaps.class), //
	OruxMapsSqlite("OruxMaps Sqlite", OruxMapsSqlite.class), //
	OSMAND("OSMAND tile storage", OSMAND.class), //
	Osmdroid("Osmdroid ZIP", Osmdroid.class), //
	OSMTracker("OSMTracker tile storage", OSMTracker.class), //
	OziPng("OziExplorer (PNG & MAP)", Ozi.class), //
	PathAway("PathAway tile cache", PathAway.class), //
	RMaps("RMaps SQLite", RMapsSQLite.class), //
	RunGPS("Run.GPS Atlas", RunGPSAtlas.class), //
	NST("Sports Tracker", SportsTracker.class), //
	Ttqv("Touratech QV", TTQV.class), //
	TaredAtlas("TrekBuddy tared atlas", TrekBuddyCustom.class), // 
	UntaredAtlas("TrekBuddy untared atlas", TrekBuddyCustom.class), // 
	Ublox("Ublox", Ublox.class), // 
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
		} catch (Throwable t) {
			throw new RuntimeException(t);
		}
	}
}
