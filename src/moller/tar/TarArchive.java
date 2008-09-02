package moller.tar;

import java.io.File;
import java.util.List;

public class TarArchive {

	private File sourceFile;
	private File destinationFile;

	public TarArchive(File theSourceFile, File theDestinationFile) {
		sourceFile = theSourceFile;
		destinationFile = theDestinationFile;
	}

	public void createArchive() {

		if (sourceFile.isDirectory()) {
			
			TarUtilities.emptyFolderContent();
			List<File> folderContent = TarUtilities.getDirectoryContent(sourceFile);
			
			TarHeader th = new TarHeader(sourceFile, sourceFile.getParentFile());				
			TarArchiveFileHandler.writeArchive(th.getHeaderAsString(), destinationFile);
						
			for (int i = 0; i < folderContent.size(); i++) {
				
				th = new TarHeader(folderContent.get(i), sourceFile.getParentFile());				
				//TarHeader th = new TarHeader(folderContent.get(i), sourceFile);
				TarArchiveFileHandler.writeArchive(th.getHeaderAsString(), destinationFile);
				
				if (!folderContent.get(i).isDirectory()) {
					TarRecord tr = new TarRecord(folderContent.get(i));	
					TarArchiveFileHandler.writeRecordContent(tr.getRecordContent(), destinationFile);
				}				
			}
			
			TarArchiveFileHandler.writeEndofArchive(destinationFile);
		}
		else {
			
			TarHeader th = new TarHeader(sourceFile, sourceFile);
			TarRecord tr = new TarRecord(sourceFile);
			
			TarArchiveFileHandler.writeArchive(th.getHeaderAsString(), destinationFile);
			TarArchiveFileHandler.writeRecordContent(tr.getRecordContent(), destinationFile);
		}
	}
		
	public void createCRTarArchive() {

		if (sourceFile.isDirectory()) {

			TarUtilities.emptyFolderContent();
			List<File> folderContent = TarUtilities.getDirectoryContent(sourceFile);

			for (int i = 0; i < folderContent.size(); i++) {
				
				TarHeader th = new TarHeader(folderContent.get(i), sourceFile);
				TarArchiveFileHandler.writeArchive(th.getHeaderAsString(), destinationFile);

				if (!folderContent.get(i).isDirectory()) {
					TarRecord tr = new TarRecord(folderContent.get(i));	
					TarArchiveFileHandler.writeRecordContent(tr.getRecordContent(), destinationFile);
				}
			}

			TarArchiveFileHandler.writeEndofArchive(destinationFile);
		}
	}
}