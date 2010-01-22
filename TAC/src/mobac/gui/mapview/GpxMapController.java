package mobac.gui.mapview;

import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.math.BigDecimal;

import javax.swing.JOptionPane;

import mobac.data.gpx.gpx11.Gpx;
import mobac.gui.panels.JGpxPanel.ListModelEntry;

import org.openstreetmap.gui.jmapviewer.JMapController;
import org.openstreetmap.gui.jmapviewer.JMapViewer;
import org.openstreetmap.gui.jmapviewer.interfaces.MapSpace;


public class GpxMapController extends JMapController implements MouseListener {

	private ListModelEntry entry;

	public GpxMapController(JMapViewer map, ListModelEntry entry, boolean enabled) {
		super(map, enabled);
		this.entry = entry;
	}

	public void mouseClicked(MouseEvent e) {
		// Add new GPX point to currently selected GPX file
		disable();
		if (e.getButton() == MouseEvent.BUTTON1) {
			Gpx gpx = entry.getLayer().getGpx();
			Point p = e.getPoint();
			Point tl = ((PreviewMap) map).getTopLeftCoordinate();
			p.x += tl.x;
			p.y += tl.y;
			MapSpace mapSpace = map.getMapSource().getMapSpace();
			int maxPixel = mapSpace.getMaxPixels(map.getZoom());
			if (p.x < 0 || p.x > maxPixel || p.y < 0 || p.y > maxPixel)
				return; // outside of world region
			double lon = mapSpace.cXToLon(p.x, map.getZoom());
			double lat = mapSpace.cYToLat(p.y, map.getZoom());
			String name = JOptionPane
					.showInputDialog(null, "Plase input a name for the new point:");
			if (name == null)
				return;
			Gpx gpx11 = (Gpx) gpx;
			mobac.data.gpx.gpx11.WptType wpt = new mobac.data.gpx.gpx11.WptType();
			wpt.setName(name);
			wpt.setLat(new BigDecimal(lat));
			wpt.setLon(new BigDecimal(lon));
			gpx11.getWpt().add(wpt);
		}
		map.repaint();
	}

	public void mouseEntered(MouseEvent e) {

	}

	public void mouseExited(MouseEvent e) {

	}

	public void mousePressed(MouseEvent e) {

	}

	public void mouseReleased(MouseEvent e) {

	}

	@Override
	public void disable() {
		super.disable();
		((PreviewMap) map).getMapSelectionController().enable();
	}

}
