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
package mobac.tools;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.HeadlessException;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;

public class MapSourceCapabilityGUI extends JFrame implements ActionListener {

	private static final String[] STATUS = { "failed", "success" };

	private final List<MapSourceCapabilityDetector> result;

	private JTable table;

	private JButton button;

	private Thread workerThread = null;

	public MapSourceCapabilityGUI(List<MapSourceCapabilityDetector> result)
			throws HeadlessException {
		super("Map source capabilities");
		setLayout(new BorderLayout());
		this.result = result;
		table = new JTable(new Model());
		table.setDefaultRenderer(Object.class, new Renderer());
		button = new JButton("Detection running - abort detection");
		button.addActionListener(this);
		add(table.getTableHeader(), BorderLayout.NORTH);
		add(table, BorderLayout.CENTER);
		add(button, BorderLayout.SOUTH);
		setSize(500, 500);
		Dimension dScreen = Toolkit.getDefaultToolkit().getScreenSize();
		setLocation((dScreen.width - getWidth()) / 2, (dScreen.height - getHeight()) / 2);
	}

	public void refresh() {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				table.revalidate();
				table.repaint();
			}
		});
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (workerThread != null)
			workerThread.interrupt();
		else
			dispose();
	}

	public void workerFinished() {
		workerThread = null;
		button.setText("Close");
	}

	public Thread getWorkerThread() {
		return workerThread;
	}

	public void setWorkerThread(Thread workerThread) {
		this.workerThread = workerThread;
	}

	private class Model extends AbstractTableModel {

		public int getRowCount() {
			return result.size();
		}

		public Object getValueAt(int rowIndex, int columnIndex) {

			MapSourceCapabilityDetector mscd = result.get(rowIndex);

			switch (columnIndex) {
			case 0:
				return STATUS[mscd.isSuccess() ? 1 : 0];
			case 1:
				return mscd.getZoom();
			case 2:
				return mscd.iseTagPresent();
			case 3:
				return mscd.isLastModifiedTimePresent();
			case 4:
				return mscd.isIfNoneMatchSupported();
			case 5:
				return mscd.isIfModifiedSinceSupported();
			case 6:
				return mscd.getContentType();
			}
			return null;
		}

		public int getColumnCount() {
			return 7;
		}

		public String getColumnName(int column) {
			switch (column) {
			case 0:
				return "Test";
			case 1:
				return "Zoom";
			case 2:
				return "eTag";
			case 3:
				return "LastModified";
			case 4:
				return "IfNoneMatch";
			case 5:
				return "IfModifiedSince";
			case 6:
				return "Content type";
			}
			return null;
		}

	}

	private class Renderer extends DefaultTableCellRenderer {

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value,
				boolean isSelected, boolean hasFocus, int row, int column) {
			this.setHorizontalAlignment(JLabel.CENTER);
			Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus,
					row, column);
			if ((column > 1) && (value != null) && (value instanceof Boolean) && ((Boolean) value))
				c.setBackground(Color.GREEN);
			else
				c.setBackground(Color.WHITE);
			return c;
		}
	}

}
