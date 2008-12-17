package tac.program.interfaces;

public interface AtlasInterface {

	public String getName();
	
	public int getLayerCount();

	public LayerInterface getLayer(int index);
}
