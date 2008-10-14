package tac.gui;

import java.awt.Color;
import java.text.ParseException;

import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import tac.utilities.Utilities;

public class JCoordinateField extends JTextField {

	private static final long serialVersionUID = 1L;

	private static final Color ERROR_COLOR = new Color(255, 100, 100);

	private static final String INVALID_TEXT = "<html>Invalid coordinate!<br>"
			+ "Please enter a number between %f and %f</html>";

	private JCoordinateListener coordinateListener;
	private boolean inputIsValid = true;

	private double min;
	private double max;

	public JCoordinateField(double min, double max) {
		super();
		this.min = min;
		this.max = max;
		coordinateListener = new JCoordinateListener();
		coordinateListener.checkCoordinate(null);
	}

	public synchronized void setCoordinate(double value) {
		try {
			// We know that the number is valid, therefore we can skip the check
			// -> saves CPU power while selecting via preview map
			if (!inputIsValid)
				coordinateListener.changeValidMode(true);
			coordinateListener.setEnabled(false);
			if (Double.isNaN(value))
				super.setText("");
			else
				super.setText(Utilities.FORMAT_6_DEC.format(value));
		} finally {
			coordinateListener.setEnabled(true);
		}
	}

	public double getCoordinate() throws ParseException {
		return Utilities.parseLocaleDouble(getText());
	}

	public void setText(String t) {
		throw new RuntimeException("Calling setText() is not allowed!");
	}
	
	public boolean isInputValid() {
		return inputIsValid;
	}

	
	protected class JCoordinateListener implements DocumentListener {

		private Color defaultColor;

		private boolean enabled;

		private JCoordinateListener() {
			enabled = true;
			defaultColor = JCoordinateField.this.getBackground();
			JCoordinateField.this.getDocument().addDocumentListener(this);
		}

		private void checkCoordinate(DocumentEvent de) {
			if (!enabled)
				return;
			boolean valid = false;
			try {
				double d = Utilities.parseLocaleDouble(JCoordinateField.this.getText());
				valid = (d >= min) && (d <= max);
			} catch (Exception e) {
				valid = false;
			}
			if (valid != inputIsValid)
				changeValidMode(valid);
		}

		private void changeValidMode(boolean valid) {
			Color newC = valid ? defaultColor : ERROR_COLOR;
			JCoordinateField.this.setBackground(newC);
			String toolTip = valid ? "" : String.format(INVALID_TEXT, new Object[] { min, max });
			JCoordinateField.this.setToolTipText(toolTip);
			if (toolTip.length() > 0)
				Utilities.showTooltipNow(JCoordinateField.this);
			inputIsValid = valid;
		}

		public void changedUpdate(DocumentEvent e) {
			checkCoordinate(e);
		}

		public void insertUpdate(DocumentEvent e) {
			checkCoordinate(e);
		}

		public void removeUpdate(DocumentEvent e) {
			checkCoordinate(e);
		}

		public boolean isEnabled() {
			return enabled;
		}

		public void setEnabled(boolean enabled) {
			this.enabled = enabled;
		}
	}
}
