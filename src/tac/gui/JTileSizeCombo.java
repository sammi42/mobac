package tac.gui;

import java.awt.Component;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.ComboBoxEditor;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.UIManager;

public class JTileSizeCombo extends JComboBox {

	static Vector<SizeEntry> TILE_SIZE_VALUES;

	static {
		TILE_SIZE_VALUES = new Vector<SizeEntry>();
		TILE_SIZE_VALUES.addElement(new SizeEntry(64));
		TILE_SIZE_VALUES.addElement(new SizeEntry(128));
		TILE_SIZE_VALUES.addElement(new SizeEntry(256) {

			@Override
			public String toString() {
				return super.toString() + " (def)";
			}
			
		});
		for (int i = 2; i < 8; i++)
			TILE_SIZE_VALUES.addElement(new SizeEntry(i * 256));
	}

	protected JTileSizeField editorComponent;

	public JTileSizeCombo() {
		super(TILE_SIZE_VALUES);
		setEditable(true);
		setEditor(new Editor());
		setMaximumRowCount(10);
	}

	public static class SizeEntry {

		private int size;

		protected SizeEntry(int size) {
			this.size = size;
		}

		public int getSize() {
			return size;
		}

		@Override
		public String toString() {
			return Integer.toString(size);
		}

	}

	public class Editor implements ComboBoxEditor {

		public Editor() {
			super();
			editorComponent = new JTileSizeField();
		}

		public void addActionListener(ActionListener l) {

		}

		public Component getEditorComponent() {
			return editorComponent;
		}

		public Object getItem() {
			return null;
		}

		public void removeActionListener(ActionListener l) {

		}

		public void selectAll() {
			editorComponent.selectAll();
		}

		public void setItem(Object entry) {
			editorComponent.setTileSize(((SizeEntry) entry).size);
		}

	}

	public static void main(String[] args) throws Exception {
		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.add(new JTileSizeCombo());
		frame.pack();
		frame.setBounds(100, 100, frame.getWidth(), frame.getHeight());
		frame.setVisible(true);
	}
}
