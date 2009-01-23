package tac.gui;

import java.awt.Component;
import java.awt.event.ActionListener;
import java.text.ParseException;
import java.util.Vector;

import javax.swing.ComboBoxEditor;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.UIManager;

import org.apache.log4j.Logger;

import tac.program.Logging;

public class JTileSizeCombo extends JComboBox {

	static Vector<SizeEntry> TILE_SIZE_VALUES;

	static SizeEntry DEFAULT;

	static Logger log = Logger.getLogger(JTileSizeCombo.class);

	static {
		DEFAULT = new SizeEntry(256);
		TILE_SIZE_VALUES = new Vector<SizeEntry>();
		TILE_SIZE_VALUES.addElement(new SizeEntry(64));
		TILE_SIZE_VALUES.addElement(new SizeEntry(128));
		TILE_SIZE_VALUES.addElement(DEFAULT);
		for (int i = 2; i < 8; i++)
			TILE_SIZE_VALUES.addElement(new SizeEntry(i * 256));
	}

	protected JTileSizeField editorComponent;

	public JTileSizeCombo() {
		super(TILE_SIZE_VALUES);
		editorComponent = new JTileSizeField();
		setEditable(true);
		setEditor(new Editor());
		setMaximumRowCount(TILE_SIZE_VALUES.size());
		setSelectedItem(DEFAULT);
	}

	public int getTileSize() {
		try {
			return editorComponent.getTileSize();
		} catch (ParseException e) {
			throw new RuntimeException(e);
		}
	}

	public void setTileSize(int newTileSize) {
		setSelectedIndex(-1);
		editorComponent.setTileSize(newTileSize);
		log.debug("tile size set: " + newTileSize + " get = " + getTileSize());
	}
	
	public boolean isTileSizeValid() {
		return editorComponent.isInputValid();
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
		}

		public void addActionListener(ActionListener l) {
			editorComponent.addActionListener(l);
		}

		public Component getEditorComponent() {
			return editorComponent;
		}

		public Object getItem() {
			return null;
		}

		public void removeActionListener(ActionListener l) {
			editorComponent.removeActionListener(l);
		}

		public void selectAll() {
			editorComponent.selectAll();
		}

		public void setItem(Object entry) {
			if (entry == null)
				return;
			editorComponent.setTileSize(((SizeEntry) entry).size);
		}

	}

	public static void main(String[] args) throws Exception {
		Logging.configureLogging();
		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		JFrame frame = new JFrame();
		frame.setLayout(new java.awt.FlowLayout());
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		JTileSizeCombo combo = new JTileSizeCombo();
		combo.setTileSize(100);
		frame.add(new JComboBox());
		combo.setTileSize(110);
		frame.add(combo);
		frame.pack();
		frame.setBounds(100, 100, frame.getWidth(), frame.getHeight());
		frame.setVisible(true);
	}
}
