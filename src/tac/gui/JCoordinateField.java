package tac.gui;

import javax.swing.JTextField;

import tac.utilities.Utilities;

public class JCoordinateField extends JTextField {

	private static final long serialVersionUID = 1L;

	private JCoordinateListener coordinateListener;

	public JCoordinateField(double min, double max) {
		super();
		coordinateListener = JCoordinateListener.Add(this, max, min);
	}

	public synchronized void setCoordinate(double value) {
		try {
			// We know that the number is valid, therefore we can skip the check
			// -> saves CPU power while selecting via preview map
			coordinateListener.setEnabled(false);
			if (Double.isNaN(value))
				super.setText("");
			else
				super.setText(Utilities.FORMAT_6_DEC.format(value));
		} finally {
			coordinateListener.setEnabled(true);
		}
	}

	public void setText(String t) {
		throw new RuntimeException("Calling setText() is not allowed!");
	}
}
