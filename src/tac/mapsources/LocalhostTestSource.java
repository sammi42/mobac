package tac.mapsources;

public class LocalhostTestSource extends AbstractMapSource {

	public LocalhostTestSource() {
		super("Localhost test", 0, 18, "png");
	}
	
	public String getTileUrl(int zoom, int tilex, int tiley) {
		return "http://localhost/tile?x=" + tilex + "&y=" + tiley + "&z=" + zoom;
	}

}
