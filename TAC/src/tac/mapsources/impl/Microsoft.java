package tac.mapsources.impl;

import tac.mapsources.AbstractMapSource;

public class Microsoft {

	/**
	 * Uses QuadTree coordinate system for addressing a tile. See <a
	 * href="http://msdn.microsoft.com/en-us/library/bb259689.aspx">Virtual
	 * Earth Tile System</a> for details.
	 */
	public static abstract class AbstractMicrosoft extends AbstractMapSource {

		protected static final char[] NUM_CHAR = { '0', '1', '2', '3' };

		protected String urlBase = ".ortho.tiles.virtualearth.net/tiles/";
		protected String urlAppend = "?g=45";
		protected int serverNum = 0;
		protected int serverNumMax = 4;
		protected char mapTypeChar;

		public AbstractMicrosoft(String name, String tileType, char mapTypeChar) {
			super(name, 1, 19, tileType);
			this.mapTypeChar = mapTypeChar;
		}

		public TileUpdate getTileUpdate() {
			return TileUpdate.None;
		}

		public String getTileUrl(int zoom, int tilex, int tiley) {
			char[] tileNum = new char[zoom];
			for (int i = zoom - 1; i >= 0; i--) {
				int num = (tilex % 2) | ((tiley % 2) << 1);
				tileNum[i] = NUM_CHAR[num];
				tilex >>= 1;
				tiley >>= 1;
			}
			serverNum = (serverNum + 1) % serverNumMax;
			return "http://" + mapTypeChar + serverNum + urlBase + mapTypeChar
					+ new String(tileNum) + "." + tileType + urlAppend;
		}

		@Override
		public String toString() {
			return getName();
		}
	}

	public static class MicrosoftMaps extends AbstractMicrosoft {

		public MicrosoftMaps() {
			super("Microsoft Maps", "png", 'r');
		}

	}

	public static class MicrosoftMapsChina extends AbstractMicrosoft {

		public MicrosoftMapsChina() {
			super("Microsoft Maps China", "png", 'r');
			urlBase = ".tiles.ditu.live.com/tiles/";
			urlAppend = "?g=1";
			maxZoom = 18;
		}

	}

	public static class MicrosoftVirtualEarth extends AbstractMicrosoft {

		public MicrosoftVirtualEarth() {
			super("Microsoft Virtual Earth", "jpg", 'a');
		}

	}

	public static class MicrosoftHybrid extends AbstractMicrosoft {

		public MicrosoftHybrid() {
			super("Microsoft Hybrid", "jpg", 'h');
		}

		@Override
		public String toString() {
			return "Microsoft Maps/Earth Hybrid";
		}

	}
}
