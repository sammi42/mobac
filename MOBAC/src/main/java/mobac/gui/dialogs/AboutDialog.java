package mobac.gui.dialogs;

import java.awt.Dimension;
import java.awt.HeadlessException;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;

import mobac.gui.MainGUI;

public class AboutDialog extends JDialog {

	public AboutDialog() throws HeadlessException {
		super(MainGUI.getMainGUI(), "About");
		setIconImages(MainGUI.MOBAC_ICONS);
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		JButton ok = new JButton("OK");
		add(ok);
		ok.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				AboutDialog.this.dispose();
			}
		});
		setSize(200, 200);

		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		setLocation((dim.width - getWidth()) / 2, (dim.height - getHeight()) / 2);

	}

}
