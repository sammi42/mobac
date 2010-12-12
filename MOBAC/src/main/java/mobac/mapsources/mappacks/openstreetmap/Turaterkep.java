package mobac.mapsources.mappacks.openstreetmap;

import java.awt.Color;

import mobac.mapsources.AbstractHttpMapSource;
import mobac.mapsources.mapspace.MapSpaceFactory;
import mobac.program.interfaces.HttpMapSource;
import mobac.program.interfaces.MapSpace;
import mobac.program.model.TileImageType;

/**
 * Uses 512x512 tiles - not fully supported at the moment!
 */
public class Turaterkep extends AbstractHttpMapSource {

	private static MapSpace space = MapSpaceFactory.getInstance(512, true);

	public Turaterkep() {
		super("Turaterkep", 7, 16, TileImageType.PNG, HttpMapSource.TileUpdate.IfNoneMatch);
	}

	public String getTileUrl(int zoom, int tilex, int tiley) {
		return "http://turaterkep.hostcity.hu/tiles/" + zoom + "/" + tilex + "/" + tiley + ".png";
	}

	@Override
	public MapSpace getMapSpace() {
		return space;
	}

	@Override
	public String toString() {
		return "Turaterkep (Hungary)";
	}

	@Override
	public Color getBackgroundColor() {
		return Color.WHITE;
	}

}