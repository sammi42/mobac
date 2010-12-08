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
package mobac.gui.actions;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.StringWriter;
import java.util.Collections;
import java.util.LinkedList;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import mobac.gui.MainGUI;
import mobac.mapsources.MapSourcesManager;
import mobac.program.interfaces.MapSource;

public class DebugShowMapSourceNames implements ActionListener {

	public void actionPerformed(ActionEvent e) {
		LinkedList<String> names = new LinkedList<String>();
		for (MapSource ms : MapSourcesManager.getInstance().getAllLayerMapSources()) {
			names.add(ms.getName());
		}
		Collections.sort(names);
		StringWriter sw = new StringWriter();
		for (String name : names)
			sw.append(name+"\n");
		
		JFrame dialog = new JFrame("Map source names");
		dialog.setLocationRelativeTo(MainGUI.getMainGUI());
		dialog.setLocation(100, 50);
		Dimension dScreen = Toolkit.getDefaultToolkit().getScreenSize();
		dScreen.height -= 200;
		dScreen.width = Math.min(dScreen.width-100, 500);
		dialog.setSize(dScreen);
		dialog.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		JTextArea namesArea = new JTextArea(sw.toString());
		JScrollPane scroller = new JScrollPane(namesArea);
		dialog.add(scroller);
		dialog.setVisible(true);
	}

}
