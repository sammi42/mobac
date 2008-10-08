package tac.program;

import java.io.File;
import java.util.Vector;

public class ProcessValues {

	private static int nrOfBytesToDownload;
	private static double nrOfDownloadedBytes = 0;
	private static Vector<File> crTarContentVector = new Vector<File>();
	private static double previewXResolution;
	private static double previewYResolution;
	private static int mouseXCoordinat;
	private static int mouseYCoordinat;
	private static boolean tileSizeErrorNotified;
	private static boolean abortAtlasDownload; 


	/***
	 * S E T Methods
	 **/

	public static void setAbortAtlasDownload(boolean abortAtlasDownload) {
		ProcessValues.abortAtlasDownload = abortAtlasDownload;
	}

	public static void setNrOfBytesToDownload(int theValue) {
		nrOfBytesToDownload = theValue;
	}

	public static void setNrOfDownloadedBytes(double theValue) {
		nrOfDownloadedBytes = theValue;
	}

	public static void setTileSizeErrorNotified(boolean tileSizeErrorNotified) {
		ProcessValues.tileSizeErrorNotified = tileSizeErrorNotified;
	}

	/***
	 * G E T Methods
	 **/
	public static int getNrOfBytesToDownload() {
		return nrOfBytesToDownload;
	}

	public static double getNrOfDownloadedBytes() {
		return nrOfDownloadedBytes;
	}

	public static Vector<File> getCrTarContentVector() {
		return crTarContentVector;
	}

	public static double getPreviewXResolution() {
		return previewXResolution;
	}

	public static double getPreviewYResolution() {
		return previewYResolution;
	}

	public static int getMouseXCoordinat() {
		return mouseXCoordinat;
	}

	public static int getMouseYCoordinat() {
		return mouseYCoordinat;
	}

	public static boolean getTileSizeErrorNotified() {
		return tileSizeErrorNotified;
	}

	/***
	 * M I S C Methods
	 **/
	public static void clearCrTarContentVector() {
		crTarContentVector.removeAllElements();
	}

	public static void addCrTarContent(File content) {
		crTarContentVector.addElement(content);
	}

	public static void resetNrOfDownloadedBytes() {
		nrOfDownloadedBytes = 0;
	}

	public static boolean getAbortAtlasDownload() {
		return ProcessValues.abortAtlasDownload;
	}
}