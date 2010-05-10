package mobac.program;

import java.io.File;

import mobac.utilities.OSUtilities;
import mobac.utilities.Utilities;
import mobac.utilities.OSUtilities.OperatingSystem;

/**
 * Provides the five common directories used within Mobile Atlas Creator:
 * <ul>
 * <li>current directory</li>
 * <li>program directory</li>
 * <li>user home directory</li>
 * <li>user settings directory</li>
 * <li>temporary directory</li>
 * </ul>
 * 
 */
public class DirectoryManager {

	// private static Logger log = Logger.getLogger(DirectoryManager.class);
	public static final File currentDir;
	public static final File programDir;
	public static final File userHomeDir;
	public static final File userSettingsDir;
	public static final File tempDir;

	static {
		currentDir = new File(System.getProperty("user.dir"));
		userHomeDir = new File(System.getProperty("user.home"));
		userSettingsDir = getUserSettingsDir();
		tempDir = new File(System.getProperty("java.io.tmpdir"));
		programDir = getProgramDir();
	}

	public static void initialize() {
		if (currentDir == null || userSettingsDir == null || tempDir == null || programDir == null)
			throw new RuntimeException("DirectoryManager failed");
	}

	/**
	 * Returns the directory from which this java program is executed
	 * 
	 * @return
	 */
	private static File getProgramDir() {
		File f = null;
		try {
			f = Utilities.getClassLocation(DirectoryManager.class);
		} catch (Exception e) {
			System.err.println(e.getMessage());
			return currentDir;
		}
		if ("bin".equals(f.getName())) // remove the bin dir -> this usually
			// happens only in a development environment
			return f.getParentFile();
		else
			return f;
	}

	/**
	 * Returns the directory where Mobile Atlas Creator saves it's application
	 * settings.
	 * 
	 * Examples:
	 * <ul>
	 * <li>English Windows XP:<br>
	 * <tt>C:\Document and Settings\%username%\Application Data\Mobile Atlas Creator</tt>
	 * <li>Vista:<br>
	 * <tt>C:\Users\%username%\Application Data\Mobile Atlas Creator</tt>
	 * <li>Linux:<br>
	 * <tt>/home/$username$/.mobac</tt></li>
	 * </ul>
	 * 
	 * @return
	 */
	private static File getUserSettingsDir() {
		if (OSUtilities.detectOs() == OperatingSystem.Windows) {
			String appData = System.getenv("APPDATA");
			if (appData == null)
				throw new RuntimeException("Unable to retrieve user application data directory - "
						+ "environment variable %APPDATA% does not exist!");
			File appDataDir = new File(appData);
			if (appDataDir.isDirectory()) {
				File mobacDataDir = new File(appData, "Mobile Atlas Creator");
				if (mobacDataDir.isDirectory() || mobacDataDir.mkdir())
					return mobacDataDir;
				else
					throw new RuntimeException("Unable to create directory \""
							+ mobacDataDir.getAbsolutePath() + "\"");
			}
		}
		File userDir = new File(System.getProperty("user.home"));
		File mobacUserDir = new File(userDir, ".mobac");
		if (!mobacUserDir.exists() && !mobacUserDir.mkdir())
			throw new RuntimeException("Unable to create directory \""
					+ mobacUserDir.getAbsolutePath() + "\"");
		return mobacUserDir;
	}
}
