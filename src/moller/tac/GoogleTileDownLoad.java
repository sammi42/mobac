package moller.tac;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.FileChannel;

public class GoogleTileDownLoad {

	public static final String SECURESTRING = "Galileo";

	public static void getImage(int x, int y, int theZoomValue,
			File destinationDirectory, int server, boolean isAtlasDownload)
			throws IOException {

		if (x == 0 || y == 0 || theZoomValue>10)
			return;
		TileStore ts = TileStore.getInstance();

		/**
		 * If the desired tile already exist in the persistent tilestore and
		 * settings is to use the tile store
		 */
		Settings s = Settings.getInstance();

		if (s.getTileStoreEnabled()
				&& ts.contains(theZoomValue, "y" + y + "x" + x + ".png", s
						.getSelectedGoogleDownloadSite())) {
			String fileSeparator = System.getProperty("file.separator");

			// Copy the file from the persistent tilestore instead of
			// downloading it from internet.
			File sourceFile = new File(System.getProperty("user.dir")
					+ fileSeparator + "tilestore" + fileSeparator
					+ s.getSelectedGoogleDownloadSite() + fileSeparator
					+ theZoomValue + fileSeparator + "y" + y + "x" + x + ".png");
			File destFile = new File(destinationDirectory
					+ System.getProperty("file.separator") + "y" + y + "x" + x
					+ ".png");

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
				if (isAtlasDownload) {
					ProcessValues.setNrOfDownloadedBytes(ProcessValues
							.getNrOfDownloadedBytes()
							+ (double) source.size());
				}
			} catch (FileNotFoundException fnfex) {
				System.out.println(fnfex.getMessage());
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
		/**
		 * otherwise download it from internet
		 */
		else {

			String googleDownloadSite = s.getSelectedGoogleDownloadSite();

			String url = "";

			int staticEndDotCom = 0;
			staticEndDotCom = s.getMapsGoogleCom().indexOf("&x");

			int staticDituDotCom = 0;
			staticDituDotCom = s.getDituGoogleCom().indexOf("&x");

			int index = calculateSecureWordLength(x, y);
			int serverC = calculateSeverNumber(x, y);

			String secureSuffix = SECURESTRING.substring(0, index);

			if (googleDownloadSite.equals("maps.google.com")) {
				url = "http://mt" + serverC
						+ s.getMapsGoogleCom().substring(10, staticEndDotCom)
						+ "&x=" + x + "&y=" + y + "&z=" + theZoomValue + "&s="
						+ secureSuffix;
			}

			else if (googleDownloadSite.equals("ditu.google.com")) {
				url = "http://mt" + serverC
						+ s.getDituGoogleCom().substring(10, staticDituDotCom)
						+ "&x=" + x + "&y=" + y + "&z=" + theZoomValue + "&s="
						+ secureSuffix;
			}

			System.out.println("Downloading " + url);
			URL u = new URL(url);
			//TODO Remove the following line
			/*
			HttpURLConnection huc = (HttpURLConnection) u.openConnection();

			huc.setRequestMethod("GET");
			huc.connect();

			InputStream is = huc.getInputStream();
			int code = huc.getResponseCode();

			if (code == HttpURLConnection.HTTP_OK) {
				byte[] buffer = new byte[4096];
				File output = new File(destinationDirectory
						+ System.getProperty("file.separator") + "y" + y + "x"
						+ x + ".png");
				FileOutputStream outputStream = new FileOutputStream(output);

				int bytes = 0;
				int sumBytes = 0;

				if (isAtlasDownload) {
					ProcessValues.setNrOfDownloadedBytes(ProcessValues
							.getNrOfDownloadedBytes()
							+ (double) huc.getContentLength());
				}

				while ((bytes = is.read(buffer)) > 0) {
					sumBytes += bytes;
					outputStream.write(buffer, 0, bytes);
				}
				outputStream.close();
			} else {
				System.out.println("Wrong URL to image");
			}
			huc.disconnect();
			if (s.getTileStoreEnabled()) {

				String fileSeparator = System.getProperty("file.separator");

				// Copy the file from the download folder to the persistent
				// tilestore folder
				File destFile = new File(System.getProperty("user.dir")
						+ fileSeparator + "tilestore" + fileSeparator
						+ s.getSelectedGoogleDownloadSite() + fileSeparator
						+ theZoomValue + fileSeparator + "y" + y + "x" + x
						+ ".png");
				File sourceFile = new File(destinationDirectory
						+ System.getProperty("file.separator") + "y" + y + "x"
						+ x + ".png");

				FileChannel source = null;
				FileChannel destination = null;
				try {
					source = new FileInputStream(sourceFile).getChannel();
					destination = new FileOutputStream(destFile).getChannel();
					destination.transferFrom(source, 0, source.size());
				} catch (FileNotFoundException fnfex) {
					System.out.println(fnfex.getMessage());
				} finally {
					if (source != null) {
						source.close();
					}
					if (destination != null) {
						destination.close();
					}
				}
				ts.add(theZoomValue, "y" + y + "x" + x + ".png", s
						.getSelectedGoogleDownloadSite());
			}
			*/
		}
	}

	private static int calculateSecureWordLength(int x, int y) {
		return ((x * 3) + y) % 8;
	}

	private static int calculateSeverNumber(int x, int y) {
		return ((2 * y) + x) % 4;
	}
}