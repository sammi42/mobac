package tac.gui.components;

import javax.swing.JTextField;

/**
 * A {@link JTextField} that only accepts ASCII characters, numbers and spaces.
 * 
 */
public class JAtlasNameField extends JRegexTextField {

	private static final long serialVersionUID = 1L;

	public JAtlasNameField() {
		super("[\\w _-]*", 40);
	}

}
