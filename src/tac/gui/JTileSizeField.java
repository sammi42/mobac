package tac.gui;

import java.awt.Color;
import java.text.ParseException;

import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import tac.utilities.Utilities;

public class JTileSizeField extends JTextField {

	private static final long serialVersionUID = 1L;

	private static final Color ERROR_COLOR = new Color(255, 100, 100);

	private static final int MIN = 50;

	private static final int MAX = 1792;

	private static final String INVALID_TEXT = "<html>Invalid tile size!<br>"
			+ "Please enter a number between %d and %d</html>";

	private InputListener listener;
	private boolean inputIsValid = true;
	private boolean hasInput = false;

	public JTileSizeField() {
		super();
		listener = new InputListener();
		listener.checkInput(null);
	}

	public synchronized void setCoordinate(double value) {
		try {
			// We know that the number is valid, therefore we can skip the check
			// -> saves CPU power while selecting via preview map
			if (!inputIsValid)
				listener.setDisplayedValidMode(true);
			listener.setEnabled(false);
			if (Double.isNaN(value))
				super.setText("");
			else
				super.setText(Utilities.FORMAT_6_DEC.format(value));
		} finally {
			listener.setEnabled(true);
		}
	}

	public int getTileSize() throws ParseException {
		return Integer.parseInt(getText());
	}

	public void setTileSize(int newTileSize) {
		super.setText(Integer.toString(newTileSize));
	}

	public void setText(String t) {
		throw new RuntimeException("Calling setText() is not allowed!");
	}

	public boolean isInputValid() {
		return testInputValid();
	}

	private boolean testInputValid() {
		try {
			int i = Integer.parseInt(getText());
			return (i >= MIN) && (i <= MAX);
		} catch (NumberFormatException e) {
			return false;
		}
	}

	protected class InputListener implements DocumentListener {

		private Color defaultColor;

		private boolean enabled;

		private InputListener() {
			enabled = true;
			defaultColor = JTileSizeField.this.getBackground();
			JTileSizeField.this.getDocument().addDocumentListener(this);
		}

		private void checkInput(DocumentEvent de) {
			if (!enabled)
				return;
			boolean valid = false;
			try {
				String text = JTileSizeField.this.getText();
				if (text.length() == 0) {
					hasInput = false;
					inputIsValid = false;
					setDisplayedValidMode(true);
					return;
				}
				valid = testInputValid();
			} catch (Exception e) {
				valid = false;
			}
			if (valid != inputIsValid || !hasInput)
				setDisplayedValidMode(valid);
			inputIsValid = valid;
			hasInput = true;
		}

		private void setDisplayedValidMode(boolean valid) {
			Color newC = valid ? defaultColor : ERROR_COLOR;
			JTileSizeField.this.setBackground(newC);
			String toolTip = valid ? "" : String.format(INVALID_TEXT, new Object[] { MIN, MAX });
			JTileSizeField.this.setToolTipText(toolTip);
			if (toolTip.length() > 0)
				Utilities.showTooltipNow(JTileSizeField.this);
		}

		public void changedUpdate(DocumentEvent e) {
			checkInput(e);
		}

		public void insertUpdate(DocumentEvent e) {
			checkInput(e);
		}

		public void removeUpdate(DocumentEvent e) {
			checkInput(e);
		}

		public boolean isEnabled() {
			return enabled;
		}

		public void setEnabled(boolean enabled) {
			this.enabled = enabled;
		}
	}
}
