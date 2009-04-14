package tac.gui.components;

import java.util.Vector;

import org.apache.log4j.Logger;

public class JTileSizeCombo extends JIntCombo {

	private static final long serialVersionUID = 1L;

	public static final int MIN = 50;

	public static final int MAX = 1792;

	static Vector<Integer> TILE_SIZE_VALUES;

	static Integer DEFAULT;

	static Logger log = Logger.getLogger(JTileSizeCombo.class);

	static {
		DEFAULT = new Integer(256);
		TILE_SIZE_VALUES = new Vector<Integer>();
		TILE_SIZE_VALUES.addElement(new Integer(64));
		TILE_SIZE_VALUES.addElement(new Integer(128));
		TILE_SIZE_VALUES.addElement(DEFAULT);
		for (int i = 2; i < 8; i++)
			TILE_SIZE_VALUES.addElement(new Integer(i * 256));
	}

	public JTileSizeCombo() {
		super(TILE_SIZE_VALUES, DEFAULT);
		setEditable(true);
		setEditor(new Editor());
		setMaximumRowCount(TILE_SIZE_VALUES.size());
		setSelectedItem(DEFAULT);
	}

	@Override
	protected void createEditorComponent() {
		editorComponent = new JIntField(MIN, MAX, 4, "<html>Invalid tile size!<br>"
				+ "Please enter a number between %d and %d</html>");
	}

}
