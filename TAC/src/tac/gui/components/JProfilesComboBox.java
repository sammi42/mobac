package tac.gui.components;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicComboBoxEditor;

import tac.program.model.Profile;

public class JProfilesComboBox extends JComboBox {

	public JProfilesComboBox() {
		super();
		setEditable(true);
		setEditor(new ProfilesComboBoxEditor());
	}

	public void loadProfilesList() {
		setModel(new DefaultComboBoxModel(Profile.getProfiles()));
		setSelectedIndex(-1);
	}

	public boolean deleteSelectedProfile() {
		Profile profile = (Profile) getSelectedItem();
		if (profile == null)
			return false;
		profile.delete();
		setSelectedIndex(-1);
		removeItem(profile);
		return true;
	}

	/**
	 * 
	 * @return the selected profile or <code>null</code> if no profile is
	 *         selected or a new unsaved profile is selected
	 */
	public Profile getSelectedProfile() {
		Object selItem = getSelectedItem();
		if (selItem instanceof Profile)
			return (Profile) selItem;
		else
			return null;
	}

	protected class ProfilesComboBoxEditor extends BasicComboBoxEditor {
		@Override
		protected JTextField createEditorComponent() {
			JAtlasNameField field = new JAtlasNameField();
			field.setBorder(new EmptyBorder(2, 2, 2, 0));
			return field;
		}
	}
}
