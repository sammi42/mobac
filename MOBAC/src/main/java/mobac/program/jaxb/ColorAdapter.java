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
package mobac.program.jaxb;

import java.awt.Color;

import javax.xml.bind.UnmarshalException;
import javax.xml.bind.annotation.adapters.XmlAdapter;

public class ColorAdapter extends XmlAdapter<String, Color> {

	@Override
	public String marshal(Color color) throws Exception {
		return String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
	}

	@Override
	public Color unmarshal(String value) throws Exception {
		value = value.trim();
		if (value.length() != 7 || !value.startsWith("#"))
			throw new UnmarshalException("Invalid format");
		int r = Integer.parseInt(value.substring(1, 3),16);
		int g = Integer.parseInt(value.substring(3, 5),16);
		int b = Integer.parseInt(value.substring(5, 7),16);
		return new Color(r, g, b);
	}
}
