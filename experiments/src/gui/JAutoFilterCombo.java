package gui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Vector;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.plaf.basic.BasicComboBoxRenderer;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import javax.swing.text.PlainDocument;

import mobac.mapsources.MapSourcesManager;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;


public class JAutoFilterCombo extends JComboBox {

	private static final Logger log = Logger.getLogger(JAutoFilterCombo.class);

	public JAutoFilterCombo(Vector<?> items) {
		super(items);
		setEditable(true);
	}

	protected class AutoCompleteDocument extends PlainDocument {

		boolean arrowKeyPressed = false;

		public AutoCompleteDocument() {
			textComponent.addKeyListener(new KeyAdapter() {

				@Override
				public void keyPressed(KeyEvent e) {
					int key = e.getKeyCode();
					if (key == KeyEvent.VK_UP || key == KeyEvent.VK_DOWN) {
						arrowKeyPressed = true;
						log.trace("arrow key pressed");
					}
				}
			});
		}

		void updateModel() throws BadLocationException {
			String textToMatch = getText(0, getLength());
			log.trace("setPattern() called from updateModel()");
			renderer.setFilter(textToMatch);
		}

		@Override
		public void remove(int offs, int len) throws BadLocationException {

			super.remove(offs, len);
			if (arrowKeyPressed) {
				arrowKeyPressed = false;
				log.trace("[remove] arrow key was pressed, updateModel() was NOT called");
			} else {
				log.trace("[remove] calling updateModel()");
				updateModel();
			}
			// clearSelection();
		}

		@Override
		public void insertString(int offs, String str, AttributeSet a) throws BadLocationException {

			// insert the string into the document
			super.insertString(offs, str, a);

			String text = getText(0, getLength());
			if (arrowKeyPressed) {
				log.trace("[insert] arrow key was pressed, updateModel() was NOT called");
				renderer.setFilter(text);
				log.trace(String.format("[insert] model.setSelectedItem(%s)", text));
				arrowKeyPressed = false;
			} else {
				Object sel = getSelectedItem();
				if (sel != null && !text.equals(sel.toString())) {
					log.trace("[insert] calling updateModel()");
					updateModel();
				}
			}

			// clearSelection();
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
		final JAutoFilterCombo combo = new JAutoFilterCombo(MapSourcesManager
				.getEnabledMapSources());
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
