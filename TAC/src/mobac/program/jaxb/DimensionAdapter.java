package mobac.program.jaxb;

import java.awt.Dimension;

import javax.xml.bind.UnmarshalException;
import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * Required {@link XmlAdapter} implementation for serializing a
 * {@link Dimension} as the default one creates a {@link StackOverflowError}
 */
public class DimensionAdapter extends XmlAdapter<String, Dimension> {

	@Override
	public String marshal(Dimension dimension) throws Exception {
		return dimension.width + "/" + dimension.height;
	}

	@Override
	public Dimension unmarshal(String value) throws Exception {
		int i = value.indexOf('/');
		if (i < 0)
			throw new UnmarshalException("Invalid format");
		int width = Integer.parseInt(value.substring(0, i));
		int height = Integer.parseInt(value.substring(i + 1));
		return new Dimension(width, height);
	}
}
