package mobac.gui.panels;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;

import mobac.gui.components.JCollapsiblePanel;
import mobac.gui.mapview.MapEventListener;
import mobac.gui.mapview.PreviewMap;
import mobac.gui.mapview.TileStoreCoverageLayer;
import mobac.program.model.MercatorPixelCoordinate;
import mobac.utilities.GBC;

import org.openstreetmap.gui.jmapviewer.interfaces.MapSource;

public class JTileStoreCoveragePanel extends JCollapsiblePanel implements MapEventListener,
		ActionListener {

	JButton showCoverage;
	JComboBox zoomCombo;
	PreviewMap mapViewer;

	public JTileStoreCoveragePanel(PreviewMap mapViewer) {
		super("Tile store coverage");
		this.mapViewer = mapViewer;

		showCoverage = new JButton("Show coverage");
		showCoverage.addActionListener(this);
		showCoverage.setToolTipText("Display tile store coverage for the current map "
				+ "source and the selected zoom level");
		zoomCombo = new JComboBox();
		zoomCombo.setToolTipText("Select the zoom level you wish "
				+ "to display tile store coverage");
		titlePanel.setToolTipText("<html>Displays the regions for the curently "
				+ "selected map source that has been <br> downloaded and "
				+ "which are therefore offline available in the tile store (tile cache)</html>");

		contentContainer.add(new JLabel("zoom level"), GBC.std());
		contentContainer.add(zoomCombo, GBC.std());
		contentContainer.add(showCoverage, GBC.std());
		mapSourceChanged(mapViewer.getMapSource());
		mapViewer.addMapEventListener(this);
	}

	public void actionPerformed(ActionEvent e) {
		Integer zoom = (Integer) zoomCombo.getSelectedItem();
		if (zoom == null)
			return;
		TileStoreCoverageLayer tscl = new TileStoreCoverageLayer(mapViewer, zoom);
		TileStoreCoverageLayer.removeCacheCoverageLayers();
		mapViewer.mapLayers.add(tscl);
	}

	public void gridZoomChanged(int newGridZoomLevel) {
	}

	public void mapSourceChanged(MapSource newMapSource) {
		TileStoreCoverageLayer.removeCacheCoverageLayers();
		Integer selZoom = (Integer) zoomCombo.getSelectedItem();
		if (selZoom == null)
			selZoom = new Integer(8);
		Integer[] items = new Integer[newMapSource.getMaxZoom() - newMapSource.getMinZoom()];
		int zoom = newMapSource.getMinZoom();
		for (int i = 0; i < items.length; i++) {
			items[i] = new Integer(zoom++);
		}
		zoomCombo.setModel(new DefaultComboBoxModel(items));
		zoomCombo.setMaximumRowCount(10);
		zoomCombo.setSelectedItem(selZoom);
	}

	public void selectNextMapSource() {
	}

	public void selectPreviousMapSource() {
	}

	public void selectionChanged(MercatorPixelCoordinate max, MercatorPixelCoordinate min) {
	}

	public void zoomChanged(int newZoomLevel) {
	}

}
