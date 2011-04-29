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
package mobac.gui.mapview.controller;

import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;

import mobac.gui.mapview.PreviewMap;
import mobac.gui.mapview.layer.PolygonSelectionLayer;

/**
 * Implements the GUI logic for the preview map panel that manages the map selection and actions triggered by key
 * strokes.
 * 
 */
public class PolygonSelectionMapController extends JMapController implements MouseListener {

	private ArrayList<Point> polygonPoints = new ArrayList<Point>();
	private PolygonSelectionLayer mapLayer = null;

	public PolygonSelectionMapController(PreviewMap map) {
		super(map, false);
		mapLayer = new PolygonSelectionLayer(this);
	}

	@Override
	public void enable() {
		map.mapLayers.add(mapLayer);
		super.enable();
	}

	@Override
	public void disable() {
		map.mapLayers.remove(mapLayer);
		super.disable();
	}

	public void mouseClicked(MouseEvent e) {
	}

	public void mousePressed(MouseEvent e) {

	}

	public void mouseReleased(MouseEvent e) {
		if (e.getButton() == MouseEvent.BUTTON1) {
			Point mapPoint = map.getTopLeftCoordinate();
			mapPoint.x += e.getX();
			mapPoint.y += e.getY();
			mapPoint = map.getMapSource().getMapSpace().changeZoom(mapPoint, map.getZoom(), PreviewMap.MAX_ZOOM);
			polygonPoints.add(mapPoint);
		}
		map.grabFocus();
		map.repaint();
	}

	public void mouseEntered(MouseEvent e) {

	}

	public void mouseExited(MouseEvent e) {

	}

	public ArrayList<Point> getPolygonPoints() {
		return polygonPoints;
	}

}
