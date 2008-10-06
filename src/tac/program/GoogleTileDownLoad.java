package tac.program;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.openstreetmap.gui.jmapviewer.interfaces.TileSource;

public class GoogleTileDownLoad {

	public static final String SECURESTRING = "Galileo";

	public static void getImage(int x, int y, int zoom, File destinationDirectory,
			TileSource tileSource, boolean isAtlasDownload) throws IOException {

		if (x == 0 || y == 0)
			return;
		TileStore ts = TileStore.getInstance();

		/**
		 * If the desired tile already exist in the persistent tilestore and
		 * settings is to use the tile store
		 */
		Settings s = Settings.getInstance();
		File destFile = new File(destinationDirectory + "/y" + y + "x" + x + ".png");

		if (s.getTileStoreEnabled()) {

			// Copy the file from the persistent tilestore instead of
			// downloading it from internet.
			try {
				if (ts.copyStoredTileTo(destFile, x, y, zoom, tileSource)) {
					return;
				}
			} catch (IOException e) {
			}
		}

		/**
		 * otherwise download it from internet
		 */
		// else {
		String url = tileSource.getTileUrl(zoom, x, y);

		System.out.println("Downloading " + url);
		URL u = new URL(url);
		// TODO XXXXXXXXXXXXXXXXXXXXXXXX
		/*
		 * HttpURLConnection huc = (HttpURLConnection) u.openConnection();
		 * 
		 * huc.setRequestMethod("GET"); huc.connect();
		 * 
		 * InputStream is = huc.getInputStream(); int code =
		 * huc.getResponseCode();
		 * 
		 * if (code == HttpURLConnection.HTTP_OK) { byte[] buffer = new
		 * byte[4096]; File output = new File(destinationDirectory +
		 * System.getProperty("file.separator") + "y" + y + "x" + x + ".png");
		 * FileOutputStream outputStream = new FileOutputStream(output);
		 * 
		 * int bytes = 0; int sumBytes = 0;
		 * 
		 * if (isAtlasDownload) {
		 * ProcessValues.setNrOfDownloadedBytes(ProcessValues
		 * .getNrOfDownloadedBytes() + (double) huc.getContentLength()); }
		 * 
		 * while ((bytes = is.read(buffer)) > 0) { sumBytes += bytes;
		 * outputStream.write(buffer, 0, bytes); } outputStream.close(); } else
		 * { System.out.println("Wrong URL to image"); } huc.disconnect(); if
		 * (s.getTileStoreEnabled()) {
		 * 
		 * String fileSeparator = System.getProperty("file.separator");
		 * 
		 * // Copy the file from the download folder to the persistent //
		 * tilestore folder File destFile = new
		 * File(System.getProperty("user.dir") + fileSeparator + "tilestore" +
		 * fileSeparator + s.getSelectedGoogleDownloadSite() + fileSeparator +
		 * theZoomValue + fileSeparator + "y" + y + "x" + x + ".png"); File
		 * sourceFile = new File(destinationDirectory +
		 * System.getProperty("file.separator") + "y" + y + "x" + x + ".png");
		 * 
		 * FileChannel source = null; FileChannel destination = null; try {
		 * source = new FileInputStream(sourceFile).getChannel(); destination =
		 * new FileOutputStream(destFile).getChannel();
		 * destination.transferFrom(source, 0, source.size()); } catch
		 * (FileNotFoundException fnfex) {
		 * System.out.println(fnfex.getMessage()); } finally { if (source !=
		 * null) { source.close(); } if (destination != null) {
		 * destination.close(); } } ts.add(theZoomValue, "y" + y + "x" + x +
		 * ".png", s .getSelectedGoogleDownloadSite()); }
		 */
		// }
	}

	private static int calculateSecureWordLength(int x, int y) {
		return ((x * 3) + y) % 8;
	}

	private static int calculateSeverNumber(int x, int y) {
		return ((2 * y) + x) % 4;
	}
}