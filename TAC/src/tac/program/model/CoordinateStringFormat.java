package tac.program.model;

import java.text.NumberFormat;

import tac.utilities.Utilities;
import tac.utilities.geo.CoordinateDm2Format;
import tac.utilities.geo.CoordinateDms2Format;

public enum CoordinateStringFormat {

	DEG_ENG("Degree (eng)", Utilities.FORMAT_6_DEC_ENG), // 
	DEG_LOCAL("Degree (local)", Utilities.FORMAT_6_DEC), // 
	DM2_ENG("Deg Min (eng)", new CoordinateDm2Format(Utilities.DFS_ENG)), //
	DM2_LOCAL("Deg Min (local)", new CoordinateDm2Format(Utilities.DFS_LOCAL)), //
	DMS2_ENG("Deg Min Sec (eng)", new CoordinateDms2Format(Utilities.DFS_ENG)), //
	DMS2_LOCAL("Deg Min Sec (local)", new CoordinateDms2Format(Utilities.DFS_LOCAL));

	/*
	 * formatButton.addDropDownItem(new JNumberFormatMenuItem());
	 * formatButton.addDropDownItem(new
	 * JNumberFormatMenuItem("Deg Min Sec,2 (local)",
	 */
	private final String displayName;
	private NumberFormat numberFormat;

	private CoordinateStringFormat(String displayName, NumberFormat numberFormat) {
		this.displayName = displayName;
		this.numberFormat = numberFormat;
	}

	public String getDisplayName() {
		return displayName;
	}

	public NumberFormat getNumberFormat() {
		return numberFormat;
	}

	@Override
	public String toString() {
		return displayName;
	}

}
