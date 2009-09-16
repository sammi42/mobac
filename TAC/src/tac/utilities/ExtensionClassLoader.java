package tac.utilities;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

import org.apache.log4j.Logger;

import tac.utilities.file.RegexFileFilter;

/**
 * Allows to load JAR files at runtime including their JNI libraries.
 */
public class ExtensionClassLoader extends URLClassLoader {

	private static final Logger log = Logger.getLogger(ExtensionClassLoader.class);
	private File jarDir;

	private ExtensionClassLoader(URL[] urls, File jarDir) throws MalformedURLException {
		super(urls);
		this.jarDir = jarDir;
	}

	/**
	 * Creates an {@link ExtensionClassLoader} for loading JARs at runtime that
	 * have JNI dependencies.
	 * 
	 * @param dirList
	 *            list of directories to search in. The first directory
	 *            containing at least one jar matching the
	 *            <code>regexFilePattern</code> is taken. The other directories
	 *            are ignored.
	 * @param regexFilePattern
	 *            jar/jni regex search pattern
	 * @return The {@link ClassLoader} that is able to load classes from the
	 *         loaded JARs and the correspondant JNI libraries
	 * @throws MalformedURLException
	 * @throws FileNotFoundException
	 */
	public static ExtensionClassLoader create(File[] dirList, String regexFilePattern)
			throws MalformedURLException, FileNotFoundException {
		File[] jarFiles = null;
		File jarDir = null;
		for (File dir : dirList) {
			if (!dir.isDirectory())
				continue;
			File[] files = dir.listFiles(new RegexFileFilter(regexFilePattern));
			if (files.length > 0) {
				log.debug("Directory: \"" + dir.getAbsolutePath() + "\"");
				log.debug("Pattern: \"" + regexFilePattern + "\"");
				jarFiles = files;
				jarDir = dir;
				break;
			}
		}
		if (jarFiles == null)
			throw new FileNotFoundException("No directory containing \"" + regexFilePattern
					+ "\" found.");
		URL[] urls = new URL[jarFiles.length];
		for (int i = 0; i < urls.length; i++) {
			urls[i] = new URL("jar", "", "file:" + jarFiles[i].getAbsolutePath() + "!/");
		}
		return new ExtensionClassLoader(urls, jarDir);
	}

	@Override
	protected String findLibrary(String libname) {
		String mappedLibname = System.mapLibraryName(libname);
		File f = new File(jarDir, mappedLibname);
		if (f.isFile())
			return f.getAbsolutePath();
		else
			return null;
	}

}
