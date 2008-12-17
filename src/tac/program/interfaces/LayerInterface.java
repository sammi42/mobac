package tac.program.interfaces;

public interface LayerInterface {

	public String getName();
	
	public int getMapCount();

	public MapInterface getMap(int index);
	
	public AtlasInterface getAtlas();
}
