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
import java.io.IOException;
import java.net.URL;
import java.security.CodeSigner;
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

import mobac.mapsources.MapSourcesManager;
import mobac.program.interfaces.MapSource;
import mobac.program.model.MapSourceLoaderInfo;
import mobac.program.model.MapSourceLoaderInfo.LoaderType;
import mobac.utilities.Utilities;
import mobac.utilities.file.FileExtFilter;

import org.apache.log4j.Logger;

public class MapPackManager {

	private static final String MAP_PACK_PACKAGE = "mobac.mapsources.mappacks";

	private final Logger log = Logger.getLogger(MapPackManager.class);

	private final int requiredMapPackVersion;

	private final MapSourcesManager mapSourcesManager;

	private final File mapPackDir;

	private final X509Certificate mapPackCert;

	public MapPackManager(MapSourcesManager mapSourceManager, File mapPackDir) throws CertificateException, IOException {
		this.mapSourcesManager = mapSourceManager;
		this.mapPackDir = mapPackDir;
		requiredMapPackVersion = Integer.parseInt(System.getProperty("mobac.mappackversion"));
		CertificateFactory cf = CertificateFactory.getInstance("X.509");
		Collection<? extends Certificate> certs = cf.generateCertificates(Utilities
				.loadResourceAsStream("cert/MapPack.cer"));
		mapPackCert = (X509Certificate) certs.iterator().next();
	}

	public void installUpdates() throws IOException {
		File[] newMapPacks = mapPackDir.listFiles(new FileExtFilter(".jar.new"));
		for (File newMapPack : newMapPacks) {
			String name = newMapPack.getName();
			name = name.substring(0, name.length() - 4); // remove ".new"
			File oldMapPack = new File(mapPackDir, name);
			if (oldMapPack.isFile()) {
				// TODO: Check if new map pack file is still compatible

				Utilities.deleteFile(oldMapPack);
			}
			newMapPack.renameTo(oldMapPack);
		}
	}

	public void loadMapPacks() throws IOException, CertificateException {
		File[] mapPacks = mapPackDir.listFiles(new FileExtFilter(".jar"));
		ArrayList<URL> urlList = new ArrayList<URL>();
		for (File mapPackFile : mapPacks) {
			try {
				testMapPack(mapPackFile);
				URL url = mapPackFile.toURI().toURL();
				urlList.add(url);
			} catch (IOException e) {
				log.error("Failed to load map pack: " + mapPackFile, e);
			}
		}
		URL[] urls = new URL[urlList.size()];
		urlList.toArray(urls);

		ClassLoader urlCl;
		urlCl = new MapPackClassLoader(MAP_PACK_PACKAGE, urls, ClassLoader.getSystemClassLoader());

		MapSourceLoaderInfo loaderInfo = new MapSourceLoaderInfo(LoaderType.MAPPACK, null);
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

	}

	protected void testMapPack(File mapPackFile) throws IOException, CertificateException {
		String fileName = mapPackFile.getName();
		JarFile jf = new JarFile(mapPackFile, true);
		log.info("Build date: " + jf.getManifest().getAttributes("Build-Date"));
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

}
