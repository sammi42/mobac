/*******************************************************************************
 * Copyright (c) MOBAC developers
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package mobac.mapsources.loader;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.CodeSigner;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import mobac.exceptions.UnrecoverableDownloadException;
import mobac.mapsources.MapSourcesManager;
import mobac.program.Logging;
import mobac.program.ProgramInfo;
import mobac.program.interfaces.MapSource;
import mobac.program.model.MapSourceLoaderInfo;
import mobac.program.model.Settings;
import mobac.program.model.MapSourceLoaderInfo.LoaderType;
import mobac.utilities.Utilities;
import mobac.utilities.file.FileExtFilter;

import org.apache.commons.codec.binary.Hex;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

public class MapPackManager {

	private static final String MAP_PACK_PACKAGE = "mobac.mapsources.mappacks";

	private final Logger log = Logger.getLogger(MapPackManager.class);

	private final int requiredMapPackVersion;

	private final File mapPackDir;

	private final X509Certificate mapPackCert;

	public MapPackManager(File mapPackDir) throws CertificateException, IOException {
		this.mapPackDir = mapPackDir;
		requiredMapPackVersion = Integer.parseInt(System.getProperty("mobac.mappackversion"));
		CertificateFactory cf = CertificateFactory.getInstance("X.509");
		Collection<? extends Certificate> certs = cf.generateCertificates(Utilities
				.loadResourceAsStream("cert/MapPack.cer"));
		mapPackCert = (X509Certificate) certs.iterator().next();
	}

	/**
	 * Searches for updated map packs, verifies the signature
	 * 
	 * @throws IOException
	 */
	public void installUpdates() throws IOException {
		File[] newMapPacks = mapPackDir.listFiles(new FileExtFilter(".jar.new"));
		for (File newMapPack : newMapPacks) {
			try {
				testMapPack(newMapPack);
				String name = newMapPack.getName();
				name = name.substring(0, name.length() - 4); // remove ".new"
				File oldMapPack = new File(mapPackDir, name);
				if (oldMapPack.isFile()) {
					// TODO: Check if new map pack file is still compatible

					Utilities.deleteFile(oldMapPack);
				}
				newMapPack.renameTo(oldMapPack);
			} catch (CertificateException e) {
				newMapPack.delete();
				log.error("Map pack certificate cerificateion failed (" + newMapPack.getName()
						+ ") installation aborted and file was deleted");
			}
		}
	}

	public File[] getAllMapPackFiles() {
		return mapPackDir.listFiles(new FileExtFilter(".jar"));
	}

	public void loadMapPacks(MapSourcesManager mapSourcesManager) throws IOException, CertificateException {
		File[] mapPacks = getAllMapPackFiles();
		ClassLoader sysClassLoader = ClassLoader.getSystemClassLoader();
		for (File mapPackFile : mapPacks) {
			try {
				// testMapPack(mapPackFile);
				URL url = mapPackFile.toURI().toURL();
				URLClassLoader urlCl = new MapPackClassLoader(MAP_PACK_PACKAGE, url, sysClassLoader);
				InputStream manifestIn = urlCl.getResourceAsStream("META-INF/MANIFEST.MF");
				String rev = null;
				if (manifestIn != null) {
					Manifest mf = new Manifest(manifestIn);
					rev = mf.getMainAttributes().getValue("MapPackRevision");
					log.debug(rev);
					manifestIn.close();
				}
				MapSourceLoaderInfo loaderInfo = new MapSourceLoaderInfo(LoaderType.MAPPACK, mapPackFile, rev);
				final Iterator<MapSource> iterator = ServiceLoader.load(MapSource.class, urlCl).iterator();
				while (iterator.hasNext()) {
					try {
						MapSource ms = iterator.next();
						ms.setLoaderInfo(loaderInfo);
						mapSourcesManager.addMapSource(ms);
						log.trace("Loaded map source: " + ms.toString() + " (name: " + ms.getName() + ")");
					} catch (Error e) {
						log.error("Faild to load a map source from map pack: " + e.getMessage(), e);
					}
				}

			} catch (IOException e) {
				log.error("Failed to load map pack: " + mapPackFile, e);
			}
		}
	}

	public String downloadMD5SumList() throws IOException {
		String md5eTag = Settings.getInstance().mapSourcesUpdate.etag;
		String updateUrl = System.getProperty("mobac.updateurl");
		if (updateUrl == null)
			throw new RuntimeException("Update url not present");

		byte[] data = null;

		HttpURLConnection conn = (HttpURLConnection) new URL(updateUrl).openConnection();
		conn.setRequestProperty("If-None-Match", md5eTag);
		int responseCode = conn.getResponseCode();
		if (responseCode == HttpURLConnection.HTTP_NOT_MODIFIED)
			return null;
		if (responseCode != HttpURLConnection.HTTP_OK)
			throw new IOException("Invalid HTTP response: " + responseCode + " for url " + conn.getURL());
		// Case HTTP_OK
		InputStream in = conn.getInputStream();
		data = Utilities.getInputBytes(in);
		in.close();
		Settings.getInstance().mapSourcesUpdate.etag = conn.getHeaderField("ETag");
		String md5sumList = new String(data);
		return md5sumList;
	}

	public void cleanMapPackDir() throws IOException { // Clean up old files
		File[] newMapPacks = mapPackDir.listFiles(new FileExtFilter(".jar.new"));
		for (File newMapPack : newMapPacks)
			Utilities.deleteFile(newMapPack);
		File[] unverifiedMapPacks = mapPackDir.listFiles(new FileExtFilter(".jar.unverified"));
		for (File unverifiedMapPack : unverifiedMapPacks)
			Utilities.deleteFile(unverifiedMapPack);

	}

	/**
	 * 
	 * @return
	 * @throws IOException
	 */
	public int updateMapPacks() throws UnrecoverableDownloadException, IOException {
		String updateBaseUrl = System.getProperty("mobac.updatebaseurl");
		if (updateBaseUrl == null)
			throw new RuntimeException("Update base url not present");

		cleanMapPackDir();
		String md5sumList = downloadMD5SumList();
		if (md5sumList == null)
			return -1;
		int updateCount = 0;
		String[] outdatedMapPacks = searchForOutdatedMapPacks(md5sumList);
		for (String mapPack : outdatedMapPacks) {
			try {
				File newMapPackFile = downloadMapPack(updateBaseUrl, mapPack);
				try {
					testMapPack(newMapPackFile);
				} catch (CertificateException e) {
					// Certificate validation failed
					log.error(e.getMessage(), e);
					Utilities.deleteFile(newMapPackFile);
					continue;
				}
				log.debug("Verification of map pack \"" + mapPack + "\" passed successfully");
				String name = newMapPackFile.getName();
				name = name.replace(".unverified", ".new");
				File f = new File(newMapPackFile.getParentFile(), name);
				// Change file extension
				newMapPackFile.renameTo(f);
				updateCount++;
			} catch (IOException e) {
				log.error(e.getMessage(), e);
			}
		}
		return updateCount;
	}

	public File downloadMapPack(String baseURL, String mapPackFilename) throws IOException {
		if (!mapPackFilename.endsWith(".jar"))
			throw new IOException("Invalid map pack filename");
		byte[] mapPackData = Utilities.downloadHttpFile(baseURL + mapPackFilename);
		File newMapPackFile = new File(mapPackDir, mapPackFilename + ".unverified");
		FileOutputStream out = new FileOutputStream(newMapPackFile);
		try {
			out.write(mapPackData);
			out.flush();
		} finally {
			Utilities.closeStream(out);
		}
		log.debug("New map pack \"" + mapPackFilename + "\" successfully downloaded");
		return newMapPackFile;
	}

	/**
	 * 
	 * @param md5sumList
	 * @return Array of filenames of map packs which are outdated
	 */
	public String[] searchForOutdatedMapPacks(String md5sumList) {
		ArrayList<String> outdatedMappacks = new ArrayList<String>();
		String[] md5s = md5sumList.split("[\\n\\r]+");
		for (String line : md5s) {
			int index = line.indexOf(' ');
			String md5 = line.substring(0, index).toLowerCase();
			String filename = line.substring(index + 1);
			// Check if there is already an update map pack
			File mapPackFile = new File(mapPackDir, filename + ".new");
			if (!mapPackFile.isFile())
				mapPackFile = new File(mapPackDir, filename);
			if (!mapPackFile.isFile()) {
				outdatedMappacks.add(filename);
				log.debug("local map pack file missing: " + filename);
				continue;
			}
			try {
				String localmd5 = generateMappackMD5(mapPackFile);
				if (localmd5.equals(md5))
					continue; // No change in map pack
				log.debug("Found outdated map pack: \"" + filename + "\" local md5: " + localmd5 + " remote md5: "
						+ md5);
				outdatedMappacks.add(filename);
			} catch (Exception e) {
				log.error("Failed to generate md5sum of " + mapPackFile, e);
			}
		}
		String[] result = new String[outdatedMappacks.size()];
		outdatedMappacks.toArray(result);
		return result;
	}

	/**
	 * Calculate the md5sum on all files in the map pack file (except those in META-INF) and their filenames inclusive
	 * path in the map pack file).
	 * 
	 * @param mapPackFile
	 * @return
	 * @throws IOException
	 * @throws NoSuchAlgorithmException
	 */
	public String generateMappackMD5(File mapPackFile) throws IOException, NoSuchAlgorithmException {
		ZipFile zip = new ZipFile(mapPackFile);
		try {
			Enumeration<? extends ZipEntry> entries = zip.entries();
			MessageDigest md5Total = MessageDigest.getInstance("MD5");
			MessageDigest md5 = MessageDigest.getInstance("MD5");
			while (entries.hasMoreElements()) {
				ZipEntry entry = entries.nextElement();

				if (entry.isDirectory())
					continue;
				// Do not hash files from META-INF
				String name = entry.getName();
				if (name.toUpperCase().startsWith("META-INF"))
					continue;
				md5.reset();
				InputStream in = zip.getInputStream(entry);
				byte[] data = Utilities.getInputBytes(in);
				in.close();
				byte[] digest = md5.digest(data);
				md5Total.update(digest);
				md5Total.update(name.getBytes());
			}
			return Hex.encodeHexString(md5Total.digest());
		} finally {
			zip.close();
		}
	}

	/**
	 * Verifies the class file signatures of the specified map pack
	 * 
	 * @param mapPackFile
	 * @throws IOException
	 * @throws CertificateException
	 */
	public void testMapPack(File mapPackFile) throws IOException, CertificateException {
		String fileName = mapPackFile.getName();
		JarFile jf = new JarFile(mapPackFile, true);
		try {
			Enumeration<JarEntry> it = jf.entries();
			while (it.hasMoreElements()) {
				JarEntry entry = it.nextElement();
				// We verify only class files
				if (!entry.getName().endsWith(".class"))
					continue; // directory or other entry
				// Get the input stream (triggers) the signature verification for the specific class
				Utilities.readFully(jf.getInputStream(entry));
				if (entry.getCodeSigners() == null)
					throw new CertificateException("Unsigned class file found: " + entry.getName());
				CodeSigner signer = entry.getCodeSigners()[0];
				List<? extends Certificate> cp = signer.getSignerCertPath().getCertificates();
				if (cp.size() > 1)
					throw new CertificateException("Signature certificate not accepted: "
							+ "certificate path contains more than one certificate");
				// Compare the used certificate with the mapPack certificate
				if (!mapPackCert.equals(cp.get(0)))
					throw new CertificateException("Signature certificate not accepted: "
							+ "not the MapPack signer certificate");
			}
			Manifest mf = jf.getManifest();
			Attributes a = mf.getMainAttributes();
			String mpv = a.getValue("MapPackVersion");
			if (mpv == null)
				throw new IOException("MapPackVersion info missing!");
			int mapPackVersion = Integer.parseInt(mpv);
			if (requiredMapPackVersion != mapPackVersion)
				throw new IOException("This pack \"" + fileName + "\" is not compatible with this MOBAC version.");
			ZipEntry entry = jf.getEntry("META-INF/services/mobac.program.interfaces.MapSource");
			if (entry == null)
				throw new IOException("MapSources services list is missing in file " + fileName);
		} finally {
			jf.close();
		}

	}

	public static void main(String[] args) {
		try {
			Logging.configureConsoleLogging(Level.DEBUG);
			ProgramInfo.initialize();
			MapPackManager mpm = new MapPackManager(new File("mapsources"));
			// System.out.println(mpm.generateMappackMD5(new File("mapsources/mp-bing.jar")));
			mpm.updateMapPacks();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
