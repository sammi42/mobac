package tac.utilities;

import java.awt.event.ActionEvent;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.text.ParsePosition;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JOptionPane;

import org.apache.log4j.Logger;

import tac.Main;
import tac.mapsources.impl.Google;
import tac.program.model.Atlas;
import tac.program.model.EastNorthCoordinate;
import tac.program.model.Layer;
import tac.program.model.Profile;
import tac.program.model.Settings;

public class Utilities {

	public static final DecimalFormatSymbols DFS_ENG = new DecimalFormatSymbols(Locale.ENGLISH);
	public static final DecimalFormat FORMAT_6_DEC = new DecimalFormat("#0.000000");
	public static final DecimalFormat FORMAT_6_DEC_ENG = new DecimalFormat("#0.000000", DFS_ENG);
	public static final DecimalFormat FORMAT_2_DEC = new DecimalFormat("0.00");
	private static final DecimalFormat cDmsMinuteFormatter = new DecimalFormat("00");
	private static final DecimalFormat cDmsSecondFormatter = new DecimalFormat("00.0");

	private static final Logger log = Logger.getLogger(Utilities.class);

	public static String fmt(String format, int value) {
		return String.format(format, new Object[] { value });
	}

	public static boolean testJaiColorQuantizerAvailable() {
		try {
			Class<?> c = Class.forName("javax.media.jai.operator.ColorQuantizerDescriptor");
			if (c != null)
				return true;
		} catch (Exception e) {
			return false;
		} catch (NoClassDefFoundError e) {
			return false;
		}
		return true;
	}

	/**
	 * 
	 * @param imageName
	 *            imagePath resource path relative to the class {@link Main}
	 * @return
	 */
	public static ImageIcon loadResourceImageIcon(String imageName) {
		URL url = Main.class.getResource("resources/images/" + imageName);
		return new ImageIcon(url);
	}

	public static URL getResourceImageUrl(String imageName) {
		return Main.class.getResource("resources/images/" + imageName);
	}

	public static void loadProperties(Properties p, URL url) throws IOException {
		InputStream propIn = url.openStream();
		try {
			p.load(propIn);
		} finally {
			closeStream(propIn);
		}
	}

	public static void loadProperties(Properties p, File f) throws IOException {
		InputStream propIn = new FileInputStream(f);
		try {
			p.load(propIn);
		} finally {
			closeStream(propIn);
		}
	}

	/**
	 * Checks if the current {@link Thread} has been interrupted and if so a
	 * {@link InterruptedException}. Therefore it behaves similar to
	 * {@link Thread#sleep(long)} without actually slowing down anything by
	 * sleeping a certain amount of time.
	 * 
	 * @throws InterruptedException
	 */
	public static void checkForInterruption() throws InterruptedException {
		if (Thread.currentThread().isInterrupted())
			throw new InterruptedException();
	}

	/**
	 * Checks if the current {@link Thread} has been interrupted and if so a
	 * {@link RuntimeException} will be thrown. This method is useful for long
	 * lasting operations that do not allow to throw an
	 * {@link InterruptedException}.
	 * 
	 * @throws RuntimeException
	 */
	public static void checkForInterruptionRt() throws RuntimeException {
		if (Thread.currentThread().isInterrupted())
			throw new RuntimeException(new InterruptedException());
	}

	public static void closeStream(InputStream in) {
		try {
			in.close();
		} catch (Exception e) {
		}
	}

	public static void closeStream(OutputStream out) {
		try {
			out.close();
		} catch (Exception e) {
		}
	}

	public static void closeWriter(Writer writer) {
		try {
			writer.close();
		} catch (Exception e) {
		}
	}

	public static void closeReader(OutputStream reader) {
		try {
			reader.close();
		} catch (Exception e) {
		}
	}

	public static double parseLocaleDouble(String text) throws ParseException {
		ParsePosition pos = new ParsePosition(0);
		Number n = Utilities.FORMAT_6_DEC.parse(text, pos);
		if (n == null)
			throw new ParseException("Unknown error", 0);
		if (pos.getIndex() != text.length())
			throw new ParseException("Text ends with unparsable characters", pos.getIndex());
		return n.doubleValue();
	}

	public static void showTooltipNow(JComponent c) {
		Action toolTipAction = c.getActionMap().get("postTip");
		if (toolTipAction != null) {
			ActionEvent postTip = new ActionEvent(c, ActionEvent.ACTION_PERFORMED, "");
			toolTipAction.actionPerformed(postTip);
		}
	}

	/**
	 * Formats a byte value depending on the size to "Bytes", "KiBytes",
	 * "MiByte" and "GiByte"
	 * 
	 * @param bytes
	 * @return Formatted {@link String}
	 */
	public static String formatBytes(long bytes) {
		if (bytes < 1000)
			return Long.toString(bytes) + " Bytes";
		if (bytes < 1000000)
			return FORMAT_2_DEC.format(bytes / 1024d) + " KiByte";
		if (bytes < 1000000000)
			return FORMAT_2_DEC.format(bytes / 1048576d) + " MiByte";
		return FORMAT_2_DEC.format(bytes / 1073741824d) + " GiByte";
	}

