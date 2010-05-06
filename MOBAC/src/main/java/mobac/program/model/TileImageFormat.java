package mobac.program.model;

import java.io.OutputStream;

import javax.swing.JComboBox;

import mobac.gui.MainGUI;
import mobac.program.interfaces.TileImageDataWriter;
import mobac.program.tiledatawriter.TileImageJpegDataWriter;
import mobac.program.tiledatawriter.TileImagePng4DataWriter;
import mobac.program.tiledatawriter.TileImagePng8DataWriter;
import mobac.program.tiledatawriter.TileImagePngDataWriter;


/**
 * Defines all available image formats selectable in the {@link JComboBox} in
 * the {@link MainGUI}. Each element of this enumeration contains one instance
 * of an {@link TileImageDataWriter} instance that can perform one or more image
 * operations (e.g. color reduction) and then saves the image to an
 * {@link OutputStream}.
 * 
 * @see TileImageDataWriter
 * @see TileImagePngDataWriter
 * @see TileImagePng4DataWriter
 * @see TileImagePng8DataWriter
 * @see TileImageJpegDataWriter
 */
public enum TileImageFormat {

	PNG("PNG", new TileImagePngDataWriter()), //
	PNG8Bit("PNG 256 colors (8 bit)", new TileImagePng8DataWriter()), //
	PNG4Bit("PNG  16 colors (4 bit)", new TileImagePng4DataWriter()), //
	JPEG100("JPEG - quality 100", new TileImageJpegDataWriter(1.00)), // 
	JPEG99("JPEG - quality 99", new TileImageJpegDataWriter(0.99)), // 
	JPEG95("JPEG - quality 95", new TileImageJpegDataWriter(0.95)), // 
	JPEG90("JPEG - quality 90", new TileImageJpegDataWriter(0.90)), // 
	JPEG85("JPEG - quality 85", new TileImageJpegDataWriter(0.85)), // 
	JPEG80("JPEG - quality 80", new TileImageJpegDataWriter(0.80)), //
	JPEG70("JPEG - quality 70", new TileImageJpegDataWriter(0.70)), //
	JPEG60("JPEG - quality 60", new TileImageJpegDataWriter(0.60)), //
	JPEG50("JPEG - quality 50", new TileImageJpegDataWriter(0.50)); //

	private final String description;

	private final TileImageDataWriter dataWriter;

	private TileImageFormat(String description, TileImageDataWriter dataWriter) {
		this.description = description;
		this.dataWriter = dataWriter;
	}

	@Override
	public String toString() {
		return description;
	}

	public TileImageDataWriter getDataWriter() {
		return dataWriter;
	}

}
