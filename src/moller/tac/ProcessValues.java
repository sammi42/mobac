package moller.tac;

import java.io.File;
import java.util.Vector;

public class ProcessValues {

	private static int nrOfBytesToDownload;
	private static double nrOfDownloadedBytes = 0;
	private static Vector<File> crTarContentVector = new Vector<File>();
	private static double previewLatMaxCoord;
	private static double previewLatMinCoord;
	private static double previewLongMaxCoord;
	private static double previewLongMinCoord;
	private static int previewLatMaxTile;
	private static int previewLatMinTile;
	private static int previewLongMaxTile;
	private static int previewLongMinTile;
	private static int previewZoomValue;
	private static double previewXResolution;
	private static double previewYResolution;
	private static int mouseXCoordinat;
	private static int mouseYCoordinat;
	private static boolean abortAtlasDownload;
	private static boolean tileSizeErrorNotified;

	/***
	 * S E T Methods
	 **/
	public static void setNrOfBytesToDownload(int theValue) {
		nrOfBytesToDownload = theValue;
	}

	public static void setNrOfDownloadedBytes(double theValue) {
		nrOfDownloadedBytes = theValue;
	}
	
	public static void setPreviewLatMaxCoord(double previewLatMaxCoord) {
		ProcessValues.previewLatMaxCoord = previewLatMaxCoord;
	}

	public static void setPreviewLatMinCoord(double previewLatMinCoord) {
		ProcessValues.previewLatMinCoord = previewLatMinCoord;
	}

	public static void setPreviewLongMaxCoord(double previewLongMaxCoord) {
		ProcessValues.previewLongMaxCoord = previewLongMaxCoord;
	}

	public static void setPreviewLongMinCoord(double previewLongMinCoord) {
		ProcessValues.previewLongMinCoord = previewLongMinCoord;
	}
	
	public static void setPreviewLatMaxTile(int previewLatMax) {
		ProcessValues.previewLatMaxTile = previewLatMax;
	}
	
	public static void setPreviewLatMinTile(int previewLatMin) {
		ProcessValues.previewLatMinTile = previewLatMin;
	}

	public static void setPreviewLongMaxTile(int previewLongMax) {
		ProcessValues.previewLongMaxTile = previewLongMax;
	}

	public static void setPreviewLongMinTile(int previewLongMin) {
		ProcessValues.previewLongMinTile = previewLongMin;
	}
	
	public static void setPreviewZoomValue(int previewZoomValue) {
		ProcessValues.previewZoomValue = previewZoomValue;
	}
	
	public static void setPreviewXResolution(double previewXResolution) {
		ProcessValues.previewXResolution = previewXResolution;
	}

	public static void setPreviewYResolution(double previewYResolution) {
		ProcessValues.previewYResolution = previewYResolution;
	}
	
	public static void setMouseXCoordinat(int mouseXCoordinat) {
		ProcessValues.mouseXCoordinat = mouseXCoordinat;
	}

	public static void setMouseYCoordinat(int mouseYCoordinat) {
		ProcessValues.mouseYCoordinat = mouseYCoordinat;
	}
	
	public static void setAbortAtlasDownload(boolean abortAtlasDownload) {
		ProcessValues.abortAtlasDownload = abortAtlasDownload;
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

	public static double getPreviewLatMaxCoord() {
		return previewLatMaxCoord;
	}

	public static double getPreviewLatMinCoord() {
		return previewLatMinCoord;
	}

	public static double getPreviewLongMaxCoord() {
		return previewLongMaxCoord;
	}

	public static double getPreviewLongMinCoord() {
		return previewLongMinCoord;
	}
	
	public static int getPreviewLatMaxTile() {
		return previewLatMaxTile;
	}

	public static int getPreviewLatMinTile() {
		return previewLatMinTile;
	}
	
	public static int getPreviewLongMaxTile() {
		return previewLongMaxTile;
	}
	
	public static int getPreviewLongMinTile() {
		return previewLongMinTile;
	}
	
	public static int getPreviewZoomValue() {
		return previewZoomValue;
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
	
	public static boolean getAbortAtlasDownload() {
		return abortAtlasDownload;
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
}