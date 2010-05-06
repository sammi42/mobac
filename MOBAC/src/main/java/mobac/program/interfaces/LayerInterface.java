package mobac.program.interfaces;

public interface LayerInterface extends AtlasObject, Iterable<MapInterface>, CapabilityDeletable {

	public void addMap(MapInterface map);

	public int getMapCount();

	public MapInterface getMap(int index);

	public AtlasInterface getAtlas();

	public int calculateTilesToDownload();

	public LayerInterface deepClone(AtlasInterface atlas);
}
