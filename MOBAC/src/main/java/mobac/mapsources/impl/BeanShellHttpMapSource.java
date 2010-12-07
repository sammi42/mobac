package mobac.mapsources.impl;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;

import javax.imageio.ImageIO;

import mobac.exceptions.UnrecoverableDownloadException;
import mobac.mapsources.AbstractHttpMapSource;
import mobac.mapsources.mapspace.MapSpaceFactory;
import mobac.mapsources.mapspace.MercatorPower2MapSpace;
import mobac.program.download.TileDownLoader;
import mobac.program.interfaces.MapSpace;
import mobac.program.model.TileImageType;

import org.apache.log4j.Logger;

import bsh.EvalError;
import bsh.Interpreter;

public class BeanShellHttpMapSource extends AbstractHttpMapSource {

	private static final String AH_ERROR = "Sourced file: inline evaluation of: "
			+ "``addHeaders(conn);'' : Command not found: addHeaders( sun.net.www.protocol.http.HttpURLConnection )";

	private static int NUM = 0;
	private String name;
	private MapSpace mapSpace;
	private int minZoom = 0;
	private int maxZoom = 22;

	private Logger log = Logger.getLogger(BeanShellHttpMapSource.class);

	private final Interpreter i;

	public static BeanShellHttpMapSource load(File f) throws EvalError, IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(f), Charset.forName("UTF-8")));
		StringWriter sw = new StringWriter();
		String line = br.readLine();
		while (line != null) {
			sw.write(line + "\n");
			line = br.readLine();
		}
		br.close();
		return new BeanShellHttpMapSource(sw.toString());
	}

	public BeanShellHttpMapSource(String code) throws EvalError {
		super("", 0, 0, TileImageType.PNG);
		name = "TestMapSource" + NUM++;
		i = new Interpreter();
		i.eval("import java.net.HttpURLConnection;");
		i.eval("import mobac.program.beanshell.*;");
		i.eval(code);
		Object o = i.get("tileSize");
		if (o != null) {
			int tileSize = ((Integer) o).intValue();
			mapSpace = MapSpaceFactory.getInstance(tileSize, true);
		} else
			mapSpace = MercatorPower2MapSpace.INSTANCE_256;
		o = i.get("minZoom");
		if (o != null)
			minZoom = ((Integer) o).intValue();
		o = i.get("maxZoom");
		if (o != null)
			maxZoom = ((Integer) o).intValue();
	}

	@Override
	public synchronized HttpURLConnection getTileUrlConnection(int zoom, int tilex, int tiley) throws IOException {
		HttpURLConnection conn = null;
		try {
			String url = getTileUrl(zoom, tilex, tiley);
			conn = (HttpURLConnection) new URL(url).openConnection();
		} catch (IOException e) {
			throw e;
		} catch (Exception e) {
			log.error("", e);
			throw new IOException(e);
		}
		try {
			i.set("conn", conn);
			i.eval(String.format("addHeaders(conn);", zoom, tilex, tiley));
		} catch (EvalError e) {
			String msg = e.getMessage();
			if (!AH_ERROR.equals(msg)) {
				log.error(e.getClass() + ": " + e.getMessage(), e);
				throw new IOException(e);
			}
		}
		return conn;
	}

	public boolean testCode() throws IOException {
		return (getTileUrlConnection(minZoom, 0, 0) != null);
	}

	public String getTileUrl(int zoom, int tilex, int tiley) {
		try {
			return (String) i.eval(String.format("getTileUrl(%d,%d,%d);", zoom, tilex, tiley));
		} catch (EvalError e) {
			log.error(e.getClass() + ": " + e.getMessage(), e);
			throw new RuntimeException(e);
		}
	}

	@Override
	public byte[] getTileData(int zoom, int x, int y, LoadMethod loadMethod) throws IOException,
			UnrecoverableDownloadException, InterruptedException {
		return TileDownLoader.getImage(x, y, zoom, this);
	}

	@Override
	public BufferedImage getTileImage(int zoom, int x, int y, LoadMethod loadMethod) throws IOException,
			UnrecoverableDownloadException, InterruptedException {
		byte[] data = getTileData(zoom, x, y, LoadMethod.DEFAULT);
		return ImageIO.read(new ByteArrayInputStream(data));
	}

	@Override
	public MapSpace getMapSpace() {
		return mapSpace;
	}

	@Override
	public int getMaxZoom() {
		return maxZoom;
	}

	@Override
	public int getMinZoom() {
		return minZoom;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public TileImageType getTileImageType() {
		return null;
	}

	@Override
	public TileUpdate getTileUpdate() {
		return TileUpdate.None;
	}

	public Color getBackgroundColor() {
		return Color.BLACK;
	}
}
