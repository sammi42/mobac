package tac.mapsources;

public class Microsoft {

	public static abstract class AbstractMicrosoft extends AbstractMapSource {

		protected static final char[] NUM_CHAR = { '0', '1', '2', '3' };

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
			return "http://" + mapTypeChar + serverNum + ".ortho.tiles.virtualearth.net/tiles/"
					+ mapTypeChar + new String(tileNum) + "." + tileType + "?g=45";
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
