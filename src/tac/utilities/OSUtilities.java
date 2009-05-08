package tac.utilities;

import java.io.File;
import java.io.IOException;

import javax.naming.NameNotFoundException;
import javax.swing.JOptionPane;

import org.apache.log4j.Logger;

public class OSUtilities {

	static Logger log = Logger.getLogger(OSUtilities.class);

	public enum OperatingSystem {
		Windows, Linux, MacOs, MacOsX, Solaris, Unknown
	};

	public enum DesktopType {
		Windows, Gnome, Kde, Unknown {
			@Override
			public String toString() {
				return super.toString() + " (" + System.getProperty("sun.desktop") + ")";
			}
		}
	};

	public static OperatingSystem detectOs() {
		String osName = System.getProperty("os.name").toLowerCase();
		if (osName.indexOf("windows") > -1)
			return OperatingSystem.Windows;
		if (osName.indexOf("linux") > -1)
			return OperatingSystem.Linux;
		if (osName.indexOf("mac os x") > -1)
			return OperatingSystem.MacOsX;
		if (osName.indexOf("mac os") > -1)
			return OperatingSystem.MacOs;
		if (osName.indexOf("sunos") > -1)
			return OperatingSystem.Solaris;

		return OperatingSystem.Unknown;
	}

	public static DesktopType detectDesktopType() {
		String desktopName = System.getProperty("sun.desktop").toLowerCase().trim();
		if (desktopName.startsWith("windows"))
			return DesktopType.Windows;
		if (desktopName.startsWith("gnome"))
			return DesktopType.Gnome;
		if (desktopName.startsWith("kde"))
			return DesktopType.Kde;
		log.error("Unknown desktop type: " + desktopName);
		return DesktopType.Unknown;
	}

	public static void openFolderBrowser(File directory) throws NameNotFoundException {
		if (!directory.isDirectory())
			throw new NameNotFoundException("Directory does not exist or is not a directory");
		String[] strCmd = null;
		try {
			String dirPath = directory.getCanonicalPath();
			log.trace(dirPath);
			DesktopType dt = detectDesktopType();
			switch (dt) {
			case Windows:
				strCmd = new String[] { "rundll32.exe", "url.dll,FileProtocolHandler",
						"\"" + dirPath + "\"" };
				break;
			case Gnome:
				strCmd = new String[] { "nautilus", dirPath };
				break;
			case Kde:
				strCmd = new String[] { "konqueror", dirPath };
				break;
			default:
				JOptionPane.showMessageDialog(null, "Your desktop environment " + dt
						+ " is not supported.", "Unsupported desktop environment",
						JOptionPane.ERROR_MESSAGE);
			}
			Runtime.getRuntime().exec(strCmd);
		} catch (IOException e) {
			String cmd = "";
			for (String s : strCmd) {
				cmd += "[" + s + "]";
			}
			log.error("Error while executing \"" + strCmd + "\"", e);
		}

	}
}
