package mobac.gui.components;

import java.awt.Toolkit;
import java.util.regex.Pattern;

import javax.swing.JTextField;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

public class JRegexTextField extends JTextField {

	private static final long serialVersionUID = 1L;

	public JRegexTextField(String regex, int maxLength) {
		super();
		setDocument(new AtlasNameDocument(regex, maxLength));
	}

	public class AtlasNameDocument extends PlainDocument {

		private static final long serialVersionUID = 1L;

		public Pattern pattern;
		public int maxLength;

		public AtlasNameDocument(String regex, int maxLength) {
			super();
			pattern = Pattern.compile(regex);
			this.maxLength = maxLength;
		}

		public void insertString(int offset, String str, AttributeSet attr)
				throws BadLocationException {

			if (str == null)
				return;

			if (!pattern.matcher(str).matches()) {
				Toolkit.getDefaultToolkit().beep();
				return;
			}

			String oldText = JRegexTextField.this.getText();

			super.insertString(offset, str, attr);

			// Maximum length exceeded?
			if (JRegexTextField.this.getText().length() > maxLength) {
				JRegexTextField.this.setText(oldText);
				Toolkit.getDefaultToolkit().beep();
			}
		}
	}
}
