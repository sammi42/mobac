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
package mobac.mapsources.impl;

import mobac.mapsources.AbstractMapSource;
import mobac.mapsources.MapSourceTools;

public class Microsoft {

	/**
	 * Uses QuadTree coordinate system for addressing a tile. See <a
	 * href="http://msdn.microsoft.com/en-us/library/bb259689.aspx">Virtual Earth Tile System</a> for details.
	 */
	public static abstract class AbstractMicrosoft extends AbstractMapSource {

		protected String urlBase = ".ortho.tiles.virtualearth.net/tiles/";
		protected String urlAppend = "?g=45";
		protected int serverNum = 0;
		protected int serverNumMax = 4;
		protected char mapTypeChar;

		public AbstractMicrosoft(String name, String tileType, char mapTypeChar, TileUpdate tileUpdate) {
			super(name, 1, 19, tileType, tileUpdate);
			this.mapTypeChar = mapTypeChar;
		}

		public String getTileUrl(int zoom, int tilex, int tiley) {
			String tileNum = MapSourceTools.encodeQuadTree(zoom, tilex, tiley);
			serverNum = (serverNum + 1) % serverNumMax;
			return "http://" + mapTypeChar + serverNum + urlBase + mapTypeChar + tileNum + "." + tileType + urlAppend;
		}

		@Override
		public String toString() {
			return getName();
		}
	}

	public static class MicrosoftMaps extends AbstractMicrosoft {

		public MicrosoftMaps() {
			super("Microsoft Maps", "png", 'r', TileUpdate.IfNoneMatch);
		}

	}
	public static class MicrosoftMapsHillShade extends AbstractMicrosoft {

		public MicrosoftMapsHillShade() {
			super("Microsoft Maps with hill shade", "png", 'r', TileUpdate.IfNoneMatch);
			urlAppend = "?g=563&shading=hill";
		}

	}
	public static class MicrosoftMapsChina extends AbstractMicrosoft {

		public MicrosoftMapsChina() {
			super("Microsoft Maps China", "png", 'r', TileUpdate.IfNoneMatch);
			urlBase = ".tiles.ditu.live.com/tiles/";
			urlAppend = "?g=1";
			maxZoom = 18;
		}

	}

	public static class MicrosoftVirtualEarth extends AbstractMicrosoft {

		public MicrosoftVirtualEarth() {
			super("Microsoft Virtual Earth", "jpg", 'a', TileUpdate.IfNoneMatch);
		}

	}

	public static class MicrosoftHybrid extends AbstractMicrosoft {

		public MicrosoftHybrid() {
			super("Microsoft Hybrid", "jpg", 'h', TileUpdate.None);
		}

		@Override
		public String toString() {
			return "Microsoft Maps/Earth Hybrid";
		}

	}

	public static class MicrosoftOrdnanceSurveyExplorer extends AbstractMapSource {

		public MicrosoftOrdnanceSurveyExplorer() {
			super("Ordnance Survey Explorer Maps (UK)", 12, 16, "png", TileUpdate.IfNoneMatch);
		}

		@Override
		public String getTileUrl(int zoom, int tilex, int tiley) {
			String tileNum = MapSourceTools.encodeQuadTree(zoom, tilex, tiley);
			String urlAppend = "?g=41&productSet=mmOS";
			return "http://ecn.t2.tiles.virtualearth.net/tiles/r" + tileNum + "." + tileType + urlAppend;
		}
		
		
	}
	
}
