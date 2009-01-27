package tac.gui;

import java.awt.Toolkit;
import java.util.regex.Pattern;

import javax.swing.JTextField;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

/**
 * A {@link JTextField} that only accepts ASCII characters, numbers and spaces.
 * 
 */
public class JAtlasNameField extends JTextField {

	public static final Pattern NAME_PATTERN = Pattern.compile("[\\w ]*");

	public JAtlasNameField() {
		super();
		setDocument(new AtlasNameDocument());
	}

	public class AtlasNameDocument extends PlainDocument {

		public void insertString(int offset, String str, AttributeSet attr)
				throws BadLocationException {

			if (str == null)
				return;

			if (!NAME_PATTERN.matcher(str).matches()) {
				Toolkit.getDefaultToolkit().beep();
				return;
			}

			super.insertString(offset, str, attr);
		}
	}
}
