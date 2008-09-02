package moller.tar;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

public class TarArchiveFileHandler {
	
	public static void writeHeaderContent(byte [] headerContent,File theDestinationFile){
	
		try {
			FileOutputStream fos = new FileOutputStream(theDestinationFile, true);
			fos.write(headerContent);
			fos.flush();
			fos.close();
		} catch (IOException iox) {
			iox.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void writeRecordContent(byte [] recordContent,File theDestinationFile){
		
		try {
			FileOutputStream fos = new FileOutputStream(theDestinationFile, true);
			fos.write(recordContent);
			fos.flush();
			fos.close();
		} catch (IOException iox) {
			iox.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}		
	}
	
	public static void writeArchive(String theFileContent, File theDestinationFile) {

		try {
			theDestinationFile.createNewFile();
		} catch (IOException ex){
			ex.printStackTrace();
		}

		if (theDestinationFile == null) {
			throw new IllegalArgumentException("File should not be null.");
		}
		Writer output = null;
		    
		try {
			//use buffering
			//FileWriter always assumes default encoding is OK!
			output = new BufferedWriter( new FileWriter(theDestinationFile, true) );
			output.write( theFileContent );
		} catch (IOException ex){	
		}
		finally {
			//flush and close both "output" and its underlying FileWriter
			if (output != null) {
				try {
					output.close();
				} catch (IOException ex){
					ex.printStackTrace();
				}
			}
		}
	}
	
	public static void writeEndofArchive(File theDestinationFile) {
		
		char [] endOfArchive = new char [1024];
		
		String endOfArchiveString = new String(endOfArchive);
	
		writeArchive(endOfArchiveString, theDestinationFile);
	}
}