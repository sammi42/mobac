package tac.gui;

import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.HeadlessException;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.DataInputStream;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;

import tac.Main;
import tac.utilities.GBC;

public class Help extends JFrame implements WindowListener {

	private static Help INSTANCE = null;

	public static synchronized void showHelp() {
		if (INSTANCE == null)
			INSTANCE = new Help();
		INSTANCE.setVisible(true);
	}

	public Help() throws HeadlessException {
		super("Help");
		setLayout(new GridBagLayout());
		JLabel text = new JLabel();
		JButton closeButton = new JButton("Close");
		closeButton.setDefaultCapable(true);
		closeButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				setVisible(false);
				dispose();
			}
		});

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
			//helpMessage = helpMessage.replaceAll("\n", "");
			text.setText(helpMessage);
		} catch (IOException e) {
		}
		add(text, GBC.eol().insets(10, 10, 10, 10));
		add(closeButton, GBC.eol().anchor(GBC.CENTER).insets(0, 0, 0, 10));
		pack();

		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		setLocation((dim.width - getWidth()) / 2, (dim.height - getHeight()) / 2);

		setAlwaysOnTop(true);
		setResizable(false);
	}

	public void windowActivated(WindowEvent e) {
	}

	public void windowClosed(WindowEvent e) {
		INSTANCE = null;
	}

	public void windowClosing(WindowEvent e) {
	}

	public void windowDeactivated(WindowEvent e) {
	}

	public void windowDeiconified(WindowEvent e) {
	}

	public void windowIconified(WindowEvent e) {
	}

	public void windowOpened(WindowEvent e) {
	}
}