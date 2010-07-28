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
