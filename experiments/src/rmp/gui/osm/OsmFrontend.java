/* ------------------------------------------------------------------------

   OsmFrontend.java

   Project: JpgToRmp

  --------------------------------------------------------------------------*/

/* ---
 created: 05.07.2009 a.sander

 $History:$
 --- */

package rmp.gui.osm;

import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.prefs.Preferences;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import rmp.gui.UserInterface;
import rmp.gui.tools.LikConstraint;
import rmp.gui.tools.LikLayout;
import rmp.rmpfile.MultiImage;
import rmp.rmpmaker.CalibratedImage;
import rmp.rmpmaker.CalibratedImage2;
import rmp.rmpmaker.OsmTile;

/**
 * JptToRMP Plugin that creates maps from the OSM online resource
 * <P>
 * 
 * For more information about the OSM online tile resource see
 * http://wiki.openstreetmap.org/wiki/Slippy_map_tilenames
 * 
 */
public class OsmFrontend implements UserInterface, ChangeListener, ActionListener {

	private String[][] services = { { "Localhost", "http://localhost/", "0", "15" },
			{ "OSM Mapnik", "http://tile.openstreetmap.org/", "0", "15" },
			{ "OSM Osmarender/Tiles@Home:", "http://tah.openstreetmap.org/Tiles/tile/", "0", "15" },
			{ "OSM Cycle Map", "http://andy.sandbox.cloudmade.com/tiles/cycle/", "0", "15" } };

	JComboBox serviceBox;
	JTextField rmpName;
	JOsmBrowser browser;
	JSlider zoom;
	JCheckBox[] selectedLevels;

	public OsmFrontend() {
		selectedLevels = new JCheckBox[16];
	}

	public String checkPlausible() {
		String err = null;
		boolean output_selected = false;
		int i;

		/* --- The only thing to check is, that there is a filename entered --- */
		if (rmpName.getText().length() == 0)
			err = "noName";

		/* --- Check that at least one output level is selected --- */
		for (i = 0; i < selectedLevels.length && !output_selected; i++)
			output_selected = selectedLevels[i].isSelected();

		if (err == null && !output_selected)
			err = "noOutputLevel";

		return err;
	}

	public String getDestPath() {
		return rmpName.getText();
	}

	public int getHeight() {
		// 19 lines and 18 gap
		return 19 * 23 + 18 * 3;
	}

	public int getWidth() {
		// 10 columns and 9 gaps
		return 10 * 85 + 9 * 3;
	}

	public CalibratedImage[] getImageCreator() throws Exception {
		ArrayList<MultiImage> layers;
		MultiImage image;
		MultiImage[] result;

		/*
		 * --- Each layer is a multi image. So create an array that is big
		 * enough to hold all layers ---
		 */
		layers = new ArrayList<MultiImage>();

		/* --- Create the MultiImage for all layers --- */
		for (int i = 0; i < selectedLevels.length; i++) {
			if (selectedLevels[i].isSelected()) {
				/* --- Create MultiImage for layer --- */
				image = new MultiImage(createCalibratedImages(i));

				/*
				 * --- The size for each tile is only 256x256 so it is save to
				 * store 64 tiles per layer ---
				 */
				image.setActiveImageMax(64);

				/* --- Add it to the layer list --- */
				layers.add(image);
			}
		}

		/* --- Convert list into an array --- */
		result = new MultiImage[layers.size()];
		result = layers.toArray(result);

		return result;
	}

	/**
	 * Create an array of calibrated images. On item per OSM-tile
	 * 
	 * @param level
	 *            zoom level to create tiles for
	 * @return array of tiles
	 */
	private CalibratedImage2[] createCalibratedImages(int level) {
		ArrayList<CalibratedImage2> images;
		CalibratedImage2[] result;
		Rectangle map;
		int min_x, min_y, max_x, max_y;
		CalibratedImage2 next_image;

		/* --- Create array for images --- */
		images = new ArrayList<CalibratedImage2>(100);

		/*
		 * --- Get the coordinates of the visible map in tiles of the current
		 * zoom level ---
		 */
		map = browser.getTileRect();

		/*
		 * --- The tile number of the upper left corner is the x/y tile of the
		 * browser window ---
		 */
		min_x = map.x;
		min_y = map.y;

		/* --- Same for the lower right corner --- */
		max_x = min_x + map.width;
		max_y = min_y + map.height;

		/* --- Calculate these values for the level to create an output for --- */
		min_x *= (int) Math.pow(2, level - browser.getZoomLevel());
		min_y *= (int) Math.pow(2, level - browser.getZoomLevel());
		max_x = (max_x + 1) * (int) Math.pow(2, level - browser.getZoomLevel()) - 1;
		max_y = (max_y + 1) * (int) Math.pow(2, level - browser.getZoomLevel()) - 1;

		/* --- Add one line above and below to get a square --- */
		min_y--;
		max_y++;

		/* --- Iterate over all tiles that match the map --- */
		for (int x = min_x; x <= max_x; x++) {
			for (int y = min_y; y <= max_y; y++) {
				/* --- Create next image --- */
				next_image = new OsmTile(x, y, level, browser.getHost());

				/* --- Add the image to the ones to export --- */
				images.add(next_image);
			}
		}

		/* --- Convert object into a traditional array --- */
		result = new CalibratedImage2[images.size()];
		result = images.toArray(result);

		return result;
	}

