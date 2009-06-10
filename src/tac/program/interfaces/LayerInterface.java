package tac.program.interfaces;

public interface LayerInterface extends AtlasObject, Iterable<MapInterface> {

	public String getName();

	public int getMapCount();

	public MapInterface getMap(int index);

	public AtlasInterface getAtlas();

	public int calculateTilesToDownload();
}
