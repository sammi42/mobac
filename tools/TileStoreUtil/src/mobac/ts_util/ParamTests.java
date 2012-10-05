package mobac.ts_util;

import java.io.File;

import mobac.utilities.file.FileExtFilter;

public class ParamTests {

	public static boolean testDir(File dir) {
		if (!dir.isDirectory()) {
			System.out.println("Error: Invalid directory \"" + dir + "\"\n");
			return false;
		}
		return true;
	}

	public static boolean testBerkelyDbDir(File dir) {
		if (!dir.isDirectory()) {
			System.out.println("Error: Invalid directory \"" + dir + "\"\n");
			return false;
		}
		File[] dbFiles = dir.listFiles(new FileExtFilter(".jdb"));
		boolean valid = (dbFiles.length > 0);
		if (!valid) {
			System.out.println("Error: Directory \"" + dir + "\" is not a valid tile store directory.\n"
					+ "No .jdb files could be found in this directory.\n");
			if (dir.getName().equalsIgnoreCase("tilestore")) {
				System.out.println("You have to specify a subdirectory of the tilestore.");
			}
		}
		return valid;
	}
}
