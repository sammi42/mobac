package mobac.gui.actions;

import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;

import mobac.data.gpx.gpx11.RteType;
import mobac.data.gpx.gpx11.TrksegType;
import mobac.data.gpx.gpx11.WptType;
import mobac.gui.components.GpxEntry;
import mobac.gui.components.RteEntry;
import mobac.gui.components.TrkEntry;
import mobac.gui.components.TrksegEntry;

/**
 * Encapsulates all functionality regarding edits of loaded gpx files.
 * 
 * @author lhoeppner
 * 
 */
public class GpxEditor {
	private static GpxEditor editor = null;

	public static GpxEditor getInstance() {
		if (editor == null) {
			editor = new GpxEditor();
		}
		return editor;
	}

	/**
	 * Adds a wpt to the selected route.
	 * 
	 * @param entry
	 * @param wpt
	 */
	public void findRteAndAdd(GpxEntry entry, WptType wpt) {
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
	public void findTrksegAndAdd(GpxEntry entry, WptType wpt) {
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
}
