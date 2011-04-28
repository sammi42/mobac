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
package mobac.gui.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JOptionPane;

import mobac.data.gpx.gpx11.TrkType;
import mobac.data.gpx.gpx11.TrksegType;
import mobac.data.gpx.interfaces.GpxPoint;
import mobac.exceptions.InvalidNameException;
import mobac.gui.MainGUI;
import mobac.gui.atlastree.JAtlasTree;
import mobac.gui.gpxtree.GpxEntry;
import mobac.gui.gpxtree.GpxRootEntry;
import mobac.gui.gpxtree.TrkEntry;
import mobac.gui.gpxtree.TrksegEntry;
import mobac.program.interfaces.AtlasInterface;
import mobac.program.interfaces.MapInterface;
import mobac.program.interfaces.MapSource;
import mobac.program.model.EastNorthCoordinate;
import mobac.program.model.Layer;
import mobac.program.model.MapPolygon;
import mobac.program.model.SelectedZoomLevels;
import mobac.program.model.Settings;
import mobac.program.model.TileImageParameters;

public class AddGpxTrackMap implements ActionListener {

	public static final AddGpxTrackMap INSTANCE = new AddGpxTrackMap();

	public void actionPerformed(ActionEvent event) {
		MainGUI mg = MainGUI.getMainGUI();
		GpxEntry entry = mg.gpxPanel.getSelectedEntry();

		if (entry == null)
			return;

		TrksegType trk = null;
		TrkType t = null;
		if (entry instanceof TrksegEntry) {
			trk = ((TrksegEntry) entry).getTrkSeg();
		} else if (entry instanceof GpxRootEntry) {
			GpxRootEntry re = (GpxRootEntry) entry;
			List<TrkType> tlist = re.getLayer().getGpx().getTrk();
			if (tlist.size() > 1) {
				JOptionPane.showMessageDialog(mg, "The select gpx file contains more than one track.\n"
						+ "Please select the desired track or track segment");
				return;
			} else if (tlist.size() == 1)
				t = tlist.get(0);
		}
		if (entry instanceof TrkEntry)
			t = ((TrkEntry) entry).getTrk();
		if (t != null) {
			if (t.getTrkseg().size() > 1) {
				JOptionPane.showMessageDialog(mg, "The select track contains more than one track segment.\n"
						+ "Please select the desired track segment");
				return;
			} else if (t.getTrkseg().size() == 1)
				trk = t.getTrkseg().get(0);
		}
		if (trk == null) {
			JOptionPane.showMessageDialog(mg, "No GPX track segment selected", "Error", JOptionPane.ERROR_MESSAGE);
			return;
		}

		JAtlasTree jAtlasTree = mg.jAtlasTree;
		final String mapNameFmt = "%s %02d";
		AtlasInterface atlasInterface = jAtlasTree.getAtlas();
		String name = mg.getUserText();
		MapSource mapSource = mg.getSelectedMapSource();
		SelectedZoomLevels sZL = mg.getSelectedZoomLevels();
		int[] zoomLevels = sZL.getZoomLevels();
		if (zoomLevels.length == 0) {
			JOptionPane.showMessageDialog(mg, "Please select at least one zoom level");
			return;
		}
		List<? extends GpxPoint> points = trk.getTrkpt();
		EastNorthCoordinate[] trackPoints = new EastNorthCoordinate[points.size()];
		for (int i = 0; i < trackPoints.length; i++) {
			GpxPoint gpxPoint = points.get(i);
			trackPoints[i] = new EastNorthCoordinate(gpxPoint.getLat().doubleValue(), gpxPoint.getLon().doubleValue());
		}

		String layerName = name;
		int c = 1;
		Layer layer = null;
		boolean success = false;
		do {
			try {
				layer = new Layer(atlasInterface, layerName);
				success = true;
			} catch (InvalidNameException e) {
				layerName = name + "_" + Integer.toString(c++);
			}
		} while (!success);

		int distance = 50;
		TileImageParameters customTileParameters = mg.getSelectedTileImageParameters();
		int maxSize = 0;
		for (int zoom : zoomLevels) {
			String mapName = String.format(mapNameFmt, new Object[] { layerName, zoom });
			MapInterface map = MapPolygon.createFromTrack(layer, mapName, mapSource, zoom, trackPoints, distance,
					customTileParameters);
			int width = map.getMaxTileCoordinate().x - map.getMinTileCoordinate().x;
			int height = map.getMaxTileCoordinate().y - map.getMinTileCoordinate().y;
			maxSize = Math.max(maxSize, Math.max(width, height));
			distance *= 2;
			layer.addMap(map);
		}
		atlasInterface.addLayer(layer);
		mg.jAtlasTree.getTreeModel().notifyNodeInsert(layer);

		if (maxSize > Settings.getInstance().maxMapSize) {
			JOptionPane.showMessageDialog(mg, "At least one map that has been added violates the maximum map size.\n"
					+ "Splitting a polygonal map into smaller pieces is not yet supported.", "Information",
					JOptionPane.INFORMATION_MESSAGE);
		}

	}
}
