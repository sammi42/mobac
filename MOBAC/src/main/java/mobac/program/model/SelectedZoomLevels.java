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

import java.io.StringWriter;
import java.util.TreeSet;

import mobac.gui.components.JZoomCheckBox;

public class SelectedZoomLevels {

	private TreeSet<Integer> zoomLevels = new TreeSet<Integer>();

	public SelectedZoomLevels(JZoomCheckBox[] zoomCheckboxes) {
		for (JZoomCheckBox cb : zoomCheckboxes) {
			if (cb.isSelected())
				setZoomLevelSelected(cb.getZoomLevel());
		}
	}

	public void setZoomLevelSelected(int zoomLevel) {
		zoomLevels.add(new Integer(zoomLevel));
	}

	public int[] getZoomLevels() {
		int result[] = new int[zoomLevels.size()];
		int i = 0;
		for (Integer z : zoomLevels) {
			result[i++] = z.intValue();
		}
		return result;
	}

	public int getNrOfLayers() {
		return zoomLevels.size();
	}

	@Override
	public String toString() {
		StringWriter sw = new StringWriter();
		for (int z : zoomLevels) {
			sw.append(" " + z + ",");
		}
		String r = sw.toString().trim();
		if (r.length() > 0)
			r = r.substring(0, r.length() - 1);
		return r;
	}

}
