package tac.mapsources;

import java.awt.Color;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.log4j.Logger;
import org.openstreetmap.gui.jmapviewer.interfaces.MapSource;
import org.openstreetmap.gui.jmapviewer.interfaces.MapSpace;

import tac.gui.MapEvaluator;
import tac.mapsources.mapspace.MercatorPower2MapSpace;
import bsh.EvalError;
import bsh.Interpreter;

public class BeanShellMapSource implements MapSource {

	private static final String AH_ERROR = "Sourced file: inline evaluation of: "
			+ "``addHeaders(conn);'' : Command not found: addHeaders( sun.net.www.protocol.http.HttpURLConnection )";

	private static int NUM = 0;
	private String name;
	private MapSpace mapSpace;

	private Logger log = Logger.getLogger(BeanShellMapSource.class);

	private final Interpreter i;

	public BeanShellMapSource(String code) throws EvalError {
		name = "TestMapSource" + NUM++;
		i = new Interpreter();
		i.eval("import java.net.HttpURLConnection;");
		i.eval(code);
		Object o = i.get("tileSize");
		if (o != null) {
			int tileSize = ((Integer) o).intValue();
			mapSpace = new MercatorPower2MapSpace(tileSize);
		} else
			mapSpace = MercatorPower2MapSpace.INSTANCE_256;
	}

	@Override
	public synchronized HttpURLConnection getTileUrlConnection(int zoom, int tilex, int tiley)
			throws IOException {
		HttpURLConnection conn = null;
		try {
			String url = getTileUrl(zoom, tilex, tiley);
			MapEvaluator.log(String.format("x=%d;y=%d;z=%d -> %s", tilex, tiley, zoom, url));
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

	public String getTileUrl(int zoom, int tilex, int tiley) throws IOException {
		try {
			return (String) i.eval(String.format("getTileUrl(%d,%d,%d);", zoom, tilex, tiley));
		} catch (EvalError e) {
			log.error(e.getClass() + ": " + e.getMessage(), e);
			throw new IOException(e);
		}
	}

	@Override
	public MapSpace getMapSpace() {
		return mapSpace;
	}

	@Override
	public int getMaxZoom() {
		return 20;
	}

	@Override
	public int getMinZoom() {
		return 0;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getStoreName() {
		return getName();
	}

	@Override
	public String getTileType() {
		return null;
	}

	@Override
	public TileUpdate getTileUpdate() {
		return TileUpdate.None;
	}

	@Override
	public boolean allowFileStore() {
		return false;
	}

	public Color getBackgroundColor() {
		return Color.BLACK;
	}
}
