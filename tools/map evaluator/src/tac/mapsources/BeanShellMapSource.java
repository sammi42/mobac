package tac.mapsources;

import org.apache.log4j.Logger;

import bsh.EvalError;
import bsh.Interpreter;

public class BeanShellMapSource extends AbstractMapSource {

	private static int NUM = 0;

	private Logger log = Logger.getLogger(BeanShellMapSource.class);

	private final String code;

	public BeanShellMapSource(String code) {
		super("TestMapSource" + Integer.toString(NUM++), 0, 20, "");
		this.code = code;
	}

	@Override
	public boolean allowFileStore() {
		return false;
	}

	@Override
	public String getTileUrl(int zoom, int tilex, int tiley) {
		try {
			return evalTileUrl(zoom, tilex, tiley);
		} catch (EvalError e) {
			log.error("", e);
			return null;
		}
	}

	public String evalTileUrl(int zoom, int tilex, int tiley) throws EvalError {
		Interpreter i = new Interpreter();
		i.eval(code);
		return (String) i.eval(String.format("getTileUrl(%d,%d,%d);", zoom, tilex, tiley));
	}
}
