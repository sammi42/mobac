package tac.tar;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class TarUtilities {

	/***
	 * Method for recursively iterate through a directory and save paths to a
	 * List
	 **/
	public static List<File> getDirectoryContent(File theDirectoryToList) {

		ArrayList<File> folderContent;
		folderContent = new ArrayList<File>();

		File[] directoryContent = theDirectoryToList.listFiles();

		/***
		 * Iterate through all files in directory
		 **/
		for (int i = 0; i < directoryContent.length; i++) {

			/***
			 * If directory is found, add pathname to the List to return and do
			 * a recursive call to this method with current directory as
			 * parameter
			 **/
			if (directoryContent[i].isDirectory()) {
				folderContent.add((directoryContent[i]));

				getDirectoryContent(directoryContent[i]);
			} else {
				folderContent.add((directoryContent[i]));
			}
		}
		return folderContent;
	}
}
