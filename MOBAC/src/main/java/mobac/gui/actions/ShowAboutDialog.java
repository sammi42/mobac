package mobac.gui.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import mobac.gui.dialogs.AboutDialog;

public class ShowAboutDialog implements ActionListener {

	public void actionPerformed(ActionEvent e) {
		new AboutDialog().setVisible(true);
	}

}
