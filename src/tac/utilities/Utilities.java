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
import java.nio.channels.FileChannel;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.text.ParsePosition;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Vector;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JOptionPane;

import tac.program.Profile;
import tac.program.Settings;

public class Utilities {

	public static final int COORD_KIND_LATTITUDE = 1;
	public static final int COORD_KIND_LONGITUDE = 2;
	private static final DecimalFormatSymbols DFS_ENG = new DecimalFormatSymbols(Locale.ENGLISH);
	public static final DecimalFormat FORMAT_6_DEC = new DecimalFormat("#0.000000");
	public static final DecimalFormat FORMAT_6_DEC_ENG = new DecimalFormat("#0.000000", DFS_ENG);

	public static final Pattern VALID_FILENAME_PATTERN = buildDisallowedCharatersPattern();

	private static Pattern buildDisallowedCharatersPattern() {
		char[] disallowedChars = new char[] { '/', '\\', ':', '<', '>', '|', '*', '?', '\"', ',',
				'.' };
		String regex = "";
		for (int i = 0; i < disallowedChars.length; i++) {
			// We are dealing with special characters
			// better we escape each disallowed character...
			regex += "\\" + disallowedChars[i];
		}
		regex = "[" + regex + "]";
		return Pattern.compile(regex);
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

	public static int validateString(String theStringToValidate) {

		int isValid = -1;

		Matcher m = VALID_FILENAME_PATTERN.matcher(theStringToValidate);
		if (m.find()) {
			MatchResult mr = m.toMatchResult();
			char match = theStringToValidate.charAt(mr.start());
			return match;
		}

		return isValid;
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

	public static String prepareMapString(String fileName, double longitudeMin,
			double longitudeMax, double latitudeMin, double latitudeMax, int width, int height) {

		StringBuffer sbMap = new StringBuffer();

		sbMap.append("OziExplorer Map Data File Version 2.2\r\n");
		sbMap.append(fileName + "\r\n");
		sbMap.append(fileName + "\r\n");
		sbMap.append("1 ,Map Code,\r\n");
		sbMap.append("WGS 84,WGS 84,   0.0000,   0.0000,WGS 84\r\n");
		sbMap.append("Reserved 1\r\n");
		sbMap.append("Reserved 2\r\n");
		sbMap.append("Magnetic Variation,,,E\r\n");
		sbMap.append("Map Projection,Mercator,PolyCal,No," + "AutoCalOnly,No,BSBUseWPX,No\r\n");

		sbMap.append("Point01,xy,    0,    0,in, deg,"
				+ getDegMinFormat(latitudeMax, COORD_KIND_LATTITUDE) + ","
				+ getDegMinFormat(longitudeMin, COORD_KIND_LONGITUDE)
				+ ", grid,   ,           ,           ,N\r\n");
		sbMap.append("Point02,xy," + (width - 1) + ",0,in, deg,"
				+ getDegMinFormat(latitudeMax, COORD_KIND_LATTITUDE) + ","
				+ getDegMinFormat(longitudeMax, COORD_KIND_LONGITUDE)
				+ ", grid,   ,           ,           ,N\r\n");
		sbMap.append("Point03,xy,    0," + (height - 1) + ",in, deg,"
				+ getDegMinFormat(latitudeMin, COORD_KIND_LATTITUDE) + ","
				+ getDegMinFormat(longitudeMin, COORD_KIND_LONGITUDE)
				+ ", grid,   ,           ,           ,N\r\n");
		sbMap.append("Point04,xy," + (width - 1) + "," + (height - 1) + ",in, deg,"
				+ getDegMinFormat(latitudeMin, COORD_KIND_LATTITUDE) + ","
				+ getDegMinFormat(longitudeMax, COORD_KIND_LONGITUDE)
				+ ", grid,   ,           ,           ,N\r\n");
		String emptyPointLine = "Point%02d,xy,     ,     ,"
				+ "in, deg,    ,        ,N,    ,        ,W, "
				+ "grid,   ,           ,           ,N\r\n";
		for (int i = 5; i <= 30; i++) {
			String s = String.format(emptyPointLine, new Object[] { i });
			sbMap.append(s);
		}
		sbMap.append("Projection Setup,,,,,,,,,,\r\n");
		sbMap.append("Map Feature = MF ; Map Comment = MC     " + "These follow if they exist\r\n");
		sbMap.append("Track File = TF      These follow if they exist\r\n");
		sbMap.append("Moving Map Parameters = MM?    " + "These follow if they exist\r\n");

		sbMap.append("MM0,Yes\r\n");
		sbMap.append("MMPNUM,4\r\n");
		sbMap.append("MMPXY,1,0,0\r\n");
		sbMap.append("MMPXY,2," + (width - 1) + ",0\r\n");
		sbMap.append("MMPXY,3,0," + (height - 1) + "\r\n");
		sbMap.append("MMPXY,4," + (width - 1) + "," + (height - 1) + "\r\n");
		sbMap.append("MMPLL,1,  " + FORMAT_6_DEC_ENG.format(longitudeMin) + ","
				+ FORMAT_6_DEC_ENG.format(latitudeMax) + "\r\n");
		sbMap.append("MMPLL,2,  " + FORMAT_6_DEC_ENG.format(longitudeMax) + ","
				+ FORMAT_6_DEC_ENG.format(latitudeMax) + "\r\n");
		sbMap.append("MMPLL,3,  " + FORMAT_6_DEC_ENG.format(longitudeMin) + ","
				+ FORMAT_6_DEC_ENG.format(latitudeMin) + "\r\n");
		sbMap.append("MMPLL,4,  " + FORMAT_6_DEC_ENG.format(longitudeMax) + ","
				+ FORMAT_6_DEC_ENG.format(latitudeMin) + "\r\n");

		sbMap.append("IWH,Map Image Width/Height," + width + "," + height + "\r\n");

		return sbMap.toString();
	}

	private static String getDegMinFormat(double coord, int COORD_KIND) {

		boolean neg = coord < 0.0 ? true : false;
		int deg = (int) coord;
		double min = (coord - deg) * 60;

		StringBuffer sbOut = new StringBuffer();
		sbOut.append((int) Math.abs(deg));
		sbOut.append(",");
		sbOut.append(FORMAT_6_DEC_ENG.format(Math.abs(min)));
		sbOut.append(",");

		if (COORD_KIND == COORD_KIND_LATTITUDE) {
			sbOut.append(neg ? "S" : "N");
		} else {
			sbOut.append(neg ? "W" : "E");
		}
		return sbOut.toString();
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