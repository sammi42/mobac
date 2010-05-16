package mobac.mapsources.custom;

import java.awt.Color;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import mobac.mapsources.mapspace.MercatorPower2MapSpace;

import org.openstreetmap.gui.jmapviewer.interfaces.MapSource;
import org.openstreetmap.gui.jmapviewer.interfaces.MapSpace;


/**
 * Custom tile store provider, configurable via settings.xml.
 */
@XmlRootElement
public class CustomMapSource implements MapSource {

	@XmlElement(nillable = false, defaultValue = "Custom")
	private String name = "Custom";

	@XmlElement(defaultValue = "0")
	private int minZoom = 0;

	@XmlElement(required = true)
	private int maxZoom = 0;

	@XmlElement(defaultValue = "png")
	private String tileType = "png";

	@XmlElement(defaultValue = "NONE")
	private TileUpdate tileUpdate;

	@XmlElement(required = true, nillable = false)
	private String url = "http://127.0.0.1/{$x}_{$y}_{$z}";

	/**
	 * Constructor without parameters - required by JAXB
	 */
	public CustomMapSource() {
	}

	public TileUpdate getTileUpdate() {
		return tileUpdate;
	}

	public int getMaxZoom() {
		return maxZoom;
	}

	public int getMinZoom() {
		return minZoom;
	}

	public String getName() {
		return name;
	}

	public String getStoreName() {
		return name;
	}

	public String getTileType() {
		return tileType;
	}

	public HttpURLConnection getTileUrlConnection(int zoom, int tilex, int tiley)
			throws IOException {
		String url = getTileUrl(zoom, tilex, tiley);
		if (url == null)
			return null;
		return (HttpURLConnection) new URL(url).openConnection();
	}

	public String getTileUrl(int zoom, int tilex, int tiley) {
		String tmp = url;
		tmp = tmp.replace("{$x}", Integer.toString(tilex));
		tmp = tmp.replace("{$y}", Integer.toString(tiley));
		tmp = tmp.replace("{$z}", Integer.toString(zoom));
		return tmp;
	}

	public boolean allowFileStore() {
		return true;
	}

	@Override
	public String toString() {
		return name;
	}

	public MapSpace getMapSpace() {
		return MercatorPower2MapSpace.INSTANCE_256;
	}

	public Color getBackgroundColor() {
		return Color.BLACK;
	}
	
}
