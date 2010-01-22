package mobac.program.interfaces;

import mobac.program.model.AtlasOutputFormat;

public interface AtlasInterface extends AtlasObject, Iterable<LayerInterface> {

	/**
	 * @return Number of layers in this atlas
	 */
	public int getLayerCount();

	/**
	 * 
	 * @param index
	 *            0 - ({@link #getLayerCount()}-1)
	 * @return
	 */
	public LayerInterface getLayer(int index);

	public void addLayer(LayerInterface l);

	public void deleteLayer(LayerInterface l);

	public void setOutputFormat(AtlasOutputFormat atlasOutputFormat);

	public AtlasOutputFormat getOutputFormat();

	public int calculateTilesToDownload();

	public int getVersion();
	
	public AtlasInterface deepClone();
}
