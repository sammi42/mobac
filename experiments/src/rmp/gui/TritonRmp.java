/* *********************************************
 * Copyright: Andreas Sander
 *
 *
 * ********************************************* */

package rmp.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.util.ArrayList;
import java.util.prefs.Preferences;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.UIManager;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import rmp.gui.osm.BackgroundInfo;
import rmp.gui.osm.OsmFrontend;
import rmp.gui.tools.LikConstraint;
import rmp.gui.tools.LikLayout;
import rmp.rmpmaker.ImageProcessor;
import utilities.Logging;

public class TritonRmp extends JFrame implements WindowListener, ActionListener, BackgroundInfo {
	private static final long serialVersionUID = 1L;
	private static final Logger log = Logger.getLogger(TritonRmp.class);

	/* --- Menu item for Application-Exit --- */
	private JMenuItem menuExit;

	/* --- Button for Start of Processing --- */
	private JButton startProcessing;

	/* --- Abort Button --- */
	private JButton stopProcessing;

	/* --- Progress bar --- */
	private JProgressBar progress;

	/* --- The UserInterface currently in use --- */
	private UserInterface currentUserInterface = null;

	/* --- Class names of available user interfaces --- */
	private ArrayList<String> availableUserInterfaces;

	private ImageProcessor processor = null;

	/**
	 * Constructor
	 */
	public TritonRmp() {
		/* --- Base constructor - sets window title --- */
		super("MainWindow.Title");

		/* --- Local variables --- */
		JMenu menu;
		JMenuBar menu_bar;
		Preferences pref;
		String uiname;
		String ui_class_name = null;
		UserInterface ui;
		int i;

		setDefaultCloseOperation(EXIT_ON_CLOSE);

		/* --- Load the list of plugins --- */
		pref = Preferences.userNodeForPackage(this.getClass());

		/* --- Load list of user interfaces --- */
		availableUserInterfaces = loadUserInterfaceList();

		/* --- Choose the user interface from the settings. --- */
		uiname = pref.get("uiname", null);
		for (i = 0; i < availableUserInterfaces.size() && uiname != null && ui_class_name == null; i++) {
			if (uiname.compareTo(availableUserInterfaces.get(i)) == 0)
				ui_class_name = availableUserInterfaces.get(i);
		}

		/* --- Use the first one if none is found --- */
		if (ui_class_name == null)
			ui_class_name = availableUserInterfaces.get(0);

		/* --- Set Layout Manager --- */
		this.setLayout(new LikLayout());

		/*
		 * --- Set this object as window listener to get the window events like
		 * closing the window
		 */
		addWindowListener(this);

		/* --- Create a menu bar and add it to the window --- */
		menu_bar = new JMenuBar();
		this.setJMenuBar(menu_bar);

		/* --- Create the File menu --- */
		menu = new JMenu("MainWindow.FileMenu");
		menu_bar.add(menu);

		/* --- Exit Menu Item --- */
		menuExit = new JMenuItem("MainWindow.Exit");
		menuExit.addActionListener(this);
		menu.add(menuExit);

		/* --- Create the Plugins menu --- */
		menu = new JMenu("MainWindow.PluginMenu");
		menu_bar.add(menu);

		/* --- Create the user interface --- */
		try {
			ui = (UserInterface) Class.forName(ui_class_name).newInstance();
			createFrontend(ui);
		} catch (Exception e) {
			String msg = e.getClass().getName() + ":" + e.getMessage();
			if (msg == null)
				msg = e.getClass().getSimpleName();

			JOptionPane.showMessageDialog(this, msg, "MainWindow.GeneralError", JOptionPane.ERROR_MESSAGE);
		}

	}

	/**
	 * Creates all elements of the frontend for a given user interface
	 */
	private void createFrontend(UserInterface ui) {
		JPanel panel;
		int interface_width;
		int interface_height;
		LikLayout layout_manager;

		/* --- Store the user interface as the current one --- */
		currentUserInterface = ui;

		/* --- Get the layout manager --- */
		layout_manager = new LikLayout();

		/* --- Get Dimensions of the user interface --- */
		interface_width = (currentUserInterface.getWidth() + layout_manager.getHorizontalGap())
				/ (layout_manager.getCellWidth() + layout_manager.getHorizontalGap());
		if (((currentUserInterface.getWidth() + layout_manager.getHorizontalGap()) % (layout_manager.getCellWidth() + layout_manager
				.getHorizontalGap())) != 0)
			interface_width += 1;

		interface_height = (currentUserInterface.getHeight() + layout_manager.getVerticalGap())
				/ (layout_manager.getCellHeight() + layout_manager.getVerticalGap());
		if (((currentUserInterface.getHeight() + layout_manager.getVerticalGap()) % (layout_manager.getCellHeight() + layout_manager
				.getVerticalGap())) != 0)
			interface_height += 1;

		/* --- Set minimal dimensions --- */
		if (interface_width < 4)
			interface_width = 4;

		/* --- Panel for User Interface --- */
		panel = new JPanel();
		this.add(panel, new LikConstraint(0, 2, interface_width, interface_height));
		currentUserInterface.initialize(panel);

		/* --- Progress bar --- */
		progress = new JProgressBar();
		progress.setIndeterminate(true);
		progress.setVisible(false);
		progress.setStringPainted(true);
		this.add(progress, new LikConstraint(1, 2 + interface_height, interface_width - 1, 1));

		/* --- Execute Button --- */
		startProcessing = new JButton("MainWindow.Execute");
		startProcessing.addActionListener(this);
		this.add(startProcessing, new LikConstraint(interface_width - 3, 3 + interface_height, 3, 1));

		/* --- Abort button at the same position but initially not visible --- */
		stopProcessing = new JButton("MainWindow.Abort");
		stopProcessing.addActionListener(this);
		stopProcessing.setVisible(false);
		this.add(stopProcessing, new LikConstraint(interface_width - 3, 3 + interface_height, 3, 1));

		/* --- Fix the window to the optimal size --- */
		this.pack();
		this.setResizable(false);

	}

