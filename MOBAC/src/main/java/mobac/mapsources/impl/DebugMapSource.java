package mobac.mapsources.impl;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.IndexColorModel;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

import mobac.exceptions.UnrecoverableDownloadException;
import mobac.gui.mapview.PreviewMap;
import mobac.mapsources.mapspace.MercatorPower2MapSpace;
import mobac.program.interfaces.MapSource;
import mobac.program.interfaces.MapSpace;
import mobac.program.model.TileImageType;

public class DebugMapSource implements MapSource {
	private static final byte[] COLORS = { 0,// 
			(byte) 0xff, (byte) 0xff, (byte) 0xff, // white
			(byte) 0xff, (byte) 0x00, (byte) 0x00 // red
	};

	private static final IndexColorModel COLORMODEL = new IndexColorModel(8, 2, COLORS, 1, false);

	private static final Font FONT_LARGE = new Font("Sans Serif", Font.BOLD, 30);

	public DebugMapSource() {
		// TODO Auto-generated constructor stub
	}

	public Color getBackgroundColor() {
		return Color.BLACK;
	}

	public MapSpace getMapSpace() {
		return MercatorPower2MapSpace.INSTANCE_256;
	}

	public int getMaxZoom() {
		return PreviewMap.MAX_ZOOM;
	}

	public int getMinZoom() {
		return 0;
	}

	public String getName() {
		return "Debug";
	}

	public String getStoreName() {
		return null;
	}

	public byte[] getTileData(int zoom, int x, int y) throws IOException, UnrecoverableDownloadException,
			InterruptedException {
		ByteArrayOutputStream buf = new ByteArrayOutputStream(16000);
		ImageIO.write(getTileImage(zoom, x, y), "png", buf);
		return buf.toByteArray();
	}

	public BufferedImage getTileImage(int zoom, int x, int y) throws IOException, UnrecoverableDownloadException,
			InterruptedException {
		BufferedImage image = new BufferedImage(256, 256, BufferedImage.TYPE_BYTE_INDEXED, COLORMODEL);
		Graphics2D g2 = image.createGraphics();
		try {
			g2.setColor(Color.WHITE);
			g2.fillRect(0, 0, 255, 255);
			g2.setColor(Color.RED);
			g2.drawRect(0, 0, 255, 255);
			g2.drawLine(0, 0, 255, 255);
			g2.drawLine(255, 0, 0, 255);
			g2.setFont(FONT_LARGE);
			g2.drawString("x: " + x, 8, 40);
			g2.drawString("y: " + y, 8, 75);
			g2.drawString("z: " + zoom, 8, 110);
			return image;
		} finally {
			g2.dispose();
		}
	}

	public TileImageType getTileImageType() {
		return TileImageType.PNG;
	}

	@Override
	public String toString() {
		return getName();
	}

	
}
