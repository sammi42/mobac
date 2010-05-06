package mobac.program.model;

import java.awt.Dimension;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class TileImageParameters implements Cloneable {

	@XmlAttribute
	private int width;

	@XmlAttribute
	private int height;

	@XmlAttribute
	private TileImageFormat format;

	/**
	 * Default constructor as required by JAXB
	 */
	@SuppressWarnings("unused")
	private TileImageParameters() {
		super();
	}

	public TileImageParameters(int width, int height, TileImageFormat format) {
		super();
		this.format = format;
		this.height = height;
		this.width = width;
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public Dimension getDimension() {
		return new Dimension(width, height);
	}

	public TileImageFormat getFormat() {
		return format;
	}

	@Override
	public String toString() {
		return "Tile size: (" + width + "/" + height + ") " + format.toString() + "("
				+ format.name() + ")";
	}

	@Override
	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}

}
