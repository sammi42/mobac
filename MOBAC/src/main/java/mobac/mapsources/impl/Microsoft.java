package mobac.mapsources.impl;

import mobac.mapsources.AbstractMapSource;
import mobac.mapsources.MapSourceTools;

public class Microsoft {

	/**
	 * Uses QuadTree coordinate system for addressing a tile. See <a
	 * href="http://msdn.microsoft.com/en-us/library/bb259689.aspx">Virtual
	 * Earth Tile System</a> for details.
	 */
	public static abstract class AbstractMicrosoft extends AbstractMapSource {

		protected String urlBase = ".ortho.tiles.virtualearth.net/tiles/";
		protected String urlAppend = "?g=45";
		protected int serverNum = 0;
		protected int serverNumMax = 4;
		protected char mapTypeChar;

		public AbstractMicrosoft(String name, String tileType, char mapTypeChar,
				TileUpdate tileUpdate) {
			super(name, 1, 19, tileType, tileUpdate);
			this.mapTypeChar = mapTypeChar;
		}

		public String getTileUrl(int zoom, int tilex, int tiley) {
			String tileNum = MapSourceTools.encodeQuadTree(zoom, tilex, tiley);
			serverNum = (serverNum + 1) % serverNumMax;
			return "http://" + mapTypeChar + serverNum + urlBase + mapTypeChar + tileNum + "."
					+ tileType + urlAppend;
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
}
