package mobac.utilities.geo;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.FieldPosition;
import java.text.NumberFormat;
import java.text.ParsePosition;

import org.apache.log4j.Logger;

public class CoordinateDm2Format extends NumberFormat {

	protected static Logger log = Logger.getLogger(CoordinateDms2Format.class);
	
	NumberFormat degFmt;
	NumberFormat minFmt;
	NumberFormat minFmtParser;

	public CoordinateDm2Format(DecimalFormatSymbols dfs) {
		degFmt = new DecimalFormat("00°", dfs);
		minFmt = new DecimalFormat("00.00''", dfs);
		minFmtParser = new DecimalFormat("##.##", dfs);
	}

	@Override
	public StringBuffer format(double number, StringBuffer toAppendTo, FieldPosition pos) {
		int degrees = (int) Math.floor(number);
		number = (number - degrees) * 60;
		double minutes = number;
		toAppendTo.append(degFmt.format(degrees) + " ");
		toAppendTo.append(minFmt.format(minutes) + " ");
		return toAppendTo;
	}

	@Override
	public StringBuffer format(long number, StringBuffer toAppendTo, FieldPosition pos) {
		throw new RuntimeException("Not implemented");
	}

	@Override
	public Number parse(String source, ParsePosition parsePosition) {
		String[] tokens = source.trim().split("[°\\']");
		if (tokens.length != 2)
			return null;
		try {
			int deg = Integer.parseInt(tokens[0].trim());
			double min = minFmtParser.parse(tokens[1].trim()).doubleValue();
			double coord = min / 60.0 + deg;
			return new Double(coord);
		} catch (Exception e) {
			parsePosition.setErrorIndex(0);
			log.error("e");
			return null;
		}
	}

}
