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
		if (pos.getIndex() != text.length()) {
			throw new ParseException("Text ends with unparsable characters", pos.getIndex());
		}
		return n.doubleValue();
	}

	public static void showTooltipNow(JComponent c) {
		Action toolTipAction = c.getActionMap().get("postTip");
		if (toolTipAction != null) {
			ActionEvent postTip = new ActionEvent(c, ActionEvent.ACTION_PERFORMED, "");
			toolTipAction.actionPerformed(postTip);
		}
	}

	public static int validateString(String theStringToValidate, boolean isPath) {

		int isValid = -1;

		if (!isPath) {
			if (theStringToValidate.indexOf(47) > -1) { // "/"
				isValid = 47;
			}
			if (theStringToValidate.indexOf(92) > -1) { // "\"
				isValid = 92;
			}
			if (theStringToValidate.indexOf(58) > -1) { // ":"
				isValid = 58;
			}
		}
		if (theStringToValidate.indexOf(60) > -1) { // "<"
			isValid = 60;
		}
		if (theStringToValidate.indexOf(62) > -1) { // ">"
			isValid = 62;
		}
		if (theStringToValidate.indexOf(124) > -1) { // "|"
			isValid = 124;
		}
		if (theStringToValidate.indexOf(42) > -1) { // "*"
			isValid = 42;
		}
		if (theStringToValidate.indexOf(63) > -1) { // "?"
			isValid = 63;
		}
		if (theStringToValidate.indexOf(34) > -1) { // """
			isValid = 34;
		}
		if (theStringToValidate.indexOf(44) > -1) { // ","
			isValid = 44;
		}
		if (theStringToValidate.indexOf(46) > -1) { // "."
			isValid = 46;
		}

		return isValid;
	}

	public static int validateTileSizeInput(String theTileSizeInput) {

		int notValidCharacter = -1;

		char[] chars = theTileSizeInput.toCharArray();

		for (int i = 0; i < chars.length; i++) {

			if (chars[i] < 48 || chars[i] > 57) {
				return notValidCharacter = chars[i];
			}
		}
		return notValidCharacter;
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

	// public static boolean createCR_TAR(File atlasFolder, File atlasTarFolder)
	// {
	//
	// File[] taredFolderFiles = atlasWorkTarFolder.listFiles();
	//
	// for (int i = 0; i < taredFolderFiles.length; i++) {
	//
	// if (taredFolderFiles[i].isDirectory()) {
	// deleteDirectory(taredFolderFiles[i]);
	// } else {
	// taredFolderFiles[i].delete();
	// }
	// }
	//
	// ProcessValues.clearCrTarContentVector();
	//
	// listAtlasDirectoryContent(atlasFolder);
	//
	// Vector<File> temp = new Vector<File>();
	// temp = ProcessValues.getCrTarContentVector();
	//
	// Vector<File> files = new Vector<File>();
	//
	// String fileSeparator = System.getProperty("file.separator");
	//
	// for (int i = 0; i < temp.size(); i++) {
	//
	// // If element in Vector is a file, then this will be added to the
	// // Vector with files
	// if (temp.elementAt(i).isFile()) {
	// files.addElement(temp.elementAt(i));
	// } else {
	//
	// String atlasWorkTarFolderParent = atlasWorkTarFolder.getParent();
	// String trimmedPath = temp.elementAt(i).getAbsolutePath().substring(
	// atlasWorkTarFolderParent.length(),
	// temp.elementAt(i).getAbsolutePath().length());
	//
	// trimmedPath = trimmedPath.substring(trimmedPath.indexOf(fileSeparator) +
	// 1,
	// trimmedPath.length());
	// trimmedPath = trimmedPath.substring(trimmedPath.indexOf(fileSeparator) +
	// 1,
	// trimmedPath.length());
	// trimmedPath = trimmedPath.substring(trimmedPath.indexOf(fileSeparator) +
	// 1,
	// trimmedPath.length());
	//
	// if (new File(atlasWorkTarFolder, trimmedPath).exists() != true) {
	// new File(atlasWorkTarFolder, trimmedPath).mkdirs();
	// }
	// }
	// }
	// for (int i = 0; i < files.size(); i++) {
	//
	// try {
	// String atlasWorkTarFolderParent = atlasWorkTarFolder.getParent();
	//
	// String trimmedPath = files.elementAt(i).getAbsolutePath().substring(
	// atlasWorkTarFolderParent.length(),
	// files.elementAt(i).getAbsolutePath().length());
	//
	// trimmedPath = trimmedPath.substring(trimmedPath.indexOf(fileSeparator) +
	// 1,
	// trimmedPath.length());
	// trimmedPath = trimmedPath.substring(trimmedPath.indexOf(fileSeparator) +
	// 1,
	// trimmedPath.length());
	// trimmedPath = trimmedPath.substring(trimmedPath.indexOf(fileSeparator) +
	// 1,
	// trimmedPath.length());
	//
	// if (files.elementAt(i).getName().equals("cr.tba")) {
	// fileCopy(files.elementAt(i), new File(atlasWorkTarFolder + fileSeparator
	// + files.elementAt(i).getName()));
	// }
	// if (files.elementAt(i).getName().endsWith(".map")) {
	// fileCopy(files.elementAt(i), new File(atlasWorkTarFolder + fileSeparator
	// + trimmedPath));
	// }
	// } catch (IOException iox) {
	// iox.printStackTrace();
	// return false;
	// }
	// }
	// TarArchive ta = null;
	// try {
	// ta = new TarArchive(atlasFolder, new File(atlasTarFolder, "cr.tar"));
	// ta.createCRTarArchive();
	// } catch (FileNotFoundException e) {
	// e.printStackTrace();
	// return false;
	// }
	// return true;
	// }
	//
	// public static void createTarPackedLayers(File atlasFolder, File
	// atlasTarFolder) {
	//
	// ProcessValues.clearCrTarContentVector();
	//
	// listAtlasDirectoryContent(atlasFolder);
	//
	// Vector<File> temp = new Vector<File>();
	// temp = ProcessValues.getCrTarContentVector();
	//
	// Vector<File> setFolders = new Vector<File>();
	//
	// for (int i = 0; i < temp.size(); i++) {
	//
	// if (!temp.elementAt(i).isFile()) {
	// if (temp.elementAt(i).getName().equals("set")) {
	// setFolders.addElement(temp.elementAt(i));
	// } else {
	//
	// String atlasTarFolderParent = atlasTarFolder.getParent();
	//
	// String trimmedPath = temp.elementAt(i).getAbsolutePath().substring(
	// atlasTarFolderParent.length(),
	// temp.elementAt(i).getAbsolutePath().length());
	//
	// trimmedPath = trimmedPath.substring(trimmedPath.indexOf(File.separator) +
	// 1,
	// trimmedPath.length());
	//
	// File f = new File(atlasTarFolder, trimmedPath);
	// if (f.exists() != true)
	// f.mkdirs();
	// }
	// }
	// }
	// for (int i = 0; i < setFolders.size(); i++) {
	//
	// String atlasTarFolderParent = atlasTarFolder.getParent();
	//
	// String trimmedPath =
	// (setFolders.elementAt(i).getParentFile()).getAbsolutePath()
	// .substring(atlasTarFolderParent.length(),
	// (setFolders.elementAt(i).getParentFile()).getAbsolutePath().length());
	//
	// trimmedPath = trimmedPath.substring(trimmedPath.indexOf(File.separator) +
	// 1,
	// trimmedPath.length());
	//
	// String destinationDir = atlasTarFolder + File.separator + trimmedPath +
	// File.separator;
	// File destinationFile = new File(destinationDir,
	// setFolders.elementAt(i).getParentFile()
	// .getName()
	// + ".tar");
	// File sourceFile = new File(atlasFolder, trimmedPath);
	//
	// TarArchive ta = null;
	// try {
	// ta = new TarArchive(sourceFile, destinationFile);
	// ta.createArchive();
	// } catch (FileNotFoundException e) {
	// e.printStackTrace();
	// }
	// }
	// }
	//
	// /***
	// * Method for recursively iterate through a directory and save paths to a
	// * Vector in ProcessValues class if the founded item is an directory or an
	// * file with suffix ".map"
	// **/
	// public static void listAtlasDirectoryContent(File theDirectoryToList) {
	//
	// File[] directoryContent = theDirectoryToList.listFiles();
	//
	// /***
	// * Iterate through all files in directory
	// **/
	// for (int i = 0; i < directoryContent.length; i++) {
	// /***
	// * If directory is found, add pathname to ProcessValues and do a
	// * recursive call to this method with current directory as parameter
	// **/
	// if (directoryContent[i].isDirectory()) {
	// ProcessValues.addCrTarContent(directoryContent[i]);
	// listAtlasDirectoryContent(directoryContent[i]);
	// }
	// /***
	// * If current file is cr.tba file, then add it�s path to
	// * ProcessValues
	// **/
	// if (directoryContent[i].getName().endsWith(".tba")) {
	// ProcessValues.addCrTarContent(directoryContent[i]);
	// }
	// /***
	// * If current file is an *.map file, then add it�s path to
	// * ProcessValues
	// **/
	// if (directoryContent[i].getName().endsWith(".map")) {
	// ProcessValues.addCrTarContent(directoryContent[i]);
	// }
	// }
	// }

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
				defaultProfile.setCustomTileSizeWidth(256);
				defaultProfile.setCustomTileSizeHeight(256);
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

	public static String nrOfDecimals(double theValue, int nrOfDecimals) {

		String workingString = Double.toString(theValue);

		int stringLength = workingString.length();
		int decimalPointIndex = workingString.indexOf(".");

		if (decimalPointIndex + nrOfDecimals < stringLength) {
			workingString = workingString.substring(0, decimalPointIndex + nrOfDecimals + 1);
		}
		return workingString;
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