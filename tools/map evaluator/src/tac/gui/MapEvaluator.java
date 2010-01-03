package tac.gui;

import java.awt.BorderLayout;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringWriter;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.xml.bind.JAXBException;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import tac.StartTAC;
import tac.gui.components.LineNumberedPaper;
import tac.gui.mapview.PreviewMap;
import tac.mapsources.BeanShellMapSource;
import tac.mapsources.impl.Google;
import tac.mapsources.impl.OsmMapSources;
import tac.program.DirectoryManager;
import tac.program.Logging;
import tac.program.model.Settings;
import tac.tilestore.TileStore;
import tac.utilities.TACExceptionHandler;
import tac.utilities.Utilities;
import bsh.EvalError;

public class MapEvaluator extends JFrame {

	protected Logger log;
	private final PreviewMap previewMap;
	private final LineNumberedPaper mapSourceEditor;

	public MapEvaluator() throws HeadlessException {
		super("TAC Map Evaluator v0.1 alpha 1");
		log = Logger.getLogger(this.getClass());
		setLayout(new BorderLayout());
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		previewMap = new PreviewMap();
		mapSourceEditor = new LineNumberedPaper(10, 140);
		try {
			String code = Utilities.loadTextResource("bsh/default.bsh");
			mapSourceEditor.setText(code);
		} catch (IOException e) {
			log.error("", e);
		}
		add(previewMap, BorderLayout.CENTER);
		JPanel bottomPanel = new JPanel(new BorderLayout());
		JToolBar toolBar = new JToolBar("Toolbar");
		addButtons(toolBar);

		bottomPanel.add(toolBar, BorderLayout.NORTH);
		bottomPanel.add(mapSourceEditor, BorderLayout.CENTER);
		add(bottomPanel, BorderLayout.SOUTH);
		setExtendedState(JFrame.MAXIMIZED_BOTH);
		setSize(600, 800);
	}

	private void addButtons(JToolBar toolBar) {
		JButton button = null;

		button = new JButton("Reset", Utilities.loadResourceImageIcon("new-icon.png"));
		button.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				try {
					String[] options = { "Empty", "OpenStreetMap Mapnik", "Yahoo", "Microsoft Maps" };
					int a = JOptionPane.showOptionDialog(MapEvaluator.this,
							"Please select an template", "Select template", 0,
							JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
					String code = "";
					switch (a) {
					case (0):
						code = Utilities.loadTextResource("bsh/empty.bsh");
						break;
					case (1):
						code = Utilities.loadTextResource("bsh/osm.bsh");
						break;
					case (2):
						code = Utilities.loadTextResource("bsh/yahoo.bsh");
						break;
					case (3):
						code = Utilities.loadTextResource("bsh/bing.bsh");
						break;
					}

					mapSourceEditor.setText(code);
				} catch (IOException e) {
					log.error("", e);
				}
			}
		});
		toolBar.add(button);

		button = new JButton("Load", Utilities.loadResourceImageIcon("open-icon.png"));
		button.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				try {
					BufferedReader br = new BufferedReader(new InputStreamReader(
							new FileInputStream("mapsource.bsh")));
					StringWriter sw = new StringWriter();
					String line = br.readLine();
					while (line != null) {
						sw.write(line + "\n");
						line = br.readLine();
					}
					br.close();
					mapSourceEditor.setText(sw.toString());
				} catch (IOException e) {
					log.error("", e);
					JOptionPane.showMessageDialog(MapEvaluator.this,
							"Error reading code from file:\n" + e.getMessage(), "Loading failed",
							JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		toolBar.add(button);

		button = new JButton("Save", Utilities.loadResourceImageIcon("save-icon.png"));
		button.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				try {
					BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
							new FileOutputStream("mapsource.bsh")));
					bw.write(mapSourceEditor.getText());
					bw.close();
				} catch (IOException e) {
					log.error("", e);
					JOptionPane.showMessageDialog(MapEvaluator.this,
							"Error writing code to disk:\n" + e.getMessage(), "Saving failed",
							JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		toolBar.add(button);

		button = new JButton("Execute code", Utilities.loadResourceImageIcon("check-icon.png"));
		button.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				exec();
			}
		});
		toolBar.add(button);

		button = new JButton("Google Maps", Utilities.loadResourceImageIcon("google-icon.png"));
		button.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				previewMap.setMapSource(new Google.GoogleMaps());
			}
		});
		toolBar.add(button);

		button = new JButton("OSM", Utilities.loadResourceImageIcon("osm-icon.png"));
		button.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				previewMap.setMapSource(new OsmMapSources.Mapnik());
			}
		});
		toolBar.add(button);
	}

	private void exec() {
		try {
			BeanShellMapSource testMapSource = new BeanShellMapSource(mapSourceEditor.getText());
			if (testMapSource.getTileUrlConnection(0, 0, 0) != null) {
				previewMap.setMapSource(testMapSource);
				return;
			}
			JOptionPane.showMessageDialog(this, "Error in custom code: result is null",
					"Error in custom code", JOptionPane.ERROR_MESSAGE);
		} catch (Exception e) {
			log.error("", e);
			if (e.getCause() instanceof EvalError) {
				Throwable cause = e.getCause();
				JOptionPane.showMessageDialog(this,
						"Error in custom code: \n" + cause.getMessage(), "Error in custom code",
						JOptionPane.ERROR_MESSAGE);
			} else {
				TACExceptionHandler.processException(e);
			}
		}
	}

	public static void main(String[] args) {
		StartTAC.setLookAndFeel();
		Logging.configureConsoleLogging(Level.TRACE, Logging.ADVANCED_LAYOUT);
		DirectoryManager.initialize();
		try {
			Settings.load();
			TileStore.initialize();
			new MapEvaluator().setVisible(true);
		} catch (JAXBException e) {
			JOptionPane.showMessageDialog(null, "Error", e.getLocalizedMessage(),
					JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		}
	}
}
