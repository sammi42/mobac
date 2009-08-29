package tac.gui.panels;

import java.awt.GridBagLayout;
import java.awt.event.ActionListener;
import java.text.ParseException;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.openstreetmap.gui.jmapviewer.interfaces.MapScale;

import tac.gui.components.JCollapsiblePanel;
import tac.gui.components.JCoordinateField;
import tac.program.MapSelection;
import tac.program.model.EastNorthCoordinate;
import tac.program.model.MercatorPixelCoordinate;
import tac.utilities.GBC;

/**
 * Encapsulates all interface components and code for the panel that shows the
 * coordinates of the current selection and allows the user to enter own
 * coordinates.
 */
public class JCoordinatesPanel extends JCollapsiblePanel {

	public static final String NAME = "Coordinates";

	private JCoordinateField latMinTextField;
	private JCoordinateField latMaxTextField;
	private JCoordinateField lonMinTextField;
	private JCoordinateField lonMaxTextField;
	private JButton applySelectionButton;

	public JCoordinatesPanel() {
		super("Selection coordinates (min/max)", new GridBagLayout());
		setName(NAME);
		// coordinates panel
		latMaxTextField = new JCoordinateField(MapSelection.LAT_MIN, MapSelection.LAT_MAX);
		latMaxTextField.setActionCommand("latMaxTextField");
		lonMinTextField = new JCoordinateField(MapSelection.LON_MIN, MapSelection.LON_MAX);
		lonMinTextField.setActionCommand("longMinTextField");
		lonMaxTextField = new JCoordinateField(MapSelection.LON_MIN, MapSelection.LON_MAX);
		lonMaxTextField.setActionCommand("longMaxTextField");
		latMinTextField = new JCoordinateField(MapSelection.LAT_MIN, MapSelection.LAT_MAX);
		latMinTextField.setActionCommand("latMinTextField");

		applySelectionButton = new JButton("Select entered coordinates");

		JLabel latMaxLabel = new JLabel("N ", JLabel.CENTER);
		JLabel lonMinLabel = new JLabel("W ", JLabel.CENTER);
		JLabel lonMaxLabel = new JLabel("E ", JLabel.CENTER);
		JLabel latMinLabel = new JLabel("S ", JLabel.CENTER);

		contentContainer.add(Box.createHorizontalGlue(), GBC.std().fill(GBC.HORIZONTAL));
		contentContainer.add(latMaxLabel, GBC.std().insets(0, 5, 0, 0));
		contentContainer.add(latMaxTextField, GBC.std().insets(0, 5, 0, 0));
		contentContainer.add(Box.createHorizontalGlue(), GBC.eol().fill(GBC.HORIZONTAL));

		JPanel eastWestPanel = new JPanel(new GridBagLayout());
		eastWestPanel.add(lonMinLabel, GBC.std());
		eastWestPanel.add(lonMinTextField, GBC.std());
		eastWestPanel.add(lonMaxLabel, GBC.std().insets(10, 0, 0, 0));
		eastWestPanel.add(lonMaxTextField, GBC.std());
		contentContainer.add(eastWestPanel, GBC.eol().fill().insets(0, 5, 0, 5));
		contentContainer.add(Box.createHorizontalGlue(), GBC.std().fill(GBC.HORIZONTAL));
		contentContainer.add(latMinLabel);
		contentContainer.add(latMinTextField);
		contentContainer.add(Box.createHorizontalGlue(), GBC.eol().fill(GBC.HORIZONTAL));
		contentContainer.add(applySelectionButton, GBC.eol().anchor(GBC.CENTER).insets(0, 5, 0, 0));
	}

	public void setCoordinates(EastNorthCoordinate max, EastNorthCoordinate min) {
		latMaxTextField.setCoordinate(max.lat);
		lonMaxTextField.setCoordinate(max.lon);
		latMinTextField.setCoordinate(min.lat);
		lonMinTextField.setCoordinate(min.lon);
	}

	public void setCoordinates(MapSelection ms) {
		MercatorPixelCoordinate max = ms.getBottomRightPixelCoordinate();
		MercatorPixelCoordinate min = ms.getTopLeftPixelCoordinate();
		setSelection(max, min);
	}

	public void setSelection(MercatorPixelCoordinate max, MercatorPixelCoordinate min) {
		EastNorthCoordinate c1 = min.getEastNorthCoordinate();
		EastNorthCoordinate c2 = max.getEastNorthCoordinate();
		latMaxTextField.setCoordinate(c1.lat);
		lonMaxTextField.setCoordinate(c2.lon);
		latMinTextField.setCoordinate(c2.lat);
		lonMinTextField.setCoordinate(c1.lon);
	}

	/**
	 * Checks if the values for min/max langitude and min/max latitude are
	 * interchanged (smaller value in the max field and larger value in the min
	 * field) and swaps them if necessary.
	 */
	public void correctMinMax() {
		try {
			double lat1 = latMaxTextField.getCoordinate();
			double lat2 = latMinTextField.getCoordinate();
			if (lat1 < lat2) {
				String tmp = latMaxTextField.getText();
				latMaxTextField.setText(latMinTextField.getText());
				latMinTextField.setText(tmp);
			}
		} catch (ParseException e) {
			// one of the lat fields contains an invalid coordinate
		}
		try {
			double lon1 = lonMaxTextField.getCoordinate();
			double lon2 = lonMinTextField.getCoordinate();
			if (lon1 < lon2) {
				String tmp = lonMaxTextField.getText();
				lonMaxTextField.setText(lonMinTextField.getText());
				lonMinTextField.setText(tmp);
			}
		} catch (ParseException e) {
			// one of the lon fields contains an invalid coordinate
		}
	}

	public MapSelection getMapSelection(MapScale mapScale) {
		EastNorthCoordinate max = getMaxCoordinate();
		EastNorthCoordinate min = getMinCoordinate();
		return new MapSelection(mapScale, max, min);
	}

	public EastNorthCoordinate getMaxCoordinate() {
		return new EastNorthCoordinate(latMaxTextField.getCoordinateOrNaN(), lonMaxTextField
				.getCoordinateOrNaN());
	}

	public EastNorthCoordinate getMinCoordinate() {
		return new EastNorthCoordinate(latMinTextField.getCoordinateOrNaN(), lonMinTextField
				.getCoordinateOrNaN());
	}

	public void addButtonActionListener(ActionListener l) {
		applySelectionButton.addActionListener(l);
	}

}
