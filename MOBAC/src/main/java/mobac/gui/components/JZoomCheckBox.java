package mobac.gui.components;

import javax.swing.JCheckBox;

public class JZoomCheckBox extends JCheckBox {

	private final int zoomLevel;

	public JZoomCheckBox(int zoomLevel) {
		super();
		this.zoomLevel = zoomLevel;
		setToolTipText("Select zoom level " + zoomLevel + " for atlas");
	}

	public int getZoomLevel() {
		return zoomLevel;
	}

	
}
