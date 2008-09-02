package moller.tac;
import java.util.Arrays;


public class SelectedZoomLevels {
	private int [] zoomLevels;
	private int nrOfLayers;
	
	public SelectedZoomLevels()  {
		zoomLevels = new int [10];
		nrOfLayers = 0;
	}
	
	public void setZoomLevelSelected(int zoomLevel, boolean isSelected) {
		if (isSelected) {
			zoomLevels[zoomLevel -1] = zoomLevel;
			nrOfLayers++;
		}
		else {
			zoomLevels[zoomLevel -1] = 99;
		}
	}
	
	public int [] getZoomLevels () {
		return zoomLevels;
	}
	
	public int getNrOfLayers () {
		return nrOfLayers;
	}
	
	public void sort () {
		Arrays.sort(zoomLevels);
	}
}