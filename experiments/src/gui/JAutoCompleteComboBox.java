package gui;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.swing.AbstractListModel;
import javax.swing.ComboBoxEditor;
import javax.swing.ComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.plaf.basic.BasicComboBoxEditor;

import mobac.mapsources.MapSourcesManager;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.openstreetmap.gui.jmapviewer.interfaces.MapSource;


/**
 * Autocomplete combobox with filtering and text inserting of new text
 * 
 * Source: http://snippets.dzone.com/posts/show/7633
 * 
 * @author Exterminator13
 */
public class JAutoCompleteComboBox<E> extends JComboBox {

	private static final Logger log = Logger.getLogger(JAutoCompleteComboBox.class);

	private final Model<E> dataModel;

	public JAutoCompleteComboBox(Vector<E> items, Class<E> dataClass) {
		super();
		dataModel = new Model<E>(items, dataClass);
		setModel(dataModel);
		setEditor(new Editor());
		setEditable(true);
	}

	@Override
	public void setSelectedItem(Object anObject) {
		log.debug("setSelectedItem(" + anObject + ")");
		super.setSelectedItem(anObject);
	}

	@Override
	public void configureEditor(ComboBoxEditor anEditor, Object anItem) {
		log.debug("configureEditor(" + anEditor + "," + anItem + ")");
		if (anItem != null)
			anEditor.setItem(anItem);
	}

	protected class Editor extends BasicComboBoxEditor implements DocumentListener, FocusListener {

		public Editor() {
			super();
			editor.getDocument().addDocumentListener(this);
			editor.addFocusListener(this);
		}

		@Override
		public Object getItem() {
			log.debug("Editor.getItem");
			return super.getItem();
		}

		@Override
		public void setItem(Object anObject) {
			log.debug("Editor.setItem(" + anObject + ")");
			editor.getDocument().removeDocumentListener(this);
			super.setItem(anObject);
			editor.getDocument().addDocumentListener(this);
		}

		private void editorChanged() {
			dataModel.setFilter(editor.getText());
		}

		@Override
		public void changedUpdate(DocumentEvent e) {
			editorChanged();
		}

		@Override
		public void insertUpdate(DocumentEvent e) {
			editorChanged();
		}

		@Override
		public void removeUpdate(DocumentEvent e) {
			editorChanged();
		}

		@Override
		public void focusGained(FocusEvent e) {
			editor.selectAll();
		}

		@Override
		public void focusLost(FocusEvent e) {
			log.debug("Editor focus lost");
			// editor.setText(JAutoCompleteComboBox.this.getSelectedItem().toString());
		}

	}

	class Model<E> extends AbstractListModel implements ComboBoxModel {

		private final Class<E> dataClass;

		private List<DataElement> allElements;
		private List<DataElement> filtered;

		private E selectedElement = null;

		private String filter = "";

		private class DataElement {
			E data;
			String lowerCase;

			public DataElement(E data) {
				super();
				this.data = data;
				this.lowerCase = data.toString().toLowerCase();
			}
		}

		public Model(Vector<E> items, Class<E> dataClass) {
			super();
			this.dataClass = dataClass;
			allElements = new ArrayList<DataElement>(items.size());
			filtered = new ArrayList<DataElement>(items.size());
			for (E ele : items) {
				DataElement de = new DataElement(ele);
				allElements.add(de);
				filtered.add(de);
			}
		}

		public int setFilter(String text) {
			text = text.toLowerCase();
			if (filter.equals(text))
				return 0;
			this.filter = text;
			ArrayList<DataElement> newFiltered = new ArrayList<DataElement>(allElements.size());

			for (DataElement de : allElements)
				if (de.lowerCase.contains(filter))
					newFiltered.add(de);

			filtered = newFiltered;
			log.debug("fire content changed");
			selectedElement = null;
			fireContentsChanged(this, 0, newFiltered.size());
			return newFiltered.size();
		}

		@Override
		public E getSelectedItem() {
			log.debug("model.getSelectedItem() = " + selectedElement);
			return selectedElement;
		}

		@Override
		public void setSelectedItem(Object anItem) {
			log.debug("model.setSelectedItem(" + anItem + ")");
			if (anItem != null && dataClass.isAssignableFrom(anItem.getClass()))
				selectedElement = (E) anItem;
		}

		@Override
		public E getElementAt(int index) {
			return filtered.get(index).data;
		}

		@Override
		public int getSize() {
			return filtered.size();
		}

	}

	public static void main(String[] args) {

		// Logger root = Logger.getRootLogger();
		// root.addAppender(new ConsoleAppender(new
		// PatternLayout("%d{ISO8601} [%5p] %m at %l%n")));
		Logger root = Logger.getRootLogger();
		root.setLevel(Level.TRACE);
		root.addAppender(new ConsoleAppender(new PatternLayout("%d{ISO8601} %m at %L%n")));

		// BasicConfigurator.configure();

		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLayout(new GridLayout(3, 1));
		final JLabel label = new JLabel("label ");
		frame.add(label);
		final JAutoCompleteComboBox<MapSource> combo;
		combo = new JAutoCompleteComboBox<MapSource>(MapSourcesManager.getEnabledMapSources(),
				MapSource.class);
		combo.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				Object item = combo.getSelectedItem();
				String c = "";
				if (item != null)
					c = item.getClass().toString();
				log.debug("************* Selection changed: " + item + " (" + c + ")");
			}
		});
		combo.setSelectedIndex(0);
		frame.add(combo);
		frame.add(new JButton());
		frame.pack();
		frame.setSize(500, frame.getHeight());
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}

}
