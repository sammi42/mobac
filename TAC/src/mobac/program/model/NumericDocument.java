package mobac.program.model;

import java.awt.Toolkit;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

public class NumericDocument extends PlainDocument {
	private static final long serialVersionUID = 1L;
	public static final String NUMERIC = "0123456789";

	public void insertString(int offset, String str, AttributeSet attr) throws BadLocationException {

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
