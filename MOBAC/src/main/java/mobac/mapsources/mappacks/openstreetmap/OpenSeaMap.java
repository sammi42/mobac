package mobac.mapsources.mappacks.openstreetmap;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.awt.image.ComponentColorModel;
import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.stream.MemoryCacheImageInputStream;

import mobac.exceptions.TileException;
import mobac.exceptions.UnrecoverableDownloadException;
import mobac.mapsources.AbstractHttpMapSource;
import mobac.mapsources.AbstractMultiLayerMapSource;
import mobac.program.Logging;
import mobac.program.interfaces.MapSource;
import mobac.program.model.TileImageType;
import mobac.program.tilestore.TileStore;
import mobac.program.tilestore.TileStoreEntry;
import mobac.utilities.Utilities;
import mobac.utilities.imageio.PngConstants;

/**
 *@see OpenSeaMapLayer
 */
public class OpenSeaMap extends AbstractMultiLayerMapSource {

	public static final String LAYER_OPENSEA = "http://tiles.openseamap.org/seamark/";

	public OpenSeaMap() {
		super("OpenSeaMap", TileImageType.PNG);
		mapSources = new MapSource[] { new Mapnik(), new OpenSeaMapLayer() };
		initializeValues();
	}

	/**
	 * Not working correctly:
	 * 
	 * 1. The map is a "sparse map" (only tiles are present that have content - the other are missing) <br>
	 * 2. The map layer's background is not transparent!
	 */
	public static class OpenSeaMapLayer extends AbstractHttpMapSource {

		public OpenSeaMapLayer() {
			super("OpenSeaMap", 11, 17, TileImageType.PNG, TileUpdate.LastModified);
		}

		public String getTileUrl(int zoom, int tilex, int tiley) {
			return LAYER_OPENSEA + zoom + "/" + tilex + "/" + tiley + ".png";
		}

		@Override
		public byte[] getTileData(int zoom, int x, int y, LoadMethod loadMethod) throws IOException,
				InterruptedException, TileException {
			byte[] data = super.getTileData(zoom, x, y, loadMethod);
			if (data != null && data.length == 0)
				// Non-existent tile loaded from tile store
				return null;
			return data;
		}

		@Override
		public BufferedImage getTileImage(int zoom, int x, int y, LoadMethod loadMethod) throws IOException,
				UnrecoverableDownloadException, InterruptedException {
			try {
				ImageReader reader = ImageIO.getImageReadersByFormatName("png").next();
				byte[] data = getTileData(zoom, x, y, LoadMethod.DEFAULT);
				if (data == null)
					return null;
				reader.setInput(new MemoryCacheImageInputStream(new ByteArrayInputStream(data)), false, false);
				BufferedImage image = reader.read(0);
				if (image.getTransparency() == Transparency.OPAQUE
						&& image.getColorModel() instanceof ComponentColorModel) {

					Color transparencyColor = null;
					IIOMetadata meta = reader.getImageMetadata(0);
					if (meta instanceof com.sun.imageio.plugins.png.PNGMetadata) {
						com.sun.imageio.plugins.png.PNGMetadata pngmeta;
						pngmeta = (com.sun.imageio.plugins.png.PNGMetadata) meta;
						if (!pngmeta.tRNS_present)
							return image;
						if (pngmeta.tRNS_colorType == PngConstants.COLOR_TRUECOLOR)
							transparencyColor = new Color(pngmeta.tRNS_red, pngmeta.tRNS_green, pngmeta.tRNS_blue);
						else if (pngmeta.tRNS_colorType == PngConstants.COLOR_GRAYSCALE) {
							// transparencyColor = new Color(pngmeta.tRNS_gray, pngmeta.tRNS_gray, pngmeta.tRNS_gray);

							// For an unknown reason the saved transparency color does not match the background color -
							// therefore at the moment we use a hard coded transparency color
							transparencyColor = new Color(0xfcfcfc);
						}

					} else
						transparencyColor = new Color(248, 248, 248);
					Image correctedImage = Utilities.makeColorTransparent(image, transparencyColor);
					BufferedImage image2 = new BufferedImage(image.getWidth(), image.getHeight(),
							BufferedImage.TYPE_INT_ARGB);
					Graphics2D g = image2.createGraphics();
					try {
						g.drawImage(correctedImage, 0, 0, null);
						// g.setColor(Color.RED);
						// g.drawRect(0, 0, image.getWidth() - 1, image.getHeight() - 1);
						image = image2;
					} finally {
						g.dispose();
					}
				}
				return image;
			} catch (FileNotFoundException e) {
				TileStore ts = TileStore.getInstance();
				long time = System.currentTimeMillis();
				TileStoreEntry entry = ts.createNewEntry(x, y, zoom, new byte[] {}, time, time + (1000 * 60 * 60 * 60),
						"");
				ts.putTile(entry, this);
			} catch (Exception e) {
				Logging.LOG.error("Unknown error in OpenSeaMap", e);
			}
			return null;
		}
	}
}