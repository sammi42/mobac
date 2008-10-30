package tac.utilities;

public class OSUtilities {

	public enum OperatingSystem {
		Windows, Linux, MacOs, MacOsX, Solaris, Unknown
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
}
