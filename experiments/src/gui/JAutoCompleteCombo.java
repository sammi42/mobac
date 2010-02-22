package gui;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import javax.swing.text.PlainDocument;

import mobac.mapsources.MapSourcesManager;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;


/**
 * Autocomplete combobox with filtering and text inserting of new text
 * 
 * Source: http://snippets.dzone.com/posts/show/7633
 * 
 * @author Exterminator13
 */
public class JAutoCompleteCombo extends JComboBox implements FocusListener {

	private static final Logger log = Logger.getLogger(JAutoCompleteCombo.class);
	private ComboBoxFilteredModel comboBoxFilteredModel;
	private boolean modelFilling = false;
	private final JTextComponent textComponent;

	private String previousPattern = null;

	public JAutoCompleteCombo(Vector<?> items) {
		super();
		textComponent = (JTextComponent) getEditor().getEditorComponent();
		setEditable(true);
		comboBoxFilteredModel = new ComboBoxFilteredModel(items);

		log.trace("setPattern() called from constructor");
		setPattern(null);

		textComponent.setDocument(new AutoCompleteDocument());
		setModel(comboBoxFilteredModel);
		setSelectedIndex(0);

		getEditor().getEditorComponent().addFocusListener(this);
		getEditor().getEditorComponent().addMouseListener(new MouseAdapter() {

			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2)
					setPopupVisible(true);
			}

		});
	}

	public String getText() {
		return getEditor().getItem().toString();
	}

	private void setPattern(String pattern) {

		if (pattern != null && pattern.trim().isEmpty())
			pattern = null;

		if (previousPattern == null && pattern == null || pattern != null && pattern.equals(previousPattern)) {
			log.trace("[setPatter] pattern is the same as previous: " + previousPattern);
			return;
		}

		previousPattern = pattern;

		modelFilling = true;

		comboBoxFilteredModel.setPattern(pattern);

		// if (log.isTraceEnabled()) {
		// StringBuilder b = new StringBuilder(100);
		// b.append("pattern filter '").append(pattern == null ? "null" :
		// pattern).append("' set:\n");
		// for (int i = 0; i < comboBoxFilteredModel.getSize(); i++) {
		// b.append(", ").append('[').append(comboBoxFilteredModel.getElementAt(i)).append(']');
		// }
		// int ind = b.indexOf(", ");
		// if (ind != -1) {
		// b.delete(ind, ind + 2);
		// }
		// // b.append('\n');
		// log.trace(b);
		// }
		modelFilling = false;
	}

	private void clearSelection() {
		int i = getText().length();
		textComponent.setSelectionStart(i);
		textComponent.setSelectionEnd(i);
	}

	public void focusGained(FocusEvent e) {
		getEditor().selectAll();
	}

	public void focusLost(FocusEvent e) {
		Object selItem = getSelectedItem();
		if (selItem == null)
			return;
		previousPattern = selItem.toString();
		textComponent.setText(previousPattern);
	}

	protected class AutoCompleteDocument extends PlainDocument {

		boolean arrowKeyPressed = false;

		public AutoCompleteDocument() {
			textComponent.addKeyListener(new KeyAdapter() {

				@Override
				public void keyPressed(KeyEvent e) {
					int key = e.getKeyCode();
					if (key == KeyEvent.VK_ESCAPE) {
						
					} else if (key == KeyEvent.VK_UP || key == KeyEvent.VK_DOWN) {
						arrowKeyPressed = true;
						log.trace("arrow key pressed");
					}
				}
			});
		}

		void updateModel() throws BadLocationException {
			String textToMatch = getText(0, getLength());
			log.trace("setPattern() called from updateModel()");
			setPattern(textToMatch);
		}

		@Override
		public void remove(int offs, int len) throws BadLocationException {

			if (modelFilling) {
				log.trace("[remove] model is being filled now");
				return;
			}

			super.remove(offs, len);
			if (arrowKeyPressed) {
				arrowKeyPressed = false;
				log.trace("[remove] arrow key was pressed, updateModel() was NOT called");
			} else {
				log.trace("[remove] calling updateModel()");
				updateModel();
			}
			clearSelection();
		}

		@Override
		public void insertString(int offs, String str, AttributeSet a) throws BadLocationException {
			if (modelFilling) {
				log.trace("[insert] model is being filled now");
				return;
			}

			// insert the string into the document
			super.insertString(offs, str, a);

			String text = getText(0, getLength());
			if (arrowKeyPressed) {
				log.trace("[insert] arrow key was pressed, updateModel() was NOT called");
				comboBoxFilteredModel.setPattern(text);
				log.trace(String.format("[insert] model.setSelectedItem(%s)", text));
				arrowKeyPressed = false;
			} else {
				Object sel = getSelectedItem();
				if (sel != null && !text.equals(sel.toString())) {
					log.trace("[insert] calling updateModel()");
					updateModel();
				}
			}

			clearSelection();
		}

	}

	protected class ComboBoxFilteredModel extends AbstractListModel implements ComboBoxModel {

		private Object selectedItem;

		private final List<Object> list;
		private final List<String> lowercase;
		private List<Object> filtered;

		protected ComboBoxFilteredModel(Vector<?> items) {
			int limit = items.size();
			list = new ArrayList<Object>(limit);
			lowercase = new ArrayList<String>(limit);
			for (Object o : items) {
				list.add(o);
				lowercase.add(o.toString().toLowerCase());
			}
		}

		List<Object> getFiltered() {
			if (filtered == null)
				filtered = list;
			return filtered;
		}

		public void setPattern(String pattern) {

			int size1 = getSize();

			if (pattern == null || pattern.isEmpty()) {
				filtered = list;
				JAutoCompleteCombo.this.setSelectedIndex(0);
				log.trace(String.format("[setPattern] combo.setSelectedItem(null)"));
			} else {
				filtered = new ArrayList<Object>(list.size());
				pattern = pattern.toLowerCase();
				for (int i = 0; i < lowercase.size(); i++) {
					// case insensitive search
					if (lowercase.get(i).contains(pattern)) {
						filtered.add(list.get(i));
					}
				}
				JAutoCompleteCombo.this.setPopupVisible(true);
				log.trace(String.format("[setPattern] combo.setSelectedItem(%s)", pattern));
			}
			log.trace(String.format("pattern:'%s', filtered: %s", pattern, filtered));

			int size2 = getSize();

			if (size1 < size2) {
				fireIntervalAdded(this, size1, size2 - 1);
				fireContentsChanged(this, 0, size1 - 1);
			} else if (size1 > size2) {
				fireIntervalRemoved(this, size2, size1 - 1);
				fireContentsChanged(this, 0, size2 - 1);
			}
		}

		public Object getSelectedItem() {
			return selectedItem;
		}

		public void setSelectedItem(Object anObject) {
			log.trace("Model.setSelectedItem(" + anObject + ")");
			if (anObject instanceof String)
				return;
			if ((selectedItem != null && !selectedItem.equals(anObject)) || selectedItem == null && anObject != null) {
				log.debug("new selectedItem: " + anObject + " " + anObject.getClass().getSimpleName());
				selectedItem = anObject;
				fireContentsChanged(this, -1, -1);
			}
		}

		public int getSize() {
			return getFiltered().size();
		}

		public Object getElementAt(int index) {
			return getFiltered().get(index);
		}

	}

	public static void main(String[] args) {

		// Logger root = Logger.getRootLogger();
		// root.addAppender(new ConsoleAppender(new
		// PatternLayout("%d{ISO8601} [%5p] %m at %l%n")));
		Logger root = Logger.getRootLogger();
		root.setLevel(Level.DEBUG);
		root.addAppender(new ConsoleAppender(new PatternLayout("%d{ISO8601} %m at %L%n")));

		// BasicConfigurator.configure();

		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLayout(new GridLayout(3, 1));
		final JLabel label = new JLabel("label ");
		frame.add(label);
		final JAutoCompleteCombo combo = new JAutoCompleteCombo(MapSourcesManager.getEnabledMapSources());
		combo.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				Object item = combo.getSelectedItem();
				String c = "";
				if (item != null)
					c = item.getClass().toString();
				log.debug("Selection changed: " + item + " (" + c + ")");
			}
		});
		frame.add(combo);
		frame.add(new JButton());
		frame.pack();
		frame.setSize(500, frame.getHeight());
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}

}
