package mobac.gui.actions;

import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import javax.swing.JOptionPane;

import mobac.gui.MainGUI;
import mobac.program.DirectoryManager;
import mobac.utilities.GUIExceptionHandler;

public class ShowReadme implements ActionListener {

	public void actionPerformed(ActionEvent event) {
		File readme = new File(DirectoryManager.programDir, "README.HTM");
		if (!readme.isFile()) {
			JOptionPane.showMessageDialog(MainGUI.getMainGUI(), "Unable to find README.HTM", "Error",
					JOptionPane.ERROR_MESSAGE);
			return;
		}
		try {
			Desktop.getDesktop().browse(readme.toURI());
		} catch (IOException e) {
			GUIExceptionHandler.processException(e);
		}
	}

}
