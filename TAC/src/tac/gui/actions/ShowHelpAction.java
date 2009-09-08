package tac.gui.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.IOException;

import javax.swing.JOptionPane;

import tac.Main;
import tac.program.Logging;

public class ShowHelpAction implements ActionListener {
	public void actionPerformed(ActionEvent event) {
		DataInputStream in = new DataInputStream(Main.class
				.getResourceAsStream("resources/text/help_dialog.html"));
		byte[] buf;
		try {
			buf = new byte[in.available()];
			in.readFully(buf);
			in.close();
			String helpMessage = new String(buf, "UTF-8");
			// Strip out all line breaks because JOptionPane shows
			// the raw HTML code otherwise
			helpMessage = helpMessage.replaceAll("\n", "");
			JOptionPane.showMessageDialog(null, helpMessage, "Help",
					JOptionPane.INFORMATION_MESSAGE);
		} catch (IOException e) {
			Logging.LOG.error("", e);
		}
	}
}
