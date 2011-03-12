package mobac.mapsources;

import java.io.IOException;

import mobac.exceptions.TileException;
import mobac.mapsources.custom.BeanShellHttpMapSource;
import mobac.program.download.TileDownLoader;
import bsh.EvalError;

public class MapEvaluatorBeanShellHttpMapSource extends BeanShellHttpMapSource {

	public MapEvaluatorBeanShellHttpMapSource(String code) throws EvalError {
		super(code);
	}

	/**
	 * Prevent tile store usage
	 */
	@Override
	public byte[] getTileData(int zoom, int x, int y, LoadMethod loadMethod) throws IOException,
			TileException, InterruptedException {
		return TileDownLoader.downloadTile(x, y, zoom, this);
	}

}
