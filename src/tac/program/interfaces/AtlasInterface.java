package tac.program.interfaces;

import tac.program.model.AtlasOutputFormat;

public interface AtlasInterface extends AtlasObject, CapabilityRenameable, Iterable<LayerInterface> {

	public String getName();

	public void setName(String newName);

	public int getLayerCount();

	public LayerInterface getLayer(int index);

	public void addLayer(LayerInterface l);

	public void deleteLayer(LayerInterface l);

	public void setOutputFormat(AtlasOutputFormat atlasOutputFormat);

	public AtlasOutputFormat getOutputFormat();

	public int calculateTilesToDownload();
}
