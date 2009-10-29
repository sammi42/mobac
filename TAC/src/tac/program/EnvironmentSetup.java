package tac.program;

import java.io.File;

import javax.swing.JOptionPane;

import org.apache.log4j.Logger;

import tac.mapsources.impl.Google;
import tac.program.model.Atlas;
import tac.program.model.EastNorthCoordinate;
import tac.program.model.Layer;
import tac.program.model.Profile;
import tac.program.model.Settings;

/**
 * Creates the necessary files on first time TrekBuddy Atlas Creator is started
 * or tries to update the environment if the version has changed.
 */
public class EnvironmentSetup {

	public static Logger log = Logger.getLogger(EnvironmentSetup.class);

	public static void checkFileSetup() {

		File userDir = new File(System.getProperty("user.dir"));

		File profiles = new File(userDir, "profiles.xml");
		if (profiles.isFile()) {
			// delete old settings and profile files
			profiles.delete();
			Settings.FILE.delete();
		}

		File atlasFolder = new File(userDir, "atlases");
		atlasFolder.mkdir();

		File tileStoreFolder = new File(userDir, "tilestore");
		tileStoreFolder.mkdir();

		if (Settings.FILE.exists() == false) {

			try {
				Settings.save();
			} catch (Exception e) {
				log.error("", e);
				JOptionPane.showMessageDialog(null,
						"Could not create file settings.xml program will exit.", "Error",
						JOptionPane.ERROR_MESSAGE);
				System.exit(0);
			}
			Profile p = new Profile("Google Maps New York");
			Atlas atlas = Atlas.newInstance();
			try {
				EastNorthCoordinate max = new EastNorthCoordinate(40.97264, -74.142609);
				EastNorthCoordinate min = new EastNorthCoordinate(40.541982, -73.699036);
				Layer layer = new Layer(atlas, "GM New York");
				layer.addMapsAutocut("GM New York 16", new Google.GoogleMaps(), max, min, 16, null,
						32000);
				layer.addMapsAutocut("GM New York 14", new Google.GoogleMaps(), max, min, 14, null,
						32000);
				atlas.addLayer(layer);
				p.save(atlas);
			} catch (Exception e) {
				log.error("", e);
			}
		}
	}
}
