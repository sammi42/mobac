package mobac.gui.mapview;

import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.math.BigDecimal;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.tree.DefaultMutableTreeNode;

import mobac.data.gpx.gpx11.Gpx;
import mobac.data.gpx.gpx11.RteType;
import mobac.data.gpx.gpx11.TrksegType;
import mobac.data.gpx.gpx11.WptType;
import mobac.gui.components.GpxEntry;
import mobac.gui.components.GpxRootEntry;
import mobac.gui.components.RteEntry;
import mobac.gui.components.TrkEntry;
import mobac.gui.components.TrksegEntry;
import mobac.gui.panels.JGpxPanel;

import org.openstreetmap.gui.jmapviewer.JMapController;
import org.openstreetmap.gui.jmapviewer.JMapViewer;
import org.openstreetmap.gui.jmapviewer.interfaces.MapSpace;

public class GpxMapController extends JMapController implements MouseListener {

	private JGpxPanel panel;
	private GpxEntry entry;

	public GpxMapController(JMapViewer map, JGpxPanel panel, boolean enabled) {
		super(map, enabled);
		this.panel = panel;
	}

	public void mouseClicked(MouseEvent e) {
		// Add new GPX point to currently selected GPX file
		disable();
		if (e.getButton() == MouseEvent.BUTTON1) {
			entry = panel.getSelectedEntry();
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
			WptType wpt = new WptType();
			wpt.setName(name);
			wpt.setLat(new BigDecimal(lat));
			wpt.setLon(new BigDecimal(lon));
			if (entry.getClass() == GpxRootEntry.class) {
				gpx11.getWpt().add(wpt);
			} else if (entry.getClass() == RteEntry.class) {
				findRteAndAdd(entry, wpt);
			} else if (entry.getClass() == TrksegEntry.class) {
				findTrksegAndAdd(entry, wpt);
			}
			panel.addWpt(wpt, entry);
		}
		map.repaint();
	}

	/**
	 * Adds a wpt to the selected route.
	 * 
	 * @param entry
	 * @param wpt
	 */
	private void findRteAndAdd(GpxEntry entry, WptType wpt) {
		List<RteType> rtes = entry.getLayer().getGpx().getRte();
		RteType rteParent = ((RteEntry) entry).getRte();
		for (RteType rte : rtes) {
			if (rte.equals(rteParent)) {
				rte.getRtept().add(wpt);
			}
		}
	}

	/**
	 * Adds a wpt to the selected track segment.
	 * 
	 * @param entry
	 * @param wpt
	 */
	private void findTrksegAndAdd(GpxEntry entry, WptType wpt) {
		// get the track the selected track segment belongs to
		DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode) entry.getNode().getParent();
		TrkEntry trkParent = (TrkEntry) parentNode.getUserObject();

		// get the selected track segment
		TrksegType trksegParent = ((TrksegEntry) entry).getTrkSeg();
		List<TrksegType> trksegs = trkParent.getTrk().getTrkseg();

		for (TrksegType trkseg : trksegs) {
			if (trkseg.equals(trksegParent)) {
				trkseg.getTrkpt().add(wpt);
			}
		}
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
