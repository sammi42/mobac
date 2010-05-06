package mobac.gui.components;

import javax.swing.JTextField;

import mobac.program.model.Profile;


/**
 * A {@link JTextField} that only accepts ASCII characters, numbers and spaces.
 * 
 */
public class JAtlasNameField extends JRegexTextField {

	private static final long serialVersionUID = 1L;

	public JAtlasNameField() {
		super(Profile.PROFILE_NAME_REGEX, 40);
	}

}
