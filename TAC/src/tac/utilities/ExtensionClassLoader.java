package tac.utilities;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

import org.apache.log4j.Logger;

import tac.utilities.file.RegexFileFilter;

public class ExtensionClassLoader extends URLClassLoader {

	private static final Logger log = Logger.getLogger(ExtensionClassLoader.class);
	private File jarDir;

	public ExtensionClassLoader(File jarFile) throws MalformedURLException {
		super(convertToUrl(new File[] { jarFile }));
		this.jarDir = jarFile.getParentFile();
	}

	public ExtensionClassLoader(File dir, String regexFilePattern) throws MalformedURLException {
		super(convertToUrl(dir.listFiles(new RegexFileFilter(regexFilePattern))));
		this.jarDir = dir;
	}

	public ExtensionClassLoader(File[] dirList, String regexFilePattern)
			throws MalformedURLException {
		super(convertToUrl(findMatchingDir(dirList, regexFilePattern)));
		this.jarDir = new File(getURLs()[0].getFile()).getParentFile();
	}

	static private File[] findMatchingDir(File[] dirList, String regexFilePattern) {
		for (File dir : dirList) {
			if (!dir.isDirectory())
				continue;
			File[] files = dir.listFiles(new RegexFileFilter(regexFilePattern));
			if (files.length > 0) {
				log.debug("Directory: \"" + dir.getAbsolutePath() + "\"");
				log.debug("Pattern: \"" + regexFilePattern + "\"");
				return files;
			}
		}
		return null;
	}

	static private URL[] convertToUrl(File[] jarFiles) throws MalformedURLException {
		URL[] urls = new URL[jarFiles.length];
		for (int i = 0; i < urls.length; i++) {
			urls[i] = new URL("jar", "", "file:" + jarFiles[i].getAbsolutePath() + "!/");
		}
		return urls;
	}

	@Override
	protected String findLibrary(String libname) {
		libname = System.mapLibraryName(libname);
		File f = new File(jarDir, libname);
		return f.getAbsolutePath();
	}

}
