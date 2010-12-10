/*******************************************************************************
 * Copyright (c) MOBAC developers
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package mobac.gui.dialogs;

import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.HeadlessException;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;

import mobac.gui.MainGUI;
import mobac.program.ProgramInfo;
import mobac.utilities.GBC;

public class AboutDialog extends JDialog {

	public AboutDialog() throws HeadlessException {
		super(MainGUI.getMainGUI(), "About");
		setLayout(new GridBagLayout());
		setIconImages(MainGUI.MOBAC_ICONS);
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		JButton ok = new JButton("OK");
		GBC std = GBC.std();
		GBC eol = GBC.eol();
		std.insets(3, 3, 3, 3);
		eol.insets(3, 3, 3, 3);
		add(new JLabel(new ImageIcon(MainGUI.MOBAC_ICONS.get(0))), std);
		JPanel infoPanel = new JPanel(new GridBagLayout());
		infoPanel.add(new JLabel("<html><h1>" + ProgramInfo.PROG_NAME + "</h1></html>"), eol);
		infoPanel.add(new JLabel("Version:"), std);
		infoPanel.add(new JLabel(ProgramInfo.getVersion()), eol);
		infoPanel.add(new JLabel("Program revision:"), std);
		infoPanel.add(new JLabel(ProgramInfo.getRevision()), eol);
		// infoPanel.add(new JLabel("Map sources revision:"), std);
		// infoPanel.add(new JLabel(Integer.toString(MapSourcesUpdater.getCurrentMapSourcesRev())), eol);
		add(infoPanel, eol);
		add(Box.createVerticalGlue(), eol);
		add(ok, GBC.eol().anchor(GBC.CENTER).insets(5, 10, 10, 10));
		ok.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				AboutDialog.this.dispose();
			}
		});
		pack();

		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		setLocation((dim.width - getWidth()) / 2, (dim.height - getHeight()) / 2);

	}

	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			ProgramInfo.initialize(); // Load revision info
			JDialog dlg = new AboutDialog();
			dlg.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
