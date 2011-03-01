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
package mobac.program.atlascreators.impl.aqm;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import mobac.utilities.Utilities;

public class FlatPackCreator {
	public static final String FLAT_PACK_HEADER = "FLATPACK1";
	public static final int FILE_COPY_BUFFER_LEN = 4096;

	private String packPath = null;

	private FileOutputStream dataStream = null;

	private StringBuffer structBuffer = null;

	private long currentDataWritedSize = 0;

	private long currentNbFiles = 0;

	public FlatPackCreator(final File packPath) throws FileNotFoundException {
		this(packPath.getAbsolutePath());
	}

	public FlatPackCreator(final String packPath) throws FileNotFoundException {
		this.packPath = packPath;

		if (packPath == null)
			throw new NullPointerException("Pack file path is null.");

		dataStream = new FileOutputStream(packPath + ".tmp");
		structBuffer = new StringBuffer("");

		currentDataWritedSize = 0;
		currentNbFiles = 0;
	}

	public final void add(final String filePath, final String fileEntryName) throws IOException {
		add(new File(filePath), fileEntryName);
	}

	public final void add(final File filePath) throws IOException {
		add(filePath, filePath.getName());
	}

	public final void add(final File filePath, final String fileEntryName) throws IOException {
		// read file content
		FileInputStream in = new FileInputStream(filePath);
		byte[] buff = new byte[(int) filePath.length()];
		int read = in.read(buff);
		in.close();

		if (filePath.length() != read)
			throw new IOException("Error reading '" + filePath + "'.");

		add(buff, fileEntryName);
	}

	public final void add(final byte[] buff, final String fileEntryName) throws IOException {
		if (dataStream == null)
			throw new IOException("Write stream is null.");

		// write file size
		String fileSize = "" + buff.length + "\0";
		for (int i = 0; i < fileSize.length(); i++)
			dataStream.write(fileSize.charAt(i));

		// write file into pack data
		if (buff.length > 0)
			dataStream.write(buff);

		// write file into pack structure
		structBuffer.append("" + fileEntryName + "\0" + currentDataWritedSize + "\0");

		// update writed size
		currentDataWritedSize += buff.length + fileSize.length();
		currentNbFiles++;
	}

	public final void close() throws IOException {
		if (dataStream == null)
			throw new NullPointerException("Write stream is null.");

		// close data file
		dataStream.flush();
		dataStream.close();
		dataStream = null;

		File tmpFile = new File(packPath + ".tmp");
		// open pack file
		FileOutputStream packStream = new FileOutputStream(packPath);
		try {
			String nbFiles = "" + currentNbFiles + "\0";

			// write header
			for (int i = 0; i < FLAT_PACK_HEADER.length(); i++)
				packStream.write(FLAT_PACK_HEADER.charAt(i));

			// write struct
			String len = "" + (structBuffer.length() + nbFiles.length());
			for (int i = 0; i < len.length(); i++)
				packStream.write(len.charAt(i));
			packStream.write('\0');

			for (int i = 0; i < nbFiles.length(); i++)
				packStream.write(nbFiles.charAt(i));

			OutputStreamWriter pw = new OutputStreamWriter(packStream);
			pw.write(structBuffer.toString());
			pw.flush();

			// write data
			FileInputStream in = new FileInputStream(tmpFile);
			try {
				byte[] buffer = new byte[FILE_COPY_BUFFER_LEN];

				int read;
				while ((read = in.read(buffer)) == FILE_COPY_BUFFER_LEN)
					packStream.write(buffer);

				packStream.write(buffer, 0, read);
				packStream.flush();
				packStream.close();
			} finally {
				Utilities.closeStream(in);
			}
		} finally {
			Utilities.closeStream(packStream);
		}

		// delete temp file
		if (tmpFile.isFile())
			Utilities.deleteFile(tmpFile);

		// reset state
		packPath = null;
		structBuffer = null;
	}

}
