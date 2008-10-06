package tac.program;

import java.util.Arrays;

public class SelectedZoomLevels {
	private int[] zoomLevels;
	private int nrOfLayers;

	public SelectedZoomLevels(int zommLevelCount) {
		zoomLevels = new int[zommLevelCount];
		for (int i = 0; i < zoomLevels.length; i++)
			zoomLevels[i] = Integer.MAX_VALUE;
		nrOfLayers = 0;
	}

	public void setZoomLevelSelected(int zoomLevel) {
		zoomLevels[zoomLevel] = zoomLevel;
		nrOfLayers++;
	}

	public int[] getZoomLevels() {
		return zoomLevels;
	}

	public int getNrOfLayers() {
		return nrOfLayers;
	}

	public void sort() {
		Arrays.sort(zoomLevels);
	}
}