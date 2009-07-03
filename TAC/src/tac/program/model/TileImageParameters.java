package tac.program.model;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class TileImageParameters {

	@XmlAttribute
	public int width;
	
	@XmlAttribute
	public int height;
	
	@XmlAttribute
	public TileImageFormat format;

	@Override
	public String toString() {
		return "Tile size: (" + width + "/" + height + ") " + format.toString() + "("
				+ format.name() + ")";
	}

}
