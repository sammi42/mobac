package mobac.mapsources.custom;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import mobac.exceptions.TileException;
import mobac.gui.mapview.PreviewMap;
import mobac.mapsources.AbstractHttpMapSource;
import mobac.mapsources.mapspace.MapSpaceFactory;
import mobac.mapsources.mapspace.MercatorPower2MapSpace;
import mobac.program.download.TileDownLoader;
import mobac.program.interfaces.MapSpace;
import mobac.program.model.TileImageType;
import mobac.utilities.Charsets;
import mobac.utilities.Utilities;
import bsh.EvalError;
import bsh.Interpreter;

public class BeanShellHttpMapSource extends AbstractHttpMapSource {

	private static final String AH_ERROR = "Sourced file: inline evaluation of: "
			+ "``addHeaders(conn);'' : Command not found: addHeaders( sun.net.www.protocol.http.HttpURLConnection )";

	private static int NUM = 0;

	private final Interpreter i;

	private final boolean enableTileStore;

	public static BeanShellHttpMapSource load(File f, boolean enableTileStore) throws EvalError, IOException {
		FileInputStream in = new FileInputStream(f);
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(in, Charsets.UTF_8));
			StringWriter sw = new StringWriter();
			String line = br.readLine();
			while (line != null) {
				sw.write(line + "\n");
				line = br.readLine();
			}
			br.close();
			return new BeanShellHttpMapSource(sw.toString(), enableTileStore);
		} finally {
			Utilities.closeStream(in);
		}
	}

	public BeanShellHttpMapSource(String code, boolean enableTileStore) throws EvalError {
		super("", 0, 0, TileImageType.PNG);
		this.enableTileStore = enableTileStore;
		name = "BeanShell map source " + NUM++;
		i = new Interpreter();
		i.eval("import java.net.HttpURLConnection;");
		i.eval("import mobac.utilities.beanshell.*;");
		i.eval(code);
		Object o = i.get("name");
		if (o != null)
			name = (String) o;
		o = i.get("tileSize");
		if (o != null) {
			int tileSize = ((Integer) o).intValue();
			mapSpace = MapSpaceFactory.getInstance(tileSize, true);
		} else
			mapSpace = MercatorPower2MapSpace.INSTANCE_256;
		o = i.get("minZoom");
		if (o != null)
			minZoom = ((Integer) o).intValue();
		else
			minZoom = 0;
		o = i.get("maxZoom");
		if (o != null)
			maxZoom = ((Integer) o).intValue();
		else
			maxZoom = PreviewMap.MAX_ZOOM;
		o = i.get("tileType");
		if (o != null)
			tileType = TileImageType.getTileImageType((String) o);
		else
			throw new EvalError("tileType definition missing", null, null);
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
			i.eval("addHeaders(conn);");
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
	public byte[] getTileData(int zoom, int x, int y, LoadMethod loadMethod) throws IOException, InterruptedException,
			TileException {
		if (enableTileStore) {
			return super.getTileData(zoom, x, y, loadMethod);
		} else {
			return TileDownLoader.getImage(x, y, zoom, this);
		}
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
	public TileUpdate getTileUpdate() {
		return TileUpdate.None;
	}

	public Color getBackgroundColor() {
		return Color.BLACK;
	}

	public boolean isEnableTileStore() {
		return enableTileStore;
	}

}
