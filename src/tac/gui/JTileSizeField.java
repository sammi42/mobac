package tac.gui;

import java.awt.Color;
import java.awt.Toolkit;

import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

import tac.utilities.Utilities;

public class JTileSizeField extends JTextField {

	private static final long serialVersionUID = 1L;

	private static final Color ERROR_COLOR = new Color(255, 100, 100);

	public static final int MIN = 50;

	public static final int MAX = 1792;

	private static final String INVALID_TEXT = "<html>Invalid tile size!<br>"
			+ "Please enter a number between %d and %d</html>";

	private InputListener listener;
	private boolean inputIsValid = true;

	public JTileSizeField() {
		super(4);
		setDocument(new NumericDocument());
		listener = new InputListener();
		listener.checkInput(null);
		setBorder(new EmptyBorder(2, 2, 2, 0));
	}

	public int getTileSize() throws NumberFormatException {
		return Integer.parseInt(getText());
	}

	public void setTileSize(int newTileSize, boolean check) {
		if (newTileSize <= 0)
			super.setText("");
		else
			super.setText(Integer.toString(newTileSize));
		if (check)
			listener.checkInput(null);
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

	public class NumericDocument extends PlainDocument {
		private static final long serialVersionUID = 1L;
		public static final String NUMERIC = "0123456789";

		public void insertString(int offset, String str, AttributeSet attr)
				throws BadLocationException {

			if (str == null)
				return;

			for (char c : str.toCharArray()) {
				if (NUMERIC.indexOf(c) == -1) {
					Toolkit.getDefaultToolkit().beep();
					return;
				}
			}

			super.insertString(offset, str, attr);

		}
	}

	protected class InputListener implements DocumentListener {

		private Color defaultColor;

		private InputListener() {
			defaultColor = JTileSizeField.this.getBackground();
			JTileSizeField.this.getDocument().addDocumentListener(this);
		}

		private void checkInput(DocumentEvent de) {
			boolean valid = false;
			try {
				valid = testInputValid();
			} catch (Exception e) {
				valid = false;
			}
			if (valid != inputIsValid)
				setDisplayedValidMode(valid);
			inputIsValid = valid;
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

	}
}
