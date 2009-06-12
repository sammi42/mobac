package tac.program.interfaces;

public interface LayerInterface extends AtlasObject, Iterable<MapInterface>, CapabilityDeletable {

	public int getMapCount();

	public MapInterface getMap(int index);

	public AtlasInterface getAtlas();

	public int calculateTilesToDownload();

}