	public static void fileCopy(File sourceFile, File destFile) throws IOException {

		FileChannel source = null;
		FileChannel destination = null;
		FileInputStream fis = null;
		FileOutputStream fos = null;

		try {
			fis = new FileInputStream(sourceFile);
			fos = new FileOutputStream(destFile);

			source = fis.getChannel();
			destination = fos.getChannel();

			destination.transferFrom(source, 0, source.size());
		} finally {
			fis.close();
			fos.close();

			if (source != null) {
				source.close();
			}
			if (destination != null) {
				destination.close();
			}
		}
	}

	public static boolean deleteDirectory(File path) {

		if (path.exists()) {
			File[] files = path.listFiles();

			for (int i = 0; i < files.length; i++) {
				if (files[i].isDirectory()) {
					deleteDirectory(files[i]);
				} else {
					files[i].delete();
				}
			}
		}
		return (path.delete());
	}

	public static byte[] getFileBytes(File file) throws IOException {
		int size = (int) file.length();
		byte[] buffer = new byte[size];
		DataInputStream in = new DataInputStream(new FileInputStream(file));
		try {
			in.readFully(buffer);
			return buffer;
		} finally {
			closeStream(in);
		}
	}

	/**
	 * Fully reads data from <tt>in</tt> to an internal buffer until the end of
	 * in has been reached. Then the buffer is returned.
	 * 
	 * @param in
	 *            data source to be read
	 * @return buffer all data available in in
	 * @throws IOException
	 */
	public static byte[] getInputBytes(InputStream in) throws IOException {
		ByteArrayOutputStream buffer = new ByteArrayOutputStream(in.available());
		byte[] b = new byte[1024];
		int read = 0;
		read = in.read(b);
		while (read >= 0) {
			buffer.write(b, 0, read);
			read = in.read(b);
		}
		return buffer.toByteArray();
	}

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
				atlas.addLayer(new Layer(atlas, "GM New York 16", new Google.GoogleMaps(), max,
						min, 16, null, 32000));
				atlas.addLayer(new Layer(atlas, "GM New York 14", new Google.GoogleMaps(), max,
						min, 14, null, 32000));
				p.save(atlas);
			} catch (Exception e) {
				log.error("", e);
			}
		}
		// defaultProfiles.addElement(createExampleProfile("Outdooractive Berlin"
		// ,
		// "Outdooractive.com", 53.079178, 52.020388, 14.276733, 12.356873,
		// new boolean[] { false, false, true, false, true, false, true, false,
		// true,
		// false }, 256, 256, "oa berlin"));
		// defaultProfiles.addElement(createExampleProfile("Openstreetmap Bavaria"
		// , "Mapnik",
		// 50.611132, 47.189712, 13.996582, 8.811035, new boolean[] { false,
		// false,
		// false, false, false, false, false, false, false, false, true,
		// false, true, false, true, false, true, false, true }, 256, 256,
		// "osm bavaria"));
	}

	/**
	 * Lists all direct sub directories of <code>dir</code>
	 * 
	 * @param dir
	 * @return list of directories
	 */
	public static File[] listSubDirectories(File dir) {
		return dir.listFiles(new DirectoryFilter());
	}

	public static List<File> listSubDirectoriesRec(File dir, int maxDepth) {
		List<File> dirList = new LinkedList<File>();
		addSubDirectories(dirList, dir, maxDepth);
		return dirList;
	}

	public static void addSubDirectories(List<File> dirList, File dir, int maxDepth) {
		File[] subDirs = dir.listFiles(new DirectoryFilter());
		for (File f : subDirs) {
			dirList.add(f);
			if (maxDepth > 0)
				addSubDirectories(dirList, f, maxDepth - 1);
		}
	}

	public static class DirectoryFilter implements FileFilter {

		public boolean accept(File f) {
			return f.isDirectory();
		}
	}

	public static String prettyPrintLatLon(double coord, boolean isCoordKindLat) {
		boolean neg = coord < 0.0;
		String c;
		if (isCoordKindLat) {
			c = (neg ? "S" : "N");
		} else {
			c = (neg ? "W" : "E");
		}
		double tAbsCoord = Math.abs(coord);
		int tDegree = (int) tAbsCoord;
		double tTmpMinutes = (tAbsCoord - tDegree) * 60;
		int tMinutes = (int) tTmpMinutes;
		double tSeconds = (tTmpMinutes - tMinutes) * 60;
		return c + tDegree + "\u00B0" + cDmsMinuteFormatter.format(tMinutes) + "\'"
				+ cDmsSecondFormatter.format(tSeconds) + "\"";
	}

	public static void setHttpProxyHost(String host) {
		if (host != null && host.length() > 0)
			System.setProperty("http.proxyHost", host);
		else
			System.getProperties().remove("http.proxyHost");
	}

	public static void setHttpProxyPort(String port) {
		if (port != null && port.length() > 0)
			System.setProperty("http.proxyPort", port);
		else
			System.getProperties().remove("http.proxyPort");
	}

}