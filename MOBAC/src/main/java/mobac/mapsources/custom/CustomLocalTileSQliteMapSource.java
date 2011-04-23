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
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.imageio.ImageIO;
import javax.swing.JOptionPane;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import mobac.exceptions.TileException;
import mobac.gui.mapview.PreviewMap;
import mobac.mapsources.mapspace.MapSpaceFactory;
import mobac.program.interfaces.FileBasedMapSource;
import mobac.program.interfaces.MapSpace;
import mobac.program.jaxb.ColorAdapter;
import mobac.program.model.MapSourceLoaderInfo;
import mobac.program.model.TileImageType;
import mobac.utilities.jdbc.SQLiteLoader;

import org.apache.log4j.Logger;

@XmlRootElement(name = "localTileSQLite")
public class CustomLocalTileSQliteMapSource implements FileBasedMapSource {

	private static Logger log = Logger.getLogger(CustomLocalTileSQliteMapSource.class);

	private MapSourceLoaderInfo loaderInfo = null;

	private boolean initialized = false;

	private TileImageType tileImageType = null;

	@XmlElement(nillable = false, defaultValue = "CustomLocalSQLite")
	private String name = "CustomLocalSQLite";

	private int minZoom = PreviewMap.MIN_ZOOM;

	private int maxZoom = PreviewMap.MAX_ZOOM;

	@XmlElement(required = true)
	private File sourceFile = null;

	@XmlElement(defaultValue = "#000000")
	@XmlJavaTypeAdapter(ColorAdapter.class)
	private Color backgroundColor = Color.BLACK;

	/**
	 * SQLite connection with database file
	 */
	private Connection conn = null;

	private PreparedStatement statement = null;

	public CustomLocalTileSQliteMapSource() {
		super();
	}

	protected void updateZoomLevelInfo() {
		// FileFilter ff = new NumericDirFileFilter();
		// File[] zoomDirs = sourceFile.listFiles(ff);
		// if (zoomDirs.length < 1) {
		// JOptionPane.showMessageDialog(null, "No zoom directories found:\nMap name: " + name + "\nSource folder: "
		// + sourceFolder, "\nInvaild source folder", JOptionPane.ERROR_MESSAGE);
		// initialized = true;
		// return;
		// }
		// int min = PreviewMap.MAX_ZOOM;
		// int max = PreviewMap.MIN_ZOOM;
		// for (File file : zoomDirs) {
		// int z = Integer.parseInt(file.getName());
		// min = Math.min(min, z);
		// max = Math.max(max, z);
		// }
		// minZoom = min;
		// maxZoom = max;
	}

	public synchronized void initialize() {
		if (initialized)
			return;
		if (!sourceFile.isFile()) {
			JOptionPane.showMessageDialog(null, "The specified source SQLite database does not exist:\nMap name: "
					+ name + "\nFilename: " + sourceFile, "Invaild source file", JOptionPane.ERROR_MESSAGE);
			initialized = true;
			return;
		}
		if (!SQLiteLoader.loadSQLiteOrShowError()) {
			initialized = true;
			return;
		}
		log.debug("Loading SQLite database " + sourceFile);
		String url = "jdbc:sqlite:/" + this.sourceFile;
		try {
			conn = DriverManager.getConnection(url);
		} catch (SQLException e) {
			JOptionPane.showMessageDialog(null, "The specified source SQLite database could not be loaded:\nMap name: "
					+ name + "\nFilename: " + sourceFile + "\nError: " + e.getMessage(), "Error loading database",
					JOptionPane.ERROR_MESSAGE);
			initialized = true;
			return;
		}
		try {
			statement = conn.prepareStatement("SELECT image from tiles WHERE z=? AND x=? AND y=?;");
		} catch (SQLException e) {
			closeConnection();
			JOptionPane.showMessageDialog(null, "The specified source SQLite database could not be loaded:\nMap name: "
					+ name + "\nFilename: " + sourceFile + "\nError: " + e.getMessage(), "Error loading database",
					JOptionPane.ERROR_MESSAGE);
			initialized = true;
			return;
		}
		updateZoomLevelInfo();
		initialized = true;
	}

	public synchronized byte[] getTileData(int zoom, int x, int y, LoadMethod loadMethod) throws IOException,
			TileException, InterruptedException {
		if (!initialized)
			initialize();
		try {
			PreparedStatement statement = conn.prepareStatement("SELECT image from tiles WHERE z=? AND x=? AND y=?;");
			statement.setInt(1, zoom);
			statement.setInt(2, x);
			statement.setInt(3, y);
			if (statement.execute()) {
				ResultSet rs = statement.getResultSet();
				if (!rs.next())
					return null;
				byte[] data = rs.getBytes(1);
				rs.close();
				statement.close();
				return data;
			}
		} catch (SQLException e) {
			log.error("", e);
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

	protected void closeConnection() {
		try {
			if (conn != null)
				conn.close();
		} catch (SQLException e) {
		}
		conn = null;
	}
}
