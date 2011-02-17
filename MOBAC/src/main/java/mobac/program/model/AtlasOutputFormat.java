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

import mobac.program.annotations.AtlasCreatorName;
import mobac.program.atlascreators.AFTrack;
import mobac.program.atlascreators.AlpineQuestMap;
import mobac.program.atlascreators.AndNav;
import mobac.program.atlascreators.AtlasCreator;
import mobac.program.atlascreators.BackCountryNavigator;
import mobac.program.atlascreators.BigPlanetTracks;
import mobac.program.atlascreators.CacheBox;
import mobac.program.atlascreators.CacheWolf;
import mobac.program.atlascreators.Galileo;
import mobac.program.atlascreators.GarminCustom;
import mobac.program.atlascreators.Glopus;
import mobac.program.atlascreators.GlopusMapFile;
import mobac.program.atlascreators.GoogleEarthOverlay;
import mobac.program.atlascreators.GpsSportsTracker;
import mobac.program.atlascreators.MGMaps;
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
import mobac.program.atlascreators.OsmdroidSQLite;
import mobac.program.atlascreators.Ozi;
import mobac.program.atlascreators.PNGWorldfile;
import mobac.program.atlascreators.PathAway;
import mobac.program.atlascreators.RMapsSQLite;
import mobac.program.atlascreators.RunGPSAtlas;
import mobac.program.atlascreators.SportsTracker;
import mobac.program.atlascreators.TTQV;
import mobac.program.atlascreators.TrekBuddy;
import mobac.program.atlascreators.TrekBuddyTared;
import mobac.program.atlascreators.Ublox;

@XmlRootElement
public enum AtlasOutputFormat {

	AFTrack(AFTrack.class), //
	AlpineQuestMap(AlpineQuestMap.class), //
	AndNav(AndNav.class), //
	BackCountryNavigator(BackCountryNavigator.class), //
	BigPlanet(BigPlanetTracks.class), //
	CacheBox(CacheBox.class), //
	CacheWolf(CacheWolf.class), //
	Galileo(Galileo.class), //
	GarminCustom(GarminCustom.class), //
	Glopus(Glopus.class), //
	Gmf(GlopusMapFile.class), //
	GoogleEarthRasterOverlay(GoogleEarthOverlay.class), //
	GpsSportsTracker(GpsSportsTracker.class), //
	Rmp(MagellanRmp.class), //
	Maplorer(Maplorer.class), //
	Maverick(Maverick.class), //
	MGM(MGMaps.class), //
	MTE(MobileTrailExplorer.class), //
	MTECache(MobileTrailExplorerCache.class), //
	NaviComputer(NaviComputer.class), //
	OruxMaps(OruxMaps.class), //
	OruxMapsSqlite(OruxMapsSqlite.class), //
	OSMAND(OSMAND.class), //
	Osmdroid(Osmdroid.class), //
	OsmdroidSQLite(OsmdroidSQLite.class), //
	OSMTracker(OSMTracker.class), //
	OziPng(Ozi.class), //
	PathAway(PathAway.class), //
	PNGWorldfile(PNGWorldfile.class), //
	RMaps(RMapsSQLite.class), //
	RunGPS(RunGPSAtlas.class), //
	NST(SportsTracker.class), //
	Ttqv(TTQV.class), //
	TaredAtlas(TrekBuddyTared.class), //
	UntaredAtlas(TrekBuddy.class), //
	// TwoNavRMAP( TwoNavRmap.class), //
	Ublox(Ublox.class) //
	;

	private Class<? extends AtlasCreator> atlasCreatorClass;

	private AtlasOutputFormat(Class<? extends AtlasCreator> mapCreatorClass) {
		this.atlasCreatorClass = mapCreatorClass;
	}

	public String toString() {
		AtlasCreatorName acName = atlasCreatorClass.getAnnotation(AtlasCreatorName.class);
		if (acName == null)
			throw new RuntimeException("AtlasCreator " + atlasCreatorClass.getName() + " has no name");
		return acName.value();
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
