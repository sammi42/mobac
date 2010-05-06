package mobac.gui.panels;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import mobac.gui.atlastree.JAtlasTree;
import mobac.gui.components.JCollapsiblePanel;
import mobac.gui.components.JProfilesComboBox;
import mobac.program.model.Profile;
import mobac.utilities.GBC;
import mobac.utilities.Utilities;

public class JProfilesPanel extends JCollapsiblePanel {

	private static final long serialVersionUID = 1L;

	private JProfilesComboBox profilesCombo;
	private JButton reloadButton;
	private JButton deleteButton;
	private JButton loadButton;
	private JButton saveAsButton;

	public JProfilesPanel(JAtlasTree atlasTree) {
		super("Saved profiles", new GridBagLayout());

		if (atlasTree == null)
			throw new NullPointerException();

		// profiles combo box
		profilesCombo = new JProfilesComboBox();
		profilesCombo.setToolTipText("Select an atlas creation profile\n "
				+ "or enter a name for a new profile");
		profilesCombo.addActionListener(new ProfileListListener());

		// delete profile button
		deleteButton = new JButton("Delete");
		deleteButton.addActionListener(new DeleteProfileListener());
		deleteButton.setToolTipText("Delete atlas profile from list");

		// save as profile button
		saveAsButton = new JButton("Save");
		saveAsButton.setToolTipText("Save atlas profile");
		saveAsButton.addActionListener(new SaveAsProfileListener(atlasTree));

		loadButton = new JButton("Load");
		loadButton.setToolTipText("Load the selected profile");

		GBC gbc = GBC.eol().fill().insets(5, 5, 5, 5);
		reloadButton = new JButton(Utilities.loadResourceImageIcon("refresh.png"));
		reloadButton.setToolTipText("reload the profiles list");
		reloadButton.addActionListener(new ReloadListener());
		reloadButton.setPreferredSize(new Dimension(24, 0));

		JPanel p = new JPanel(new BorderLayout());
		p.add(profilesCombo, BorderLayout.CENTER);
		p.add(reloadButton, BorderLayout.EAST);

		contentContainer.add(p, gbc);
		contentContainer.add(deleteButton, gbc.toggleEol());
		contentContainer.add(saveAsButton, gbc);
		contentContainer.add(loadButton, gbc.toggleEol());

		saveAsButton.setEnabled(false);
		deleteButton.setEnabled(false);
		loadButton.setEnabled(false);
	}

	public void initialize() {
		// Load all profiles from the profiles file from disk
		profilesCombo.loadProfilesList();
		deleteButton.setEnabled(false);
		loadButton.setEnabled(false);
	}

	public void reloadProfileList() {
		initialize();
	}

	public JProfilesComboBox getProfilesCombo() {
		return profilesCombo;
	}

	public JButton getLoadButton() {
		return loadButton;
	}

	public JButton getDeleteButton() {
		return deleteButton;
	}

	public JButton getSaveAsButton() {
		return saveAsButton;
	}

	public Profile getSelectedProfile() {
		return profilesCombo.getSelectedProfile();
	}

	private class SaveAsProfileListener implements ActionListener {

		JAtlasTree jAtlasTree;

		public SaveAsProfileListener(JAtlasTree atlasTree) {
			super();
			jAtlasTree = atlasTree;
		}

		public void actionPerformed(ActionEvent e) {
			if (!jAtlasTree.testAtlasContentValid())
				return;
			Object selObject = profilesCombo.getEditor().getItem();
			String profileName = null;
			Profile profile = null;
			if (selObject instanceof Profile) {
				profile = (Profile) selObject;
				profileName = profile.getName();
			} else
				profileName = (String) selObject;

			if (profileName.length() == 0) {
				JOptionPane.showMessageDialog(null, "Please enter a profile name", "Error",
						JOptionPane.ERROR_MESSAGE);
				return;
			}

			profile = new Profile(profileName);

			if (profile.exists()) {
				int response = JOptionPane.showConfirmDialog(null, "Profile \"" + profileName
						+ "\" already exists. Overwrite?", "Please confirm",
						JOptionPane.YES_NO_OPTION);
				if (response == JOptionPane.NO_OPTION)
					return;
			}

			if (jAtlasTree.save(profile)) {
				reloadProfileList();
				profilesCombo.setSelectedItem(profile);
			}
		}
	}

	private class DeleteProfileListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			profilesCombo.deleteSelectedProfile();
		}
	}

	private class ReloadListener implements ActionListener {

		public void actionPerformed(ActionEvent e) {
			reloadProfileList();
		}
	}

	private class ProfileListListener implements ActionListener {

		public void actionPerformed(ActionEvent e) {
			boolean existingProfileSelected = profilesCombo.getSelectedProfile() != null;
			loadButton.setEnabled(existingProfileSelected);
			deleteButton.setEnabled(existingProfileSelected);
		}
	}
}