	public String getName() {
		return "name";
	}

	public void initialize(JComponent parent) {
		LikLayout layout;
		JLabel lab;
		int i;
		Preferences prefs;

		/* --- Get access to the stored settings --- */
		prefs = Preferences.userNodeForPackage(this.getClass());

		/* --- Set up layout manager --- */
		layout = new LikLayout();
		layout.setLeftBorder(0);
		layout.setTopBorder(0);
		parent.setLayout(layout);

		serviceBox = new JComboBox();
		for (i = 0; i < services.length; i++)
			serviceBox.addItem(services[i][0]);
		parent.add(serviceBox, new LikConstraint(1, 0, 4, 1));

		serviceBox.setSelectedIndex(prefs.getInt("service", 0));
		serviceBox.addActionListener(this);

		rmpName = new JTextField();
		rmpName.setText(prefs.get("rmpName", "osm"));
		parent.add(rmpName, new LikConstraint(1, 1, 4, 1));

		/* --- The OSM browser --- */
		browser = new JOsmBrowser();
		browser.setHost(services[serviceBox.getSelectedIndex()][1]);
		parent.add(browser, new LikConstraint(1, 2, 8, 16));

		browser.setZoomLevel(prefs.getInt("zoom", 4));
		browser.setOriginX(prefs.getInt("x", 6));
		browser.setOriginY(prefs.getInt("y", 4));
		browser.setTileOffsetX(prefs.getInt("tilex", 212));
		browser.setTileOffsetY(prefs.getInt("tiley", 199));

		zoom = new JSlider(SwingConstants.HORIZONTAL);
		zoom.setMinorTickSpacing(1);
		zoom.setMajorTickSpacing(1);
		zoom.setPaintTicks(true);
		zoom.setSnapToTicks(true);
		parent.add(zoom, new LikConstraint(6, 1, 3, 1));
		adaptSlider();
		zoom.addChangeListener(this);

		JPanel panel = new JPanel();
		for (i = 0; i < selectedLevels.length; i++) {
			selectedLevels[i] = new JCheckBox(String.format("%d", i));
			selectedLevels[i].setSelected(prefs.getBoolean("outputLevel" + i, true));
			panel.add(selectedLevels[i]);
		}
		parent.add(panel, new LikConstraint(2, 18, 7, 1));
		adaptLevelBox();
	}

	public void release() {
		Preferences prefs;
		int i;

		/* --- Stop Background processing of the OSM browser --- */
		if (browser != null)
			browser.stop();

		/* --- Get access to the preferences --- */
		prefs = Preferences.userNodeForPackage(this.getClass());

		/* --- Store the current settings --- */
		if (serviceBox != null)
			prefs.putInt("service", serviceBox.getSelectedIndex());

		if (rmpName != null)
			prefs.put("rmpName", rmpName.getText());

		if (browser != null) {
			prefs.putInt("zoom", browser.getZoomLevel());
			prefs.putInt("x", browser.getOriginX());
			prefs.putInt("y", browser.getOriginY());
			prefs.putInt("tilex", browser.getTileOffsetX());
			prefs.putInt("tiley", browser.getTileOffsetY());
		}

		for (i = 0; i < selectedLevels.length; i++)
			prefs.putBoolean("outputLevel" + i, selectedLevels[i].isSelected());

		/* --- Make sure the preferences are stored --- */
		try {
			prefs.flush();
		} catch (Exception e) {
			// Nothing
		}

	}

	/**
	 * Adapt the slider settings from the settings of the service box
	 */
	private void adaptSlider() {
		int service;

		/* --- Get the number of the current service --- */
		service = serviceBox.getSelectedIndex();

		/* --- Set the slider --- */
		zoom.setMinimum(Integer.parseInt(services[service][2]));
		zoom.setMaximum(Integer.parseInt(services[service][3]));
		zoom.setValue(browser.getZoomLevel());
	}

	/*
	 * --- Set the number of possible into the level box.
	 */
	private void adaptLevelBox() {
		int i;

		/*
		 * --- There is a limit, that a level must not exceed 18.000 pixels.
		 * Since the current level as many pixels as the size of the control
		 * (roughly) and the number of pixels doubles on each level, we can
		 * calculate the number of levels. The size of the control is 845 pixel.
		 * So the table is: 1 level: 1690 2 level: 3380 3 level: 6760 4 level:
		 * 13520 5 level: 27040
		 * 
		 * So the current level is allowed and up to four addition levels
		 */

		for (i = 0; i < selectedLevels.length; i++) {
			if (i < zoom.getValue() || i > zoom.getValue() + 4) {
				selectedLevels[i].setSelected(false);
				selectedLevels[i].setEnabled(false);
			} else
				selectedLevels[i].setEnabled(true);
		}
	}

	public void actionPerformed(ActionEvent e) {
		/*
		 * --- If the Service box has changed, then inform the osm browser about
		 * the changed service ---
		 */
		if (e.getSource().equals(serviceBox)) {
			browser.setHost(services[serviceBox.getSelectedIndex()][1]);
			adaptSlider();
		}
	}

	public void stateChanged(ChangeEvent e) {
		/* --- if the zoom level changed, then inform the osm browser --- */
		if (e.getSource().equals(zoom)) {
			browser.setZoomLevel(zoom.getValue());

			/* --- Also adapt the level box --- */
			adaptLevelBox();
		}

	}

}
