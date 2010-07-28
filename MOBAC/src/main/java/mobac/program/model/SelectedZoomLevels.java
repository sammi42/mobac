/*******************************************************************************
 * Copyright (c) MOBAC developers
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package mobac.program.model;

import javax.swing.JCheckBox;

public class SelectedZoomLevels {
	private boolean[] zoomLevels;
	private int nrOfLayers;

	public SelectedZoomLevels(int minZoom, JCheckBox[] zoomCheckboxes) {
		this(minZoom + zoomCheckboxes.length);
		for (int i = 0; i < zoomCheckboxes.length; i++) {
			if (zoomCheckboxes[i].isSelected())
				setZoomLevelSelected(minZoom + i);
		}
	}

	public SelectedZoomLevels(int zommLevelCount) {
		zoomLevels = new boolean[zommLevelCount];
		for (int i = 0; i < zoomLevels.length; i++)
			zoomLevels[i] = false;
		nrOfLayers = 0;
	}

	public void setZoomLevelSelected(int zoomLevel) {
		zoomLevels[zoomLevel] = true;
		nrOfLayers++;
	}

	public int[] getZoomLevels() {
		int result[] = new int[nrOfLayers];
		int j = 0;
		for (int i = 0; i < zoomLevels.length; i++) {
			if (zoomLevels[i])
				result[j++] = i;
		}
		return result;
	}

	public int getNrOfLayers() {
		return nrOfLayers;
	}

	@Override
	public String toString() {
		String r = "";
		for (int i = 0; i < zoomLevels.length; i++) {
			if (zoomLevels[i])
				r += " " + i + ",";
		}
		r = r.trim();
		if (r.length() > 0)
			r = r.substring(0, r.length() - 1);
		return r;
	}

}
