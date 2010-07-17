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
