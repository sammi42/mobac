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
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;
import javax.swing.JOptionPane;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import mobac.exceptions.TileException;
import mobac.mapsources.mapspace.MapSpaceFactory;
import mobac.program.interfaces.MapSource;
import mobac.program.interfaces.MapSpace;
import mobac.program.jaxb.ColorAdapter;
import mobac.program.model.MapSourceLoaderInfo;
import mobac.program.model.TileImageType;
import mobac.utilities.Utilities;

@XmlRootElement(name = "localTrackerFiles")
public class CustomLocalTrackerFileMapSource implements MapSource {

	private MapSourceLoaderInfo loaderInfo = null;

	private boolean initialized = false;

	private String fileSyntax = null;

	private TileImageType tileImageType = null;

	@XmlElement(nillable = false, defaultValue = "CustomLocal")
	private String name = "Custom";

	private int minZoom = 0;

	private int maxZoom = 0;

	@XmlElement(required = true)
	private File sourceFolder = null;

	@XmlElement(defaultValue = "#000000")
	@XmlJavaTypeAdapter(ColorAdapter.class)
	private Color backgroundColor = Color.BLACK;

	public CustomLocalTrackerFileMapSource() {
		super();
	}

	public void afterUnmarshal(Unmarshaller u, Object parent) {
		if (!sourceFolder.isDirectory()) {
			JOptionPane.showMessageDialog(null, "The specified sorce folder does not exist:\nMap name: " + name
					+ "\nSource folder: " + sourceFolder, "\nInvaild source folder", JOptionPane.ERROR_MESSAGE);
			initialized = true;
			return;
		}
		FileFilter ff = new NumericDirFileFilter();
		File[] zoomDirs = sourceFolder.listFiles(ff);
		if (zoomDirs.length < 1) {
			JOptionPane.showMessageDialog(null, "No zoom directories found:\nMap name: " + name + "\nSource folder: "
					+ sourceFolder, "\nInvaild source folder", JOptionPane.ERROR_MESSAGE);
			initialized = true;
			return;
		}
		Arrays.sort(zoomDirs);
		minZoom = Integer.parseInt(zoomDirs[0].getName());
		maxZoom = Integer.parseInt(zoomDirs[zoomDirs.length - 1].getName());
	}

	protected synchronized void initialize() {
		if (initialized)
			return;
		try {
			FileFilter ff = new NumericDirFileFilter();
			for (File zDir : sourceFolder.listFiles(ff)) {
				for (File xDir : zDir.listFiles(ff)) {
					try {
						xDir.listFiles(new FilenameFilter() {

							String syntax = "%d/%d/%d";

							public boolean accept(File dir, String name) {
								String[] parts = name.split("\\.");
								if (parts.length < 2 || parts.length > 3)
									return false;
								syntax += "." + parts[1];
								if (parts.length == 3) {
									syntax += parts[2];
									tileImageType = TileImageType.getTileImageType(parts[2]);
								} else
									tileImageType = TileImageType.getTileImageType(parts[1]);
								fileSyntax = syntax;
								throw new RuntimeException("break");
							}
						});
					} catch (Exception e) {
					}
					break;
				}
			}

		} finally {
			// TODO: Check file system
			initialized = true;
		}
	}

	public byte[] getTileData(int zoom, int x, int y, LoadMethod loadMethod) throws IOException, TileException,
			InterruptedException {
		if (!initialized)
			initialize();
		if (fileSyntax == null)
			return null;
		try {
			return Utilities.getFileBytes(new File(sourceFolder, String.format(fileSyntax, zoom, x, y)));
		} catch (FileNotFoundException e) {
			return null;
		}
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
		return MapSpaceFactory.getInstance(256, true);
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

	private static class NumericDirFileFilter implements FileFilter {

		private Pattern p = Pattern.compile("^\\d+$");

		public boolean accept(File f) {
			if (!f.isDirectory())
				return false;
			return p.matcher(f.getName()).matches();
		}

	}
}
