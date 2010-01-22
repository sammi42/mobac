package mobac.gui.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import mobac.gui.Help;


public class ShowHelpAction implements ActionListener {
	public void actionPerformed(ActionEvent event) {
		Help.showHelp();
	}
}
