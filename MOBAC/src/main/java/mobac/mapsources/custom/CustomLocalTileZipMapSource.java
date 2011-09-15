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
package mobac.mapsources.custom;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.imageio.ImageIO;
import javax.swing.JOptionPane;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import mobac.exceptions.TileException;
import mobac.gui.mapview.PreviewMap;
import mobac.mapsources.mapspace.MapSpaceFactory;
import mobac.program.Logging;
import mobac.program.interfaces.FileBasedMapSource;
import mobac.program.interfaces.MapSpace;
import mobac.program.jaxb.ColorAdapter;
import mobac.program.model.MapSourceLoaderInfo;
import mobac.program.model.TileImageType;
import mobac.utilities.Utilities;

@XmlRootElement(name = "localTileZip")
public class CustomLocalTileZipMapSource implements FileBasedMapSource {

	private MapSourceLoaderInfo loaderInfo = null;

	private MapSpace mapSpace = MapSpaceFactory.getInstance(256, true);

	private boolean initialized = false;

	private String fileSyntax = null;

	private TileImageType tileImageType = null;

	@XmlElement(nillable = false, defaultValue = "CustomLocal")
	private String name = "Custom";

	private int minZoom = PreviewMap.MIN_ZOOM;

	private int maxZoom = PreviewMap.MAX_ZOOM;

	@XmlElement(name = "zipFile", required = true)
	private File[] zipFiles = new File[] {};

	@XmlElement(defaultValue = "false")
	private boolean flipXYDir = false;

	@XmlElement(defaultValue = "false")
	private boolean invertYCoordinate = false;

	private LinkedList<ZipFile> zips = new LinkedList<ZipFile>();

	@XmlElement(defaultValue = "#000000")
	@XmlJavaTypeAdapter(ColorAdapter.class)
	private Color backgroundColor = Color.BLACK;

	public CustomLocalTileZipMapSource() {
		super();
	}

	protected synchronized void openZipFile() {
		for (File zipFile : zipFiles) {
			if (!zipFile.isFile()) {
				JOptionPane.showMessageDialog(null, "The specified sorce zip does not exist:\nMap name: " + name
						+ "\nZip file: " + zipFile, "\nInvaild source zip", JOptionPane.ERROR_MESSAGE);
			} else {
				try {
					Logging.LOG.debug("Opening zip file " + zipFile.getAbsolutePath());
					zips.add(new ZipFile(zipFile));
					Logging.LOG.debug("Zip file open completed");
				} catch (Exception e) {
					JOptionPane.showMessageDialog(null, "The specified sorce zip can not be read:\nMap name: " + name
							+ "\nZip file: " + zipFile, "\nError reading zip file", JOptionPane.ERROR_MESSAGE);
				}
			}
		}
	}

	protected void updateZoomInfo() {
		int min = PreviewMap.MAX_ZOOM;
		int max = PreviewMap.MIN_ZOOM;
		for (ZipFile zip : zips) {
			for (int z = PreviewMap.MAX_ZOOM; z > PreviewMap.MIN_ZOOM; z--) {
				ZipEntry entry = zip.getEntry(Integer.toString(z) + "/");
				if (entry != null) {
					max = Math.max(max, z);
					break;
				}
			}
			for (int z = PreviewMap.MIN_ZOOM; z < PreviewMap.MAX_ZOOM; z++) {
				ZipEntry entry = zip.getEntry(Integer.toString(z) + "/");
				if (entry != null) {
					min = Math.min(min, z);
					break;
				}
			}
		}
		minZoom = min;
		maxZoom = max;
	}

	public synchronized void initialize() {
		if (initialized)
			return;
		try {
			openZipFile();
			updateZoomInfo();
			if (zips.size() == 0)
				return;
			Enumeration<? extends ZipEntry> entries = zips.get(0).entries();
			String syntax = "%d/%d/%d";
			while (entries.hasMoreElements()) {
				ZipEntry entry = entries.nextElement();
				if (entry.isDirectory())
					continue;
				String name = entry.getName();
				int i = name.lastIndexOf("/");
				name = name.substring(i + 1);

				String[] parts = name.split("\\.");
				if (parts.length < 2 || parts.length > 3)
					break;
				syntax += "." + parts[1];
				if (parts.length == 3) {
					syntax += parts[2];
					tileImageType = TileImageType.getTileImageType(parts[2]);
				} else
					tileImageType = TileImageType.getTileImageType(parts[1]);
				fileSyntax = syntax;
				break;
			}
		} finally {
			initialized = true;
		}
	}

	public byte[] getTileData(int zoom, int x, int y, LoadMethod loadMethod) throws IOException, TileException,
			InterruptedException {
		if (!initialized)
			initialize();
		if (fileSyntax == null)
			return null;
		if (invertYCoordinate)
			y = ((1 << zoom) - y - 1);
		ZipEntry entry = null;
		for (ZipFile zip : zips) {
			String fileName;
			if (flipXYDir)
				fileName = String.format(fileSyntax, zoom, y, x);
			else
				fileName = String.format(fileSyntax, zoom, x, y);
			entry = zip.getEntry(fileName);
			if (entry != null) {
				InputStream in = zip.getInputStream(entry);
				byte[] data = Utilities.getInputBytes(in);
				in.close();
				return data;
			}
		}
		return null;
	}

	public BufferedImage getTileImage(int zoom, int x, int y, LoadMethod loadMethod) throws IOException, TileException,
			InterruptedException {
		byte[] data = getTileData(zoom, x, y, loadMethod);
		if (data == null)
			return null;
		return ImageIO.read(new ByteArrayInputStream(data));
	}

	public TileImageType getTileImageType() {
		return tileImageType;
	}

	public int getMaxZoom() {
		return maxZoom;
	}

	public int getMinZoom() {
		return minZoom;
	}

	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		return name;
	}

	public MapSpace getMapSpace() {
		return mapSpace;
	}

	public Color getBackgroundColor() {
		return backgroundColor;
	}

	@XmlTransient
	public MapSourceLoaderInfo getLoaderInfo() {
		return loaderInfo;
	}

	public void setLoaderInfo(MapSourceLoaderInfo loaderInfo) {
		this.loaderInfo = loaderInfo;
	}

}
