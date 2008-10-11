package tac.gui;

import java.awt.Color;

import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import tac.utilities.Utilities;

public class JCoordinateListener implements DocumentListener {

	private static final Color ERROR_COLOR = new Color(255, 100, 100);

	private static final String INVALID_TEXT = "<html>Invalid coordinate!<br>"
			+ "Please enter a number between %f and %f</html>";

	private JTextField textField;
	private double min;
	private double max;
	private Color defaultColor;

	private boolean validState;
	private boolean enabled;

	public static JCoordinateListener Add(JCoordinateField textField, double min, double max) {
		return new JCoordinateListener(textField, max, min);
	}

	private JCoordinateListener(JCoordinateField textField, double min, double max) {
		this.textField = textField;
		defaultColor = textField.getBackground();
		validState = true;
		enabled = true;
		this.min = min;
		this.max = max;
		textField.getDocument().addDocumentListener(this);
	}

	private void checkCoordinate(DocumentEvent de) {
		if (!enabled)
			return;
		boolean valid = false;
		try {
			double d = Utilities.parseLocaleDouble(textField.getText());
			valid = (d >= min) && (d <= max);
		} catch (Exception e) {
			valid = false;
		}
		if (valid != validState)
			changeValidMode(valid);
	}

	private void changeValidMode(boolean valid) {
		Color newC = valid ? defaultColor : ERROR_COLOR;
		textField.setBackground(newC);
		String toolTip = valid ? "" : String.format(INVALID_TEXT, new Object[] { min, max });
		textField.setToolTipText(toolTip);
		if (toolTip.length() > 0)
			Utilities.showTooltipNow(textField);
		validState = valid;
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
