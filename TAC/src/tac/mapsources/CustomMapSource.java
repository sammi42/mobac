package tac.mapsources;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.openstreetmap.gui.jmapviewer.interfaces.MapSource;

/**
 * Custom tile store provider, configurable via settings.xml.
 */
@XmlRootElement
public class CustomMapSource implements MapSource {

	@XmlElement(nillable = false, defaultValue = "Custom")
	private String name;

	@XmlElement(defaultValue = "0")
	private int minZoom;

	@XmlElement(required = true)
	private int maxZoom = 0;

	@XmlElement(defaultValue = "png")
	private String tileType;

	@XmlElement(defaultValue = "NONE")
	private TileUpdate tileUpdate;

	@XmlElement(required = true, nillable = false)
	private String url = null;

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

	public String getTileType() {
		return tileType;
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

}