	/**
	 * Load list of available plugins
	 */
	private ArrayList<String> loadUserInterfaceList() {
		ArrayList<String> result;

		/* --- Fixed interfaces --- */
		result = new ArrayList<String>();
		result.add(OsmFrontend.class.getName());
		return result;
	}

	/**
	 * Event Function - Not used here
	 */
	public void windowActivated(WindowEvent arg0) {
	}

	/**
	 * Event Function
	 */
	public void windowClosed(WindowEvent arg0) {
		// Not used
	}

	/**
	 * Event Function. Called when the user wants to close the window.
	 * <P>
	 * 
	 * Closes this window and disposes all resources
	 */
	public void windowClosing(WindowEvent arg0) {
		saveSettings();
		System.exit(0);
	}

	/**
	 * Event Function - Not used here
	 */
	public void windowDeactivated(WindowEvent arg0) {
	}

	/**
	 * Event Function - Not used here
	 */
	public void windowDeiconified(WindowEvent arg0) {
	}

	/**
	 * Event Function - Not used here
	 */
	public void windowIconified(WindowEvent arg0) {
	}

	/**
	 * Event Function - Not used here
	 */
	public void windowOpened(WindowEvent arg0) {
	}

	/**
	 * Store the current settings in the system properties
	 */
	private void saveSettings() {
		Preferences pref;

		/* --- Save the name of the current user interface --- */
		pref = Preferences.userNodeForPackage(this.getClass());
		pref.put("uiname", currentUserInterface.getClass().getName());

		/* --- Release the user interface --- */
		currentUserInterface.release();
	}

	/**
	 * Event function from GUI elements
	 */
	public void actionPerformed(ActionEvent event) {
		Object source = event.getSource();

		if (source.equals(menuExit)) {
			saveSettings();
			dispose();
		} else if (source.equals(startProcessing))
			doStart();
		else if (source.equals(stopProcessing)) {
			if (processor != null)
				try {
					processor.interrupt();
				} catch (Throwable t) {
				}
		}
	}

	/**
	 * Start Processing and create the RMP file
	 */
	private void doStart() {
		log.debug("Starung processing");

		try {
			/* --- Set controls into working state --- */
			progress.setVisible(true);
			stopProcessing.setVisible(true);
			startProcessing.setVisible(false);

			/* --- Create background thread for processing the image --- */
			processor = new ImageProcessor(currentUserInterface, this, new File(currentUserInterface.getDestPath()));

			processor.start();
		} catch (Throwable e) {
			/*
			 * --- If something went wrong before starting the background
			 * thread, then the processing is automatically finished
			 */
			finished(e);
		}
	}

	/**
	 * Remove Progress bar and abort button. Show error message if there is one
	 */
	public void finished(Throwable ex) {
		processor = null;
		try {
			/* --- Bring controls into idle state --- */
			progress.setVisible(false);
			stopProcessing.setVisible(false);
			startProcessing.setVisible(true);

			/* --- Show error message if there is one --- */
			if (ex != null)
				throw ex;

		} catch (Throwable e) {
			log.error("", e);
			String msg = e.getMessage();
			if (msg == null)
				msg = e.getClass().getSimpleName();

			JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
		}
	}

	/**
	 * Show the text in the progress bar
	 */
	public void setActionText(String text) {
		progress.setString(text);
	}

	/**
	 * Starter function
	 */
	public static void main(String[] args) {
		try {
			Logging.configureConsoleLogging(Level.TRACE);
			Logger.getLogger(TritonRmp.class).info("Starting");
			/*
			 * --- Remove security manager or we get problems loading plugins
			 * ---
			 */
			System.setSecurityManager(null);

			/* --- Set the system default look and feel --- */
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

			/* --- Start the application --- */
			TritonRmp wnd = new TritonRmp();
			wnd.setVisible(true);
		} catch (Exception e) {
			System.out.println("Error creating main window: " + e.getClass().getName() + "/" + e.getMessage());
		}
	}

}
