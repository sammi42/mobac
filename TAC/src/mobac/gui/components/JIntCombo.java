package mobac.gui.components;

import java.awt.Component;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.ComboBoxEditor;
import javax.swing.JComboBox;

import org.apache.log4j.Logger;

public class JIntCombo extends JComboBox {

	private static final long serialVersionUID = 1L;

	static Logger log = Logger.getLogger(JIntCombo.class);

	protected JIntField editorComponent;
	protected Integer defaultValue;

	public JIntCombo(Vector<Integer> values, Integer defaultValue) {
		super(values);
		this.defaultValue = defaultValue;

		createEditorComponent();
		setEditable(true);
		setEditor(new Editor());
		setMaximumRowCount(values.size());
		setSelectedItem(defaultValue);
	}

	protected void createEditorComponent() {
	}

	public int getValue() {
		try {
			return editorComponent.getValue();
		} catch (NumberFormatException e) {
			return defaultValue;
		}
	}

	public void setValue(int newValue) {
		setSelectedIndex(-1);
		editorComponent.setValue(newValue, true);
	}

	public boolean isValueValid() {
		return editorComponent.isInputValid();
	}

	protected class Editor implements ComboBoxEditor {

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
			try {
				return editorComponent.getValue();
			} catch (NumberFormatException e) {
				return getValue();
			}
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
			editorComponent.setValue(((Integer) entry).intValue(), true);
		}

	}

}
