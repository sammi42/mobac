package tac.utilities;

import java.awt.event.ActionEvent;
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
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.ParsePosition;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Vector;

import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JOptionPane;

import tac.StartTAC;
import tac.program.Profile;
import tac.program.Settings;

public class Utilities {

	public static final DecimalFormatSymbols DFS_ENG = new DecimalFormatSymbols(Locale.ENGLISH);
	public static final DecimalFormat FORMAT_6_DEC = new DecimalFormat("#0.000000");
	public static final DecimalFormat FORMAT_6_DEC_ENG = new DecimalFormat("#0.000000", DFS_ENG);
	public static final NumberFormat FORMAT_2_DEC = new DecimalFormat("0.00");

	/**
	 * 
	 * @param imagePath
	 *            imagePath resource path relative to the class {@link StartTAC}
	 * @return
	 */
	public static ImageIcon loadResourceImageIcon(String imagePath) {
		URL url = StartTAC.class.getResource("images/" + imagePath);
		return new ImageIcon(url);
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

	public static void checkFileSetup() {

		File userDir = new File(System.getProperty("user.dir"));

		File atlasFolder = new File(userDir, "atlases");
		atlasFolder.mkdir();

		File atlasTaredFolder = new File(userDir, "atlasestared");
		atlasTaredFolder.mkdir();

		File oziFolder = new File(userDir, "ozi");
		oziFolder.mkdir();

		File tileStoreFolder = new File(userDir, "tilestore");
		tileStoreFolder.mkdir();

		File settingsFile = new File(userDir, "settings.xml");

		if (settingsFile.exists() == false) {

			try {
				Settings s = Settings.getInstance();
				s.store();
			} catch (IOException iox) {
				JOptionPane.showMessageDialog(null,
						"Could not create file settings.xml program will exit.", "Error",
						JOptionPane.ERROR_MESSAGE);
				System.exit(0);
			}
		}

		File profilesFile = new File(userDir, "profiles.xml");

		if (profilesFile.exists() == false) {

			try {
				profilesFile.createNewFile();

				Vector<Profile> defaultProfiles = new Vector<Profile>();

				Profile defaultProfile = new Profile();

				defaultProfile.setProfileName("Default");
				defaultProfile.setLatitudeMax(85.000000);
				defaultProfile.setLatitudeMin(-85.000000);
				defaultProfile.setLongitudeMax(179.000000);
				defaultProfile.setLongitudeMin(-179.000000);

				boolean[] zoomValues = { false, false, false, false, false, false, false, false,
						false, true };

				defaultProfile.setZoomLevels(zoomValues);
				defaultProfile.setTileSizeWidth(256);
				defaultProfile.setTileSizeHeight(256);
				defaultProfile.setAtlasName("Default");

				defaultProfiles.addElement(defaultProfile);

				PersistentProfiles.store(defaultProfiles);
			} catch (IOException iox) {
				JOptionPane.showMessageDialog(null,
						"Could not create file profiles.xml program will exit.", "Error",
						JOptionPane.ERROR_MESSAGE);
				System.exit(0);
			}
		}
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
}