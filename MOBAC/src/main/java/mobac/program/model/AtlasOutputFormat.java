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

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Vector;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

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
import mobac.program.atlascreators.NFComPass;
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
import mobac.program.jaxb.AtlasOutputFormatAdapter;

@XmlRootElement
@XmlJavaTypeAdapter(AtlasOutputFormatAdapter.class)
public class AtlasOutputFormat implements Comparable<AtlasOutputFormat> {

	public static List<AtlasOutputFormat> FORMATS;

	static {
		FORMATS = new ArrayList<AtlasOutputFormat>(40);
		FORMATS.add(new AtlasOutputFormat(AFTrack.class));
		FORMATS.add(new AtlasOutputFormat(AlpineQuestMap.class));
		FORMATS.add(new AtlasOutputFormat(AndNav.class));
		FORMATS.add(new AtlasOutputFormat(BackCountryNavigator.class));
		FORMATS.add(new AtlasOutputFormat(BigPlanetTracks.class));
		FORMATS.add(new AtlasOutputFormat(CacheBox.class));
		FORMATS.add(new AtlasOutputFormat(CacheWolf.class));
		FORMATS.add(new AtlasOutputFormat(Galileo.class));
		FORMATS.add(new AtlasOutputFormat(GarminCustom.class));
		FORMATS.add(new AtlasOutputFormat(Glopus.class));
		FORMATS.add(new AtlasOutputFormat(GlopusMapFile.class));
		FORMATS.add(new AtlasOutputFormat(GoogleEarthOverlay.class));
		FORMATS.add(new AtlasOutputFormat(GpsSportsTracker.class));
		FORMATS.add(new AtlasOutputFormat(MagellanRmp.class));
		FORMATS.add(new AtlasOutputFormat(Maplorer.class));
		FORMATS.add(new AtlasOutputFormat(Maverick.class));
		FORMATS.add(new AtlasOutputFormat(MGMaps.class));
		FORMATS.add(new AtlasOutputFormat(MobileTrailExplorer.class));
		FORMATS.add(new AtlasOutputFormat(MobileTrailExplorerCache.class));
		FORMATS.add(new AtlasOutputFormat(NaviComputer.class));
		FORMATS.add(new AtlasOutputFormat(NFComPass.class));
		FORMATS.add(new AtlasOutputFormat(OruxMaps.class));
		FORMATS.add(new AtlasOutputFormat(OruxMapsSqlite.class));
		FORMATS.add(new AtlasOutputFormat(OSMAND.class));
		FORMATS.add(new AtlasOutputFormat(Osmdroid.class));
		FORMATS.add(new AtlasOutputFormat(OsmdroidSQLite.class));
		FORMATS.add(new AtlasOutputFormat(OSMTracker.class));
		FORMATS.add(new AtlasOutputFormat(Ozi.class));
		FORMATS.add(new AtlasOutputFormat(PathAway.class));
		FORMATS.add(new AtlasOutputFormat(PNGWorldfile.class));
		FORMATS.add(new AtlasOutputFormat(RMapsSQLite.class));
		FORMATS.add(new AtlasOutputFormat(RunGPSAtlas.class));
		FORMATS.add(new AtlasOutputFormat(SportsTracker.class));
		FORMATS.add(new AtlasOutputFormat(TTQV.class));
		FORMATS.add(new AtlasOutputFormat(TrekBuddyTared.class));
		FORMATS.add(new AtlasOutputFormat(TrekBuddy.class));
		// FORMATS.add(new AtlasOutputFormat(TwoNavRMAP.class));
		FORMATS.add(new AtlasOutputFormat(Ublox.class));
	}

	public static Vector<AtlasOutputFormat> getFormatsAsVector() {
		return new Vector<AtlasOutputFormat>(FORMATS);
	}

	public static AtlasOutputFormat getFormatByName(String Name) {
		for (AtlasOutputFormat af : FORMATS) {
			if (af.getTypeName().equals(Name))
				return af;
		}
		throw new NoSuchElementException("Unknown atlas format: \"" + Name + "\"");
	}

	private Class<? extends AtlasCreator> atlasCreatorClass;

	private AtlasOutputFormat(Class<? extends AtlasCreator> mapCreatorClass) {
		if (mapCreatorClass == null)
			throw new NullPointerException();
		this.atlasCreatorClass = mapCreatorClass;
	}

	protected AtlasCreatorName getAtlasCreatorNameAnnotation() {
		AtlasCreatorName acName = atlasCreatorClass.getAnnotation(AtlasCreatorName.class);
		if (acName == null)
			throw new RuntimeException("AtlasCreator " + atlasCreatorClass.getName() + " has no name");
		return acName;
	}

	public String toString() {
		return getAtlasCreatorNameAnnotation().value();
	}

	public Class<? extends AtlasCreator> getMapCreatorClass() {
		return atlasCreatorClass;
	}

	public String getTypeName() {
		AtlasCreatorName acName = getAtlasCreatorNameAnnotation();
		String type = acName.type();
		if (type == null || type.length() == 0)
			return getMapCreatorClass().getSimpleName();
		return type;
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

	public int compareTo(AtlasOutputFormat o) {
		return this.toString().compareTo(o.toString());
	}

}
